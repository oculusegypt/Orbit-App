package com.example.services

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Process-local reactive bus that bridges Firebase Messaging callbacks
 * (which run outside any ViewModel) into the UI layer's state machine.
 *
 * This replaces the old "simulate incoming call" button: an incoming-call
 * flow item is produced ONLY by a real FCM data message (type = "call"),
 * and [com.example.ui.viewmodel.OrbitViewModel] collects it to drive the
 * genuine ringing state in PrivateCallController.
 */
object OrbitInAppSignalBus {

    data class IncomingCallSignal(val fromUserId: String, val callTypeName: String)

    private val _incomingCalls = MutableSharedFlow<IncomingCallSignal>(
        replay = 1,
        extraBufferCapacity = 4
    )
    val incomingCalls: SharedFlow<IncomingCallSignal> = _incomingCalls.asSharedFlow()

    fun emitIncomingCall(fromUserId: String, callTypeName: String) {
        if (fromUserId.isBlank()) return
        _incomingCalls.tryEmit(IncomingCallSignal(fromUserId, callTypeName))
    }
}
