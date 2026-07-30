package com.brokerbot.chat.data

import com.brokerbot.chat.model.ChatMessage
import com.brokerbot.chat.model.Conversation
import com.brokerbot.chat.model.ModelProvider
import com.brokerbot.chat.model.Role

/** Mockup content used to make every screen demoable without a backend. */
object DemoData {

    val conversations = listOf(
        Conversation("1", "Kotlin coroutines help",
            "Best way to handle structured concurrency in my Android app is to scope jobs to the ViewModel.",
            "2h ago", ModelProvider.OPENAI),
        Conversation("2", "Trip to Kyoto",
            "For your 5-day itinerary I recommend visiting the Fushimi Inari shrine early in the morning.",
            "Yesterday", ModelProvider.ANTHROPIC),
        Conversation("3", "Summarize research paper",
            "The primary finding of the paper indicates a significant correlation between the two variables.",
            "2 days ago", ModelProvider.OPENAI),
        Conversation("4", "Workout routine plan",
            "Based on your preference for high-intensity training, I drafted a 4-day split for you.",
            "3 days ago", ModelProvider.ANTHROPIC),
        Conversation("5", "Recipe for mushroom risotto",
            "The secret to a creamy risotto is adding the warm broth one ladle at a time, stirring often.",
            "Mon", ModelProvider.OPENAI),
        Conversation("6", "Explain quantum entanglement",
            "Think of entanglement as two magic coins: when you flip one and get heads, the other is tails.",
            "Last week", ModelProvider.ANTHROPIC),
    )

    /** A partly-streamed conversation used for the chat screenshot. */
    val streamingConversation = listOf(
        ChatMessage(role = Role.USER, content = "What are the main benefits of SSE compared to WebSockets?"),
        ChatMessage(
            role = Role.ASSISTANT,
            content = "SSE is often preferred for streaming LLM responses because it is " +
                "unidirectional, the server pushes exactly what is needed for text generation.\n\n" +
                "Key advantages:\n1. Lightweight, uses plain HTTP.\n2. Auto-reconnect handled natively.\n" +
                "3. Firewall friendly over standard ports. Currently I am using an EventSource",
            isStreaming = true,
        ),
    )

    val suggestions = listOf(
        "Summarize an article",
        "Write Kotlin code",
        "Explain SSE streaming",
    )
}
