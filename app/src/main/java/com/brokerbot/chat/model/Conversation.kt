package com.brokerbot.chat.model

/** A past conversation shown in the History list. */
data class Conversation(
    val id: String,
    val title: String,
    val preview: String,
    val timeLabel: String,
    val model: ModelProvider,
)

enum class ModelProvider(val label: String, val defaultModel: String) {
    OPENAI("OpenAI", "GPT-4o"),
    ANTHROPIC("Anthropic", "Claude Sonnet"),
}
