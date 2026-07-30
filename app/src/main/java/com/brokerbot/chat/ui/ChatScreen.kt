package com.brokerbot.chat.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brokerbot.chat.data.DemoData
import com.brokerbot.chat.model.ChatMessage
import com.brokerbot.chat.model.Role
import com.brokerbot.chat.ui.theme.Accent
import com.brokerbot.chat.ui.theme.AccentSoft
import com.brokerbot.chat.ui.theme.AssistantBubble
import com.brokerbot.chat.ui.theme.OnlineGreen
import com.brokerbot.chat.ui.theme.SurfaceAlt
import com.brokerbot.chat.ui.theme.TextSecondary
import com.brokerbot.chat.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size, state.messages.lastOrNull()?.content) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding(),
    ) {
        CenterAlignedTopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(8.dp).clip(CircleShape).background(OnlineGreen),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        state.model.defaultModel,
                        fontWeight = FontWeight.SemiBold,
                        color = Accent,
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
            ),
        )

        if (state.isEmpty) {
            EmptyState(
                modifier = Modifier.weight(1f),
                onSuggestion = viewModel::onSuggestion,
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
            ) {
                items(state.messages, key = { it.id }) { MessageBubble(it) }
            }
        }

        InputBar(
            value = state.input,
            onValueChange = viewModel::onInputChanged,
            isStreaming = state.isStreaming,
            onSend = viewModel::send,
            onStop = viewModel::cancel,
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier, onSuggestion: (String) -> Unit) {
    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier.size(72.dp).clip(CircleShape).background(Accent),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
        }
        Spacer(Modifier.height(20.dp))
        Text("Ask anything", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Start a conversation, responses stream in real time.",
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(28.dp))
        DemoData.suggestions.forEach { s ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AccentSoft)
                    .clickable { onSuggestion(s) }
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                Text(s, color = Accent, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val isUser = message.role == Role.USER
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (isUser) Accent else AssistantBubble)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            if (message.content.isNotEmpty()) {
                Text(
                    text = message.content,
                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                )
            }
            if (message.isStreaming) {
                if (message.content.isNotEmpty()) Spacer(Modifier.height(6.dp))
                TypingIndicator()
            }
        }
    }
}

@Composable
private fun TypingIndicator() {
    val transition = rememberInfiniteTransition(label = "typing")
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(3) { i ->
            val alpha by transition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, delayMillis = i * 160),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "dot$i",
            )
            Box(
                Modifier.size(7.dp).alpha(alpha).clip(CircleShape).background(TextSecondary),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InputBar(
    value: String,
    onValueChange: (String) -> Unit,
    isStreaming: Boolean,
    onSend: () -> Unit,
    onStop: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f).clip(RoundedCornerShape(28.dp)),
            placeholder = { Text("Message") },
            enabled = !isStreaming,
            maxLines = 4,
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = SurfaceAlt,
                unfocusedContainerColor = SurfaceAlt,
                disabledContainerColor = SurfaceAlt,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            ),
        )
        Spacer(Modifier.width(10.dp))
        Surface(
            shape = if (isStreaming) RoundedCornerShape(14.dp) else CircleShape,
            color = Accent,
            modifier = Modifier.size(48.dp).clickable {
                if (isStreaming) onStop() else onSend()
            },
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isStreaming) {
                    Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color.White)
                } else {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                    )
                }
            }
        }
    }
}
