package com.brokerbot.chat.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val BrokerColors = lightColorScheme(
    primary = Accent,
    onPrimary = Surface,
    primaryContainer = AccentSoft,
    onPrimaryContainer = Accent,
    background = Surface,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = AssistantBubble,
    onSurfaceVariant = TextPrimary,
    outline = TextSecondary,
)

private val BrokerShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
)

@Composable
fun BrokerBotTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BrokerColors,
        shapes = BrokerShapes,
        content = content,
    )
}
