package com.brokerbot.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brokerbot.chat.ui.theme.Accent
import com.brokerbot.chat.ui.theme.TextSecondary
import com.brokerbot.chat.viewmodel.ChatViewModel

enum class Tab(val label: String, val icon: ImageVector) {
    CHAT("Chat", Icons.AutoMirrored.Filled.Chat),
    HISTORY("History", Icons.Default.History),
    SYNC("Sync", Icons.Default.Sync),
    SETTINGS("Settings", Icons.Default.Settings),
}

@Composable
fun AppRoot(viewModel: ChatViewModel, startTab: Tab = Tab.CHAT) {
    var tab by remember { mutableStateOf(startTab) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(Modifier.weight(1f)) {
            when (tab) {
                Tab.CHAT -> ChatScreen(viewModel)
                Tab.HISTORY -> HistoryScreen(onNewChat = { tab = Tab.CHAT })
                Tab.SYNC -> SyncScreen()
                Tab.SETTINGS -> SettingsScreen()
            }
        }
        BottomBar(active = tab, onSelect = { tab = it })
    }
}

@Composable
private fun BottomBar(active: Tab, onSelect: (Tab) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        Tab.entries.forEach { t ->
            val selected = t == active
            val tint = if (selected) Accent else TextSecondary
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onSelect(t) }.padding(horizontal = 8.dp),
            ) {
                Icon(t.icon, contentDescription = t.label, tint = tint, modifier = Modifier.size(24.dp))
                Spacer(Modifier.height(4.dp))
                Text(
                    t.label,
                    color = tint,
                    fontSize = 12.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}
