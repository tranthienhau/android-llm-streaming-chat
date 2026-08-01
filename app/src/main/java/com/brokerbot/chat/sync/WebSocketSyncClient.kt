package com.brokerbot.chat.sync

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import com.brokerbot.chat.model.SyncEvent
import com.brokerbot.chat.model.SyncPhase
import com.brokerbot.chat.model.SyncState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Real implementation of the live-sync channel:
 *  - discovers the master with NSD (mDNS / `_brokerbot._tcp`),
 *  - opens a low-latency OkHttp WebSocket (15s keep-alive ping),
 *  - measures the clock offset NTP-style (offset = ((t2-t1)+(t3-t4))/2), and
 *  - reconnects with exponential backoff on any failure.
 *
 * Runs inside [com.brokerbot.chat.service.SyncService] so the socket + timer
 * survive backgrounding. On a single emulator with no master, use
 * [FakeSyncClient] instead - both satisfy [SyncClient].
 */
class WebSocketSyncClient(
    context: Context,
    private val serviceType: String = "_brokerbot._tcp",
) : SyncClient {

    private val nsd = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val multicastLock =
        (context.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            .createMulticastLock("brokerbot-sync").apply { setReferenceCounted(true) }

    private val http = OkHttpClient.Builder()
        .pingInterval(15, TimeUnit.SECONDS) // detect a dead peer
        .build()

    private val json = Json { ignoreUnknownKeys = true }
    private val clock = SimpleDateFormat("HH:mm:ss", Locale.US)
    private fun now() = clock.format(System.currentTimeMillis())

    @Serializable private data class Ping(val type: String = "ping", val t1: Long)
    @Serializable private data class Pong(val t1: Long, val t2: Long, val t3: Long)

    override fun connect(): Flow<SyncState> = callbackFlow {
        var state = SyncState()
        val log = ArrayDeque<SyncEvent>()
        fun mutate(block: (SyncState) -> SyncState) { state = block(state); trySend(state) }
        fun log(text: String) = mutate {
            log.addLast(SyncEvent(now(), text))
            while (log.size > 6) log.removeFirst()
            it.copy(log = log.toList())
        }

        var socket: WebSocket? = null
        var backoffMs = 1000L
        var beat = 0L
        // Forward handle so the WebSocket failure callback can restart discovery.
        lateinit var startDiscovery: () -> Unit

        fun sendPing(ws: WebSocket) =
            ws.send(json.encodeToString(Ping.serializer(), Ping(t1 = System.currentTimeMillis())))

        val wsListener = object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                backoffMs = 1000L
                log("websocket connected")
                sendPing(ws)
            }

            override fun onMessage(ws: WebSocket, text: String) {
                val t4 = System.currentTimeMillis()
                val pong = runCatching { json.decodeFromString(Pong.serializer(), text) }.getOrNull()
                    ?: return
                val offset = ((pong.t2 - pong.t1) + (pong.t3 - t4)) / 2
                val rtt = ((t4 - pong.t1) - (pong.t3 - pong.t2)).coerceAtLeast(0)
                beat++
                mutate {
                    it.copy(phase = SyncPhase.CONNECTED, offsetMs = offset, rttMs = rtt, heartbeatBeat = beat)
                }
                launch { delay(5000); if (isActive) sendPing(ws) } // next offset probe
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                mutate { it.copy(phase = SyncPhase.RECONNECTING) }
                log("connection lost: ${t.message ?: "error"}, backoff ${backoffMs}ms")
                launch {
                    delay(backoffMs)
                    backoffMs = (backoffMs * 2).coerceAtMost(30_000)
                    startDiscovery()
                }
            }
        }

        fun openSocket(info: NsdServiceInfo) {
            val host = info.host?.hostAddress ?: return
            mutate {
                it.copy(
                    phase = SyncPhase.CONNECTING,
                    peerName = info.serviceName,
                    peerAddress = "$host:${info.port}",
                )
            }
            log("discovered ${info.serviceName} at $host:${info.port}")
            socket = http.newWebSocket(Request.Builder().url("ws://$host:${info.port}").build(), wsListener)
        }

        val resolveListener = object : NsdManager.ResolveListener {
            override fun onServiceResolved(info: NsdServiceInfo) = openSocket(info)
            override fun onResolveFailed(info: NsdServiceInfo, code: Int) { log("resolve failed ($code)") }
        }

        val discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onServiceFound(info: NsdServiceInfo) {
                if (info.serviceType.contains(serviceType.trim('.'))) nsd.resolveService(info, resolveListener)
            }
            override fun onDiscoveryStarted(t: String) { log("discovering $serviceType via NSD") }
            override fun onServiceLost(info: NsdServiceInfo) { log("service lost") }
            override fun onDiscoveryStopped(t: String) {}
            override fun onStartDiscoveryFailed(t: String, code: Int) { log("discovery failed ($code)") }
            override fun onStopDiscoveryFailed(t: String, code: Int) {}
        }

        startDiscovery = {
            runCatching { nsd.stopServiceDiscovery(discoveryListener) }
            mutate { it.copy(phase = SyncPhase.DISCOVERING) }
            nsd.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        }

        multicastLock.acquire()
        startDiscovery()

        awaitClose {
            runCatching { nsd.stopServiceDiscovery(discoveryListener) }
            socket?.close(1000, "bye")
            if (multicastLock.isHeld) multicastLock.release()
        }
    }
}
