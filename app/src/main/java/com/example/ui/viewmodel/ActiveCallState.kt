package com.example.ui.viewmodel

import com.example.data.local.CallStatus
import com.example.data.local.CallType
import com.example.data.local.ScreenShareContent
import com.example.data.local.UserAccount

data class ActiveCallState(
    val callId: String,
    val otherUser: UserAccount,
    val isOutgoing: Boolean,
    val callType: CallType,
    val status: CallStatus,
    val isMuted: Boolean = false,
    val isCameraOff: Boolean = false,
    val isFrontCamera: Boolean = true,
    val isSpeakerOn: Boolean = true,
    val isScreenSharing: Boolean = false,
    val screenShareContent: ScreenShareContent = ScreenShareContent.SOROBAN_BOARD,
    val durationSeconds: Int = 0,
    val sorobanUpperBeadActive: Boolean = false,
    val sorobanLowerBeadsCount: Int = 2
)
