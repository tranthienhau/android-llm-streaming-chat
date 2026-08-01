package com.brokerbot.chat.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.brokerbot.chat.model.SyncPhase
import com.brokerbot.chat.sync.FakeSyncClient
import com.brokerbot.chat.sync.SyncClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Foreground service that owns the live-sync channel so the WebSocket, clock-offset
 * loop, and heartbeats keep running while the app is backgrounded - the piece that
 * makes long-running, continuous operation viable on Android. It collects a
 * [SyncClient] and republishes each [com.brokerbot.chat.model.SyncState] to [SyncBus].
 *
 * Demo build uses [FakeSyncClient]; swap for
 * `WebSocketSyncClient(applicationContext)` to sync against a real master on the LAN.
 */
class SyncService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var job: Job? = null
    private val client: SyncClient by lazy { FakeSyncClient() }

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIF_ID, buildNotification("Discovering devices"))
        job = client.connect()
            .onEach { state ->
                SyncBus.publish(state)
                updateNotification(state.phase)
            }
            .launchIn(scope)
    }

    // START_STICKY: if the OS kills us under memory pressure, restart and reconnect.
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        job?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    private fun updateNotification(phase: SyncPhase) {
        val text = when (phase) {
            SyncPhase.CONNECTED -> "Synced with master"
            SyncPhase.RECONNECTING -> "Reconnecting..."
            SyncPhase.CONNECTING -> "Connecting..."
            SyncPhase.DISCONNECTED -> "Disconnected"
            SyncPhase.DISCOVERING -> "Discovering devices"
        }
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID, buildNotification(text))
    }

    private fun buildNotification(text: String): Notification {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL, "Live Sync", NotificationManager.IMPORTANCE_LOW),
            )
        }
        return Notification.Builder(this, CHANNEL)
            .setContentTitle("BrokerBot Live Sync")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val CHANNEL = "live_sync"
        private const val NOTIF_ID = 42

        fun start(context: Context) {
            val intent = Intent(context, SyncService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
