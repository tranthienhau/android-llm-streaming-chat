package com.brokerbot.chat.network

import com.brokerbot.chat.model.ChatMessage
import kotlinx.coroutines.flow.Flow

/** Streams an assistant reply token-by-token for a prompt + prior history. */
interface StreamClient {
    fun stream(prompt: String, history: List<ChatMessage>): Flow<String>
}
