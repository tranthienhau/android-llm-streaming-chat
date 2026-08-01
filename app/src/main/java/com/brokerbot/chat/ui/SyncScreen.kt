package com.brokerbot.chat.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brokerbot.chat.model.SyncPhase
import com.brokerbot.chat.model.SyncState
import com.brokerbot.chat.service.SyncBus
import com.brokerbot.chat.ui.theme.Accent
import com.brokerbot.chat.ui.theme.AccentSoft
import com.brokerbot.chat.ui.theme.OnlineGreen
import com.brokerbot.chat.ui.theme.SurfaceAlt
import com.brokerbot.chat.ui.theme.TextSecondary
import kotlinx.coroutines.delay

private const val CUE_TOTAL_MS = 5 * 60_000f
private val Amber = Color(0xFFF59E0B)

@Composable
fun SyncScreen() {
    val state by SyncBus.state.collectAsStateWithLifecycle()

    // Tick once a second; remaining is derived from the master-synced clock:
    // remaining = cueEpoch - (localNow + offset).
    val remainingMs by produceState(0L, state.cueEpochMs, state.offsetMs) {
        while (true) {
            val synced = System.currentTimeMillis() + state.offsetMs
            value = (state.cueEpochMs - synced).coerceAtLeast(0)
            delay(250)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Header(state.phase)
        Spacer(Modifier.height(16.dp))
        StatusCard(state)
        Spacer(Modifier.height(14.dp))
        StatRow(state)
        Spacer(Modifier.height(24.dp))
        Countdown(remainingMs)
        Spacer(Modifier.height(20.dp))
        Heartbeat(state)
        Spacer(Modifier.height(24.dp))
        EventLog(state)
    }
}

@Composable
private fun Header(phase: SyncPhase) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Live Sync", fontSize = 26.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        val running = phase != SyncPhase.DISCONNECTED
        Row(
            Modifier.clip(RoundedCornerShape(20.dp))
                .background(if (running) Color(0xFFE7F9EE) else SurfaceAlt)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(if (running) OnlineGreen else TextSecondary))
            Spacer(Modifier.width(6.dp))
            Text(
                if (running) "SERVICE RUNNING" else "STOPPED",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (running) Color(0xFF16A34A) else TextSecondary,
            )
        }
    }
}

@Composable
private fun StatusCard(state: SyncState) {
    val connected = state.phase == SyncPhase.CONNECTED
    val reconnecting = state.phase == SyncPhase.RECONNECTING
    val bg by animateColorAsState(
        when {
            connected -> Accent
            reconnecting -> Amber
            else -> TextSecondary
        },
        label = "cardbg",
    )
    val label = when (state.phase) {
        SyncPhase.CONNECTED -> "Connected"
        SyncPhase.CONNECTING -> "Connecting"
        SyncPhase.DISCOVERING -> "Discovering"
        SyncPhase.RECONNECTING -> "Reconnecting"
        SyncPhase.DISCONNECTED -> "Disconnected"
    }
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(bg).padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(9.dp).clip(CircleShape).background(if (connected) OnlineGreen else Color.White))
            Spacer(Modifier.width(8.dp))
            Text(label, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(10.dp))
        Text(state.peerName ?: "-", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(state.peerAddress ?: "searching the local network", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
    }
}

@Composable
private fun StatRow(state: SyncState) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatChip(Icons.Default.NetworkPing, "Latency", "${state.rttMs} ms", Modifier.weight(1f))
        val sign = if (state.offsetMs >= 0) "+" else ""
        StatChip(Icons.Default.Schedule, "Clock offset", "$sign${state.offsetMs} ms", Modifier.weight(1f))
    }
}

@Composable
private fun StatChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(16.dp)).background(SurfaceAlt).padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, color = TextSecondary, fontSize = 12.sp)
        }
        Spacer(Modifier.height(6.dp))
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun Countdown(remainingMs: Long) {
    val totalSec = (remainingMs / 1000)
    val mm = (totalSec / 60).toString().padStart(2, '0')
    val ss = (totalSec % 60).toString().padStart(2, '0')
    val progress = (remainingMs / CUE_TOTAL_MS).coerceIn(0f, 1f)
    Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(200.dp)) {
            val stroke = 10.dp.toPx()
            val topLeft = Offset(stroke / 2, stroke / 2)
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(Color(0xFFE9EBF2), -90f, 360f, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
            drawArc(Accent, -90f, -360f * progress, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("NEXT CUE", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            Text("$mm:$ss", fontSize = 52.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun Heartbeat(state: SyncState) {
    val transition = rememberInfiniteTransition(label = "hb")
    val scale by transition.animateFloat(
        1f, 1.6f,
        infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "hbscale",
    )
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(10.dp).scale(if (state.isConnected) scale else 1f)
                .clip(CircleShape).background(if (state.isConnected) Accent else TextSecondary),
        )
        Spacer(Modifier.width(10.dp))
        Text("heartbeat 1s  -  beat ${state.heartbeatBeat}", color = TextSecondary, fontSize = 13.sp)
    }
}

@Composable
private fun EventLog(state: SyncState) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Bolt, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text("Event Log", fontWeight = FontWeight.SemiBold)
    }
    Spacer(Modifier.height(10.dp))
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(SurfaceAlt).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (state.log.isEmpty()) {
            Text("waiting for events...", color = TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
        }
        state.log.forEach { e ->
            Row {
                Text(e.timeLabel, color = TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                Spacer(Modifier.width(10.dp))
                Text(e.text, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    Box(
        Modifier.clip(RoundedCornerShape(8.dp)).background(AccentSoft).padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text("foreground service - survives backgrounding", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
