package com.brokerbot.chat.model

import java.util.UUID

enum class Role { USER, ASSISTANT, SYSTEM }

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: Role,
    val content: String,
    val isStreaming: Boolean = false,
)
