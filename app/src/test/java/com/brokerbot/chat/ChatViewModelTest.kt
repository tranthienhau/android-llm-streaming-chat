package com.brokerbot.chat

import app.cash.turbine.test
import com.brokerbot.chat.model.ChatMessage
import com.brokerbot.chat.model.Role
import com.brokerbot.chat.network.StreamClient
import com.brokerbot.chat.viewmodel.ChatViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `tokens accumulate in assistant message`() = runTest {
        val stub = object : StreamClient {
            override fun stream(prompt: String, history: List<ChatMessage>): Flow<String> = flow {
                emit("Hel"); emit("lo"); emit(" world")
            }
        }
        val vm = ChatViewModel(stub)
        vm.onInputChanged("Hi")
        vm.send()

        vm.state.test {
            var last = awaitItem()
            while (last.isStreaming) last = awaitItem()
            val assistantText = last.messages.last { it.role == Role.ASSISTANT }.content
            assertEquals("Hello world", assistantText)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
