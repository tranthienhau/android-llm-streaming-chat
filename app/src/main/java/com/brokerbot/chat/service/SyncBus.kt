package com.brokerbot.chat.service

import com.brokerbot.chat.model.SyncState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Process-wide bridge between [SyncService] (which owns the socket + timer in the
 * background) and the UI. The service publishes; the ViewModel observes. Kept as a
 * singleton so the sync channel outlives any single screen or Activity.
 */
object SyncBus {
    private val _state = MutableStateFlow(SyncState())
    val state: StateFlow<SyncState> = _state.asStateFlow()

    fun publish(state: SyncState) { _state.value = state }
}
