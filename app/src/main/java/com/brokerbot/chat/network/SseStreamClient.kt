package com.brokerbot.chat.network

import com.brokerbot.chat.model.ChatMessage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources

/**
 * Server-Sent Events streaming client for OpenAI / Anthropic compatible
 * chat streaming endpoints. Built on OkHttp-sse - backpressure via
 * callbackFlow, cancellation propagates to the underlying EventSource.
 */
class SseStreamClient(
    private val endpoint: String = "https://api.example.com/v1/chat/stream",
    private val apiKey: String? = System.getenv("BROKERBOT_API_KEY"),
    private val okHttp: OkHttpClient = OkHttpClient.Builder().build(),
) : StreamClient {

    @Serializable
    private data class Msg(val role: String, val content: String)

    @Serializable
    private data class Payload(val messages: List<Msg>, val stream: Boolean = true)

    override fun stream(prompt: String, history: List<ChatMessage>): Flow<String> = callbackFlow {
        val messages = history.map { Msg(it.role.name.lowercase(), it.content) } +
            Msg("user", prompt)
        val body = Json.encodeToString(Payload.serializer(), Payload(messages))

        val reqBuilder = Request.Builder()
            .url(endpoint)
            .addHeader("Accept", "text/event-stream")
            .addHeader("Content-Type", "application/json")
            .post(body.toRequestBody("application/json".toMediaType()))
        apiKey?.let { reqBuilder.addHeader("Authorization", "Bearer $it") }

        val listener = object : EventSourceListener() {
            override fun onEvent(source: EventSource, id: String?, type: String?, data: String) {
                if (data == "[DONE]") { close(); return }
                decodeToken(data)?.let { trySend(it) }
            }
            override fun onFailure(source: EventSource, t: Throwable?, response: Response?) {
                close(t ?: RuntimeException("SSE failed: ${response?.code}"))
            }
            override fun onClosed(source: EventSource) { close() }
        }

        val source = EventSources.createFactory(okHttp).newEventSource(reqBuilder.build(), listener)
        awaitClose { source.cancel() }
    }

    /** Handles both Anthropic `content_block_delta` and OpenAI `choices[0].delta.content`. */
    private fun decodeToken(raw: String): String? = runCatching {
        val root = Json.parseToJsonElement(raw).jsonObject
        if (root["type"]?.jsonPrimitive?.content == "content_block_delta") {
            return root["delta"]?.jsonObject?.get("text")?.jsonPrimitive?.content
        }
        (root["choices"] as? JsonArray)
            ?.firstOrNull()?.jsonObject
            ?.get("delta")?.jsonObject
            ?.get("content")?.jsonPrimitive?.content
    }.getOrNull()
}
