package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.CallStatus
import com.example.data.local.CallType
import com.example.data.local.ScreenShareContent
import com.example.data.models.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.ActiveCallState
import com.example.ui.viewmodel.OrbitViewModel
import com.example.ui.screens.privatecall.*

@Composable
fun PrivateCallScreen(viewModel: OrbitViewModel) {
    val callState by viewModel.activeCall.collectAsState()
    val state = callState ?: return

    when (state.status) {
        CallStatus.OUTGOING_RINGING -> {
            OutgoingCallView(state = state, onCancel = { viewModel.endCall() })
        }
        CallStatus.INCOMING_RINGING -> {
            IncomingCallView(
                state = state,
                onAccept = { viewModel.acceptCall() },
                onDecline = { viewModel.rejectCall() }
            )
        }
        CallStatus.CONNECTED -> {
            ActiveCallRoomView(state = state, viewModel = viewModel)
        }
        CallStatus.ENDED, CallStatus.IDLE -> {
            CallEndedView(state = state)
        }
    }
}

