package com.brokerbot.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brokerbot.chat.model.ChatMessage
import com.brokerbot.chat.model.ModelProvider
import com.brokerbot.chat.model.Role
import com.brokerbot.chat.network.StreamClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    val isStreaming: Boolean = false,
    val model: ModelProvider = ModelProvider.ANTHROPIC,
    val error: String? = null,
) {
    val isEmpty: Boolean get() = messages.isEmpty()
}

class ChatViewModel(
    private val client: StreamClient,
) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    private var streamJob: Job? = null

    /** Preload a conversation - used for demos and screenshots. */
    fun seed(messages: List<ChatMessage>) {
        val streaming = messages.lastOrNull()?.isStreaming == true
        _state.update { it.copy(messages = messages, isStreaming = streaming) }
    }

    fun onInputChanged(value: String) {
        _state.update { it.copy(input = value) }
    }

    fun onSuggestion(text: String) {
        _state.update { it.copy(input = text) }
        send()
    }

    fun send() {
        val trimmed = _state.value.input.trim()
        if (trimmed.isEmpty() || _state.value.isStreaming) return

        val userMsg = ChatMessage(role = Role.USER, content = trimmed)
        val assistantMsg = ChatMessage(role = Role.ASSISTANT, content = "", isStreaming = true)
        val history = _state.value.messages

        _state.update {
            it.copy(
                messages = it.messages + userMsg + assistantMsg,
                input = "",
                isStreaming = true,
                error = null,
            )
        }

        streamJob = viewModelScope.launch {
            client.stream(trimmed, history)
                .catch { t ->
                    _state.update { s ->
                        s.copy(
                            isStreaming = false,
                            error = t.message ?: "Stream failed",
                            messages = s.messages.dropLastWhile { m -> m.id == assistantMsg.id && m.content.isEmpty() },
                        )
                    }
                }
                .collect { token ->
                    _state.update { s ->
                        s.copy(
                            messages = s.messages.map {
                                if (it.id == assistantMsg.id) it.copy(content = it.content + token) else it
                            },
                        )
                    }
                }
            _state.update { s ->
                s.copy(
                    isStreaming = false,
                    messages = s.messages.map {
                        if (it.id == assistantMsg.id) it.copy(isStreaming = false) else it
                    },
                )
            }
        }
    }

    fun cancel() {
        streamJob?.cancel()
        streamJob = null
        _state.update { s ->
            s.copy(
                isStreaming = false,
                messages = s.messages.map { it.copy(isStreaming = false) },
            )
        }
    }
}
