package com.example.ui.viewmodel

import com.example.data.local.CallSessionEntity
import com.example.data.local.CallStatus
import com.example.data.local.CallType
import com.example.data.local.ScreenShareContent
import com.example.data.local.UserAccount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Owns the 1-on-1 private call state machine (ringing, connected, in-call
 * controls) and delegates call-session persistence through [onCallRecorded].
 * Extracted from [OrbitViewModel] to isolate telephony-style logic.
 */
class PrivateCallController(
    private val scope: CoroutineScope,
    private val currentUserProvider: () -> UserAccount?,
    private val fallbackUserProvider: () -> UserAccount?,
    private val onCallRecorded: suspend (CallSessionEntity) -> Unit
) {

    private var durationJob: Job? = null

    private val _activeCall = MutableStateFlow<ActiveCallState?>(null)
    val activeCall: StateFlow<ActiveCallState?> = _activeCall.asStateFlow()

    fun startOutgoingCall(targetUser: UserAccount, callType: CallType) {
        val caller = currentUserProvider() ?: fallbackUserProvider() ?: return
        val newCallId = generateCallId()

        _activeCall.value = ActiveCallState(
            callId = newCallId,
            otherUser = targetUser,
            isOutgoing = true,
            callType = callType,
            status = CallStatus.OUTGOING_RINGING,
            isScreenSharing = (callType == CallType.VIDEO_SCREEN)
        )

        // Realistic outgoing ring delay, then connect
        scope.launch {
            delay(RING_DURATION_MS)
            val call = _activeCall.value
            if (call?.callId == newCallId && call.status == CallStatus.OUTGOING_RINGING) {
                _activeCall.value = call.copy(status = CallStatus.CONNECTED)
                startDurationTimer()
            }
        }
    }

    fun simulateIncomingCall(fromUser: UserAccount, callType: CallType) {
        _activeCall.value = ActiveCallState(
            callId = generateCallId(),
            otherUser = fromUser,
            isOutgoing = false,
            callType = callType,
            status = CallStatus.INCOMING_RINGING,
            isScreenSharing = (callType == CallType.VIDEO_SCREEN)
        )
    }

    fun accept() {
        val current = _activeCall.value ?: return
        _activeCall.value = current.copy(status = CallStatus.CONNECTED)
        startDurationTimer()
    }

    fun reject() {
        val current = _activeCall.value ?: return
        val me = currentUserProvider()
        scope.launch {
            onCallRecorded(
                buildSession(
                    call = current,
                    caller = current.otherUser,
                    receiver = me,
                    durationSeconds = 0,
                    hadScreenShare = false,
                    status = "REJECTED"
                )
            )
        }
        closeCall(afterClearDelayMs = REJECT_CLEAR_DELAY_MS)
    }

    fun end() {
        durationJob?.cancel()
        val current = _activeCall.value ?: return
        val me = currentUserProvider()
        val (caller, receiver) =
            if (current.isOutgoing) (me to current.otherUser) else (current.otherUser to me)

        scope.launch {
            onCallRecorded(
                buildSession(
                    call = current,
                    caller = caller,
                    receiver = receiver,
                    durationSeconds = current.durationSeconds,
                    hadScreenShare = current.isScreenSharing,
                    status = "COMPLETED"
                )
            )
        }
        closeCall(afterClearDelayMs = END_CLEAR_DELAY_MS)
    }

    // ---- In-call controls -------------------------------------------------

    fun toggleMute() = updateCall { it.copy(isMuted = !it.isMuted) }
    fun toggleCamera() = updateCall { it.copy(isCameraOff = !it.isCameraOff) }
    fun flipCamera() = updateCall { it.copy(isFrontCamera = !it.isFrontCamera) }
    fun toggleSpeaker() = updateCall { it.copy(isSpeakerOn = !it.isSpeakerOn) }
    fun toggleScreenShare() = updateCall { it.copy(isScreenSharing = !it.isScreenSharing) }

    fun setScreenShareContent(content: ScreenShareContent) =
        updateCall { it.copy(screenShareContent = content, isScreenSharing = true) }

    fun toggleSorobanUpperBead() =
        updateCall { it.copy(sorobanUpperBeadActive = !it.sorobanUpperBeadActive) }

    fun setSorobanLowerBeads(count: Int) =
        updateCall { it.copy(sorobanLowerBeadsCount = count.coerceIn(0, MAX_LOWER_BEADS)) }

    // ---- Internals --------------------------------------------------------

    private inline fun updateCall(transform: (ActiveCallState) -> ActiveCallState) {
        val call = _activeCall.value ?: return
        _activeCall.value = transform(call)
    }

    private fun closeCall(afterClearDelayMs: Long) {
        val current = _activeCall.value ?: return
        _activeCall.value = current.copy(status = CallStatus.ENDED)
        scope.launch {
            delay(afterClearDelayMs)
            _activeCall.value = null
        }
    }

    private fun startDurationTimer() {
        durationJob?.cancel()
        durationJob = scope.launch {
            while (true) {
                delay(TICK_MS)
                val call = _activeCall.value
                if (call != null && call.status == CallStatus.CONNECTED) {
                    _activeCall.value = call.copy(durationSeconds = call.durationSeconds + 1)
                } else {
                    break
                }
            }
        }
    }

    private fun buildSession(
        call: ActiveCallState,
        caller: UserAccount?,
        receiver: UserAccount?,
        durationSeconds: Int,
        hadScreenShare: Boolean,
        status: String
    ) = CallSessionEntity(
        callId = call.callId,
        callerId = caller?.id ?: UNKNOWN_ID,
        callerName = caller?.displayName ?: FALLBACK_CALLER_NAME,
        callerRole = caller?.role?.name ?: DEFAULT_ROLE,
        callerAvatar = caller?.avatarUrl ?: "",
        receiverId = receiver?.id ?: UNKNOWN_ID,
        receiverName = receiver?.displayName ?: FALLBACK_RECEIVER_NAME,
        receiverRole = receiver?.role?.name ?: DEFAULT_ROLE,
        receiverAvatar = receiver?.avatarUrl ?: "",
        callType = call.callType.name,
        durationSeconds = durationSeconds,
        hadScreenShare = hadScreenShare,
        status = status
    )

    private fun generateCallId(): String = "call_" + UUID.randomUUID().toString().take(8)

    private companion object {
        const val RING_DURATION_MS = 2200L
        const val REJECT_CLEAR_DELAY_MS = 900L
        const val END_CLEAR_DELAY_MS = 1200L
        const val TICK_MS = 1000L
        const val MAX_LOWER_BEADS = 4
        const val UNKNOWN_ID = "current"
        const val DEFAULT_ROLE = "LEARNER"
        const val FALLBACK_CALLER_NAME = "أنا"
        const val FALLBACK_RECEIVER_NAME = "المستخدم"
    }
}
