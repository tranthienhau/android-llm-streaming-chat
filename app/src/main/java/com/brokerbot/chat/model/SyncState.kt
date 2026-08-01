package com.brokerbot.chat.model

/** Connection lifecycle for the real-time device-sync channel. */
enum class SyncPhase { DISCOVERING, CONNECTING, CONNECTED, RECONNECTING, DISCONNECTED }

data class SyncEvent(val timeLabel: String, val text: String)

/**
 * Snapshot of the live-sync channel, published by [com.brokerbot.chat.service.SyncService].
 *
 * @param offsetMs synced clock offset vs the master (add to local clock).
 * @param rttMs measured round-trip latency to the master.
 * @param cueEpochMs the shared countdown target on the master's synced clock.
 */
data class SyncState(
    val phase: SyncPhase = SyncPhase.DISCOVERING,
    val peerName: String? = null,
    val peerAddress: String? = null,
    val offsetMs: Long = 0,
    val rttMs: Long = 0,
    val cueEpochMs: Long = 0,
    val heartbeatBeat: Long = 0,
    val log: List<SyncEvent> = emptyList(),
) {
    val isConnected: Boolean get() = phase == SyncPhase.CONNECTED
}
