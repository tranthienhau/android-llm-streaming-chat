package com.brokerbot.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brokerbot.chat.data.DemoData
import com.brokerbot.chat.model.Conversation
import com.brokerbot.chat.ui.theme.Accent
import com.brokerbot.chat.ui.theme.AccentSoft
import com.brokerbot.chat.ui.theme.SurfaceAlt
import com.brokerbot.chat.ui.theme.TextSecondary

@Composable
fun HistoryScreen(onNewChat: () -> Unit) {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 20.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text("Chats", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                SearchBar()
                Spacer(Modifier.height(4.dp))
            }
            items(DemoData.conversations, key = { it.id }) { ConversationCard(it) }
        }
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .size(56.dp)
                .clip(CircleShape)
                .background(Accent)
                .clickable { onNewChat() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Add, contentDescription = "New chat", tint = Color.White)
        }
    }
}

@Composable
private fun SearchBar() {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceAlt)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary)
        Spacer(Modifier.width(10.dp))
        Text("Search chats", color = TextSecondary)
    }
}

@Composable
private fun ConversationCard(c: Conversation) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceAlt)
            .clickable {}
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(c.title, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text(c.timeLabel, color = TextSecondary, fontSize = 13.sp)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            c.preview,
            color = TextSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            fontSize = 14.sp,
        )
        Spacer(Modifier.height(10.dp))
        Box(
            Modifier.clip(RoundedCornerShape(8.dp)).background(AccentSoft)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text(c.model.defaultModel, color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}
