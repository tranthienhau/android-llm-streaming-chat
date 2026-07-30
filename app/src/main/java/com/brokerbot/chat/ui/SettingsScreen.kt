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
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brokerbot.chat.model.ModelProvider
import com.brokerbot.chat.ui.theme.Accent
import com.brokerbot.chat.ui.theme.SurfaceAlt
import com.brokerbot.chat.ui.theme.TextSecondary

@Composable
fun SettingsScreen() {
    var provider by remember { mutableStateOf(ModelProvider.ANTHROPIC) }
    var darkMode by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Text("Settings", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))

        SectionLabel("CONNECTION")
        Card {
            FieldRow("API Endpoint", "https://api.anthropic.com/v1")
            Divider()
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("API Key", color = TextSecondary, fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("••••••••••••••")
                }
                Icon(Icons.Default.Visibility, contentDescription = "Show key", tint = TextSecondary)
            }
        }

        Spacer(Modifier.height(20.dp))
        SectionLabel("MODEL")
        Card {
            Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ModelProvider.entries.forEach { p ->
                    Segment(p.label, selected = p == provider, modifier = Modifier.weight(1f)) {
                        provider = p
                    }
                }
            }
            Divider()
            Row(Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Selected Model", modifier = Modifier.weight(1f))
                Text(provider.defaultModel, color = Accent, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(Modifier.height(20.dp))
        SectionLabel("APPEARANCE")
        Card {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.DarkMode, contentDescription = null, tint = TextSecondary)
                Spacer(Modifier.width(12.dp))
                Text("Dark Mode", modifier = Modifier.weight(1f))
                Switch(
                    checked = darkMode,
                    onCheckedChange = { darkMode = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = Accent),
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        SectionLabel("ABOUT")
        Card {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = TextSecondary)
                Spacer(Modifier.width(12.dp))
                Text("Version", modifier = Modifier.weight(1f))
                Text("v1.0.0", color = TextSecondary)
            }
        }

        Spacer(Modifier.height(28.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Accent)
                .clickable {}
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("Save Changes", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun Card(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(SurfaceAlt),
        content = content,
    )
}

@Composable
private fun FieldRow(label: String, value: String) {
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Text(label, color = TextSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        Text(value)
    }
}

@Composable
private fun Divider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE9EBF2)))
}

@Composable
private fun Segment(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Accent else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (selected) Color.White else TextSecondary,
            fontWeight = FontWeight.Medium,
        )
    }
}
