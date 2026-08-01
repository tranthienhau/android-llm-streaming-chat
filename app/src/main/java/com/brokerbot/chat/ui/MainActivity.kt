package com.brokerbot.chat.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.brokerbot.chat.data.DemoData
import com.brokerbot.chat.network.FakeStreamClient
import com.brokerbot.chat.service.SyncService
import com.brokerbot.chat.ui.theme.BrokerBotTheme
import com.brokerbot.chat.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {

    // Demo build uses the offline FakeStreamClient so the app runs with no endpoint.
    // Swap for SseStreamClient(endpoint = "...") to wire a real backend.
    private val viewModel: ChatViewModel by viewModels {
        viewModelFactory {
            initializer { ChatViewModel(FakeStreamClient()) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Optional deep-link into a tab / seeded state for demos and screenshots:
        // adb shell am start -n com.brokerbot.chat/.ui.MainActivity --es screen history
        // Start the foreground sync service so the live channel runs in the background.
        SyncService.start(this)

        val screen = intent.getStringExtra("screen")
        val startTab = when (screen) {
            "history" -> Tab.HISTORY
            "sync" -> Tab.SYNC
            "settings" -> Tab.SETTINGS
            else -> Tab.CHAT
        }
        if (screen == "chat") viewModel.seed(DemoData.streamingConversation)

        setContent {
            BrokerBotTheme {
                AppRoot(viewModel = viewModel, startTab = startTab)
            }
        }
    }
}
