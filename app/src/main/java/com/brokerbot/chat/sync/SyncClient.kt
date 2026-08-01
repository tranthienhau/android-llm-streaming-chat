package com.brokerbot.chat.sync

import com.brokerbot.chat.model.SyncState
import kotlinx.coroutines.flow.Flow

/**
 * Drives the real-time device-sync channel: discover a master on the LAN,
 * open a low-latency socket, measure the clock offset, and keep the connection
 * alive with heartbeats + reconnect. Emits a [SyncState] whenever anything
 * changes. Two implementations mirror the [com.brokerbot.chat.network.StreamClient]
 * pattern: [WebSocketSyncClient] (real NSD + OkHttp WebSocket) and
 * [FakeSyncClient] (offline loopback for demo / simulator).
 */
interface SyncClient {
    fun connect(): Flow<SyncState>
}
