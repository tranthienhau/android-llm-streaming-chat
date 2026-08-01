package com.brokerbot.chat.sync

import com.brokerbot.chat.model.SyncEvent
import com.brokerbot.chat.model.SyncPhase
import com.brokerbot.chat.model.SyncState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Offline stand-in for [WebSocketSyncClient] so the sync screen is fully demoable
 * on a simulator with no second device. Reproduces the real lifecycle: NSD
 * discovery, connect, clock-offset measurement, 1s heartbeats, and a simulated
 * network drop followed by backoff + reconnect (offset re-measured).
 */
class FakeSyncClient : SyncClient {

    private val clock = SimpleDateFormat("HH:mm:ss", Locale.US)
    private fun now() = clock.format(System.currentTimeMillis())

    override fun connect(): Flow<SyncState> = flow {
        val cueEpoch = System.currentTimeMillis() + 5 * 60_000L // shared 5-min cue
        val log = ArrayDeque<SyncEvent>()
        fun log(text: String): List<SyncEvent> {
            log.addLast(SyncEvent(now(), text))
            while (log.size > 6) log.removeFirst()
            return log.toList()
        }

        emit(SyncState(phase = SyncPhase.DISCOVERING, log = log("discovering _brokerbot._tcp via NSD")))
        delay(1400)
        emit(SyncState(phase = SyncPhase.CONNECTING, peerName = PEER, peerAddress = ADDR,
            log = log("discovered $PEER at $ADDR")))
        delay(900)

        var offset = 37L
        var beat = 0L
        emit(connected(offset, 9, cueEpoch, beat, log("websocket connected")))
        emit(connected(offset, 9, cueEpoch, beat, log("clock offset synced +${offset}ms")))

        while (true) {
            // steady heartbeats with small latency jitter
            repeat(9) {
                delay(1000)
                beat++
                val rtt = 8L + (beat % 5)
                val l = if (beat % 3 == 0L) log("heartbeat ok  rtt ${rtt}ms") else log.toList()
                emit(connected(offset, rtt, cueEpoch, beat, l))
            }
            // simulate an unstable-network drop + backoff reconnect
            emit(SyncState(phase = SyncPhase.RECONNECTING, peerName = PEER, peerAddress = ADDR,
                cueEpochMs = cueEpoch, offsetMs = offset, heartbeatBeat = beat,
                log = log("connection lost, backoff 2s")))
            delay(2200)
            offset = 41L // re-measured after reconnect
            emit(connected(offset, 12, cueEpoch, beat, log("reconnected, offset re-synced +${offset}ms")))
        }
    }

    private fun connected(offset: Long, rtt: Long, cue: Long, beat: Long, log: List<SyncEvent>) =
        SyncState(
            phase = SyncPhase.CONNECTED,
            peerName = PEER,
            peerAddress = ADDR,
            offsetMs = offset,
            rttMs = rtt,
            cueEpochMs = cue,
            heartbeatBeat = beat,
            log = log,
        )

    private companion object {
        const val PEER = "studio-master"
        const val ADDR = "192.168.1.42:8973"
    }
}
