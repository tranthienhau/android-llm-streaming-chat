package com.brokerbot.chat.network

import com.brokerbot.chat.model.ChatMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Offline stand-in for [SseStreamClient] so the app is demoable on a simulator
 * with no endpoint or API key. Emits a canned reply word-by-word with a small
 * delay to reproduce the token streaming + typing indicator behavior.
 */
class FakeStreamClient(private val perTokenMs: Long = 45) : StreamClient {

    override fun stream(prompt: String, history: List<ChatMessage>): Flow<String> = flow {
        val reply = replyFor(prompt)
        for (word in reply.split(" ")) {
            emit("$word ")
            delay(perTokenMs)
        }
    }

    private fun replyFor(prompt: String): String {
        val p = prompt.lowercase()
        return when {
            "sse" in p || "stream" in p -> SSE_REPLY
            "kotlin" in p || "code" in p -> KOTLIN_REPLY
            else -> DEFAULT_REPLY
        }
    }

    private companion object {
        const val SSE_REPLY =
            "SSE is often preferred for streaming LLM responses because it is " +
                "unidirectional, the server pushes exactly what is needed for text " +
                "generation. Key advantages: 1. Lightweight, plain HTTP, no custom " +
                "protocol. 2. Auto-reconnect handled by the browser. 3. Firewall " +
                "friendly over standard ports. I parse each data: frame and emit the " +
                "delta token into a Kotlin Flow."
        const val KOTLIN_REPLY =
            "Here is the idea: wrap the EventSource in a callbackFlow, trySend each " +
                "decoded token, and awaitClose { source.cancel() } so cancelling the " +
                "coroutine tears down the network call. The ViewModel collects it and " +
                "appends tokens to the streaming message."
        const val DEFAULT_REPLY =
            "Happy to help. I stream responses token by token over SSE, so you see " +
                "the answer build up live. Ask me about the architecture, the Flow " +
                "pipeline, or how cancellation propagates to the socket."
    }
}
