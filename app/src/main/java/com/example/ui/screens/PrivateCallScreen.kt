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

@Composable
fun OutgoingCallView(state: ActiveCallState, onCancel: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E1B4B),
                        Color(0xFF0F172A)
                    )
                )
            )
            .padding(24.dp)
            .testTag("outgoing_call_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "جاري الاتصال المباشر الخاص...",
                color = OrbitGold,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (state.callType == CallType.VIDEO_SCREEN) "مكالمة فيديو ومشاركة شاشة 1:1" else "مكالمة صوتية خاصة 1:1",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Pulsing Avatar Ring
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(OrbitIndigo.copy(alpha = 0.2f))
                )
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .border(3.dp, OrbitGold, CircleShape)
                ) {
                    if (state.otherUser.avatarUrl.isNotBlank()) {
                        AsyncImage(
                            model = state.otherUser.avatarUrl,
                            contentDescription = state.otherUser.displayName,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().background(OrbitIndigoDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(60.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = state.otherUser.displayName,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = OrbitGold.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = state.otherUser.role.labelAr,
                    color = OrbitGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
            if (state.otherUser.bioOrGrade.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.otherUser.bioOrGrade,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            Spacer(modifier = Modifier.height(60.dp))

            // Cancel Call Button
            FloatingActionButton(
                onClick = onCancel,
                containerColor = Color(0xFFDC2626),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(64.dp)
                    .testTag("btn_cancel_outgoing_call")
            ) {
                Icon(Icons.Default.CallEnd, contentDescription = "إنهاء المكالمة", modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("إلغاء الاتصال", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        }
    }
}

@Composable
fun IncomingCallView(
    state: ActiveCallState,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_incoming")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "incomingPulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E293B),
                        Color(0xFF0A0F1D)
                    )
                )
            )
            .padding(24.dp)
            .testTag("incoming_call_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                color = OrbitEmerald.copy(alpha = 0.2f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OrbitEmerald)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(OrbitEmerald)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "اتصال مباشر خاص وارد الآن 📞",
                        color = OrbitEmerald,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Avatar Ring
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(OrbitEmerald.copy(alpha = 0.2f))
                )
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .border(3.dp, OrbitEmerald, CircleShape)
                ) {
                    if (state.otherUser.avatarUrl.isNotBlank()) {
                        AsyncImage(
                            model = state.otherUser.avatarUrl,
                            contentDescription = state.otherUser.displayName,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().background(OrbitIndigoDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = state.otherUser.displayName,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = state.otherUser.role.labelAr + " • " + (if (state.callType == CallType.VIDEO_SCREEN) "فيديو ومشاركة شاشة" else "صوت فقط"),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(56.dp))

            // Action Buttons: Accept / Decline
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
            ) {
                // Decline
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    FloatingActionButton(
                        onClick = onDecline,
                        containerColor = Color(0xFFDC2626),
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(64.dp).testTag("btn_decline_incoming_call")
                    ) {
                        Icon(Icons.Default.CallEnd, contentDescription = "رفض المكالمة", modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("رفض", color = Color.White, fontSize = 12.sp)
                }

                // Accept
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    FloatingActionButton(
                        onClick = onAccept,
                        containerColor = Color(0xFF16A34A),
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(64.dp).testTag("btn_accept_incoming_call")
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "قبول المكالمة", modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("قبول الاتصال", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveCallRoomView(state: ActiveCallState, viewModel: OrbitViewModel) {
    val durationFormatted = remember(state.durationSeconds) {
        val minutes = state.durationSeconds / 60
        val seconds = state.durationSeconds % 60
        String.format("%02d:%02d", minutes, seconds)
    }

    var showSharePicker by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .testTag("active_call_room")
    ) {
        // Main Content: Screen Share OR Video Stream
        if (state.isScreenSharing) {
            ScreenShareWorkspace(
                content = state.screenShareContent,
                otherUserName = state.otherUser.displayName,
                sorobanUpper = state.sorobanUpperBeadActive,
                sorobanLowerCount = state.sorobanLowerBeadsCount,
                onToggleUpper = { viewModel.toggleSorobanUpperBead() },
                onSetLowerCount = { viewModel.setSorobanLowerBeads(it) }
            )
        } else {
            // High Quality Video Stream View
            ParticipantVideoSurface(
                user = state.otherUser,
                isCameraOff = false,
                isMuted = state.isMuted
            )
        }

        // Floating Local PIP Camera Preview
        if (!state.isCameraOff) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 70.dp, end = 16.dp)
                    .size(width = 110.dp, height = 150.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, OrbitGold, RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E293B))
                    .clickable { viewModel.flipCamera() }
                    .testTag("local_camera_pip")
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (state.isFrontCamera) Icons.Default.CameraFront else Icons.Default.CameraRear,
                            contentDescription = "الكاميرا المحلية",
                            tint = OrbitGold,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (state.isFrontCamera) "كاميرا أمامية" else "كاميرا خلفية",
                            fontSize = 9.sp,
                            color = Color.White
                        )
                        Text(
                            text = "اضغط للتبديل",
                            fontSize = 8.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        // Top Status Bar Overlay
        Surface(
            color = Color.Black.copy(alpha = 0.65f),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(OrbitEmerald)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = state.otherUser.displayName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = OrbitGold.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = state.otherUser.role.labelAr,
                                    color = OrbitGold,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "🔒 اتصال مباشر مشفر 1:1 • $durationFormatted",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }

                // Screen Share Mode Button in Top Bar
                if (state.isScreenSharing) {
                    FilledTonalButton(
                        onClick = { showSharePicker = true },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = OrbitGold,
                            contentColor = OrbitNavyDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_change_share_mode")
                    ) {
                        Icon(Icons.Default.ScreenShare, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تغيير المحتوى", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Bottom Controls Dock
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF1E293B).copy(alpha = 0.95f),
            tonalElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth()
                .testTag("call_controls_dock")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Mic Mute/Unmute
                IconButton(
                    onClick = { viewModel.toggleMute() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (state.isMuted) Color(0xFFDC2626) else Color.White.copy(alpha = 0.12f))
                        .testTag("btn_toggle_mic")
                ) {
                    Icon(
                        imageVector = if (state.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "المايكروفون",
                        tint = Color.White
                    )
                }

                // 2. Camera Toggle
                IconButton(
                    onClick = { viewModel.toggleCamera() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (state.isCameraOff) Color(0xFFDC2626) else Color.White.copy(alpha = 0.12f))
                        .testTag("btn_toggle_camera")
                ) {
                    Icon(
                        imageVector = if (state.isCameraOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                        contentDescription = "الكاميرا",
                        tint = Color.White
                    )
                }

                // 3. Screen Sharing Toggle (HIGHLIGHTED)
                IconButton(
                    onClick = {
                        if (!state.isScreenSharing) {
                            showSharePicker = true
                        } else {
                            viewModel.toggleScreenShare()
                        }
                    },
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(if (state.isScreenSharing) OrbitGold else OrbitIndigo)
                        .testTag("btn_toggle_screen_share")
                ) {
                    Icon(
                        imageVector = if (state.isScreenSharing) Icons.Default.StopScreenShare else Icons.Default.ScreenShare,
                        contentDescription = "مشاركة الشاشة",
                        tint = if (state.isScreenSharing) OrbitNavyDark else Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // 4. Speaker Toggle
                IconButton(
                    onClick = { viewModel.toggleSpeaker() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (state.isSpeakerOn) OrbitEmerald.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.12f))
                        .testTag("btn_toggle_speaker")
                ) {
                    Icon(
                        imageVector = if (state.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                        contentDescription = "مكبر الصوت",
                        tint = if (state.isSpeakerOn) OrbitEmerald else Color.White
                    )
                }

                // 5. End Call Button (Red Pill)
                IconButton(
                    onClick = { viewModel.endCall() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDC2626))
                        .testTag("btn_end_call")
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "إنهاء المكالمة",
                        tint = Color.White
                    )
                }
            }
        }
    }

    // Modal Sheet for choosing what screen to share
    if (showSharePicker) {
        ModalBottomSheet(
            onDismissRequest = { showSharePicker = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("مشاركة الشاشة الحية 🖥️", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("اختر ما ترغب في مشاركته مع ${state.otherUser.displayName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { showSharePicker = false }) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                ScreenShareContent.values().forEach { option ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (state.screenShareContent == option && state.isScreenSharing) OrbitGold.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (state.screenShareContent == option && state.isScreenSharing) androidx.compose.foundation.BorderStroke(2.dp, OrbitGold) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                viewModel.setScreenShareContent(option)
                                if (!state.isScreenSharing) {
                                    viewModel.toggleScreenShare()
                                }
                                showSharePicker = false
                            }
                            .testTag("share_option_${option.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(OrbitIndigo.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (option) {
                                        ScreenShareContent.SOROBAN_BOARD -> Icons.Default.Calculate
                                        ScreenShareContent.MATH_WORKSHEET -> Icons.Default.Assignment
                                        ScreenShareContent.WHITEBOARD -> Icons.Default.Draw
                                        ScreenShareContent.DEVICE_SCREEN -> Icons.Default.Smartphone
                                    },
                                    contentDescription = null,
                                    tint = OrbitIndigo
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(option.labelAr, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    text = when (option) {
                                        ScreenShareContent.SOROBAN_BOARD -> "شرح عملي وحل المسائل بواسطة عداد السوربان الياباني التفاعلي"
                                        ScreenShareContent.MATH_WORKSHEET -> "مسائل حساب ذهني وتدريبات فورية مباشرة"
                                        ScreenShareContent.WHITEBOARD -> "لوحة بيضاء للكتابة ورسم العمليات والخطوات"
                                        ScreenShareContent.DEVICE_SCREEN -> "بث شاشة الهاتف المباشرة بجودة فائقة"
                                    },
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (state.screenShareContent == option && state.isScreenSharing) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = OrbitGold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ParticipantVideoSurface(user: com.example.data.local.UserAccount, isCameraOff: Boolean, isMuted: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)),
        contentAlignment = Alignment.Center
    ) {
        if (!isCameraOff && user.avatarUrl.isNotBlank()) {
            AsyncImage(
                model = user.avatarUrl,
                contentDescription = user.displayName,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(0.dp))
            )
            // Gradient Overlay for readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.5f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(OrbitIndigoDark)
                        .border(3.dp, OrbitGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(70.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(user.displayName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("الكاميرا مغلقة حالياً من قبل المشارك", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            }
        }

        // Live Speaking Wave Indicator
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 90.dp, start = 20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.GraphicEq,
                contentDescription = null,
                tint = if (isMuted) Color(0xFFEF4444) else OrbitEmerald,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isMuted) "الصوت مكتوم" else "يتحدث الآن...",
                color = Color.White,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun ScreenShareWorkspace(
    content: ScreenShareContent,
    otherUserName: String,
    sorobanUpper: Boolean,
    sorobanLowerCount: Int,
    onToggleUpper: () -> Unit,
    onSetLowerCount: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 64.dp, bottom = 90.dp)
            .background(Color(0xFF020617))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Live Screen Banner
            Surface(
                color = OrbitIndigoDark,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OrbitGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDC2626))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "🔴 مشاركة حية: ${content.labelAr}",
                            color = OrbitGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "مع $otherUserName (1080p 60fps)",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (content) {
                ScreenShareContent.SOROBAN_BOARD -> {
                    LiveSorobanShareCanvas(
                        upperActive = sorobanUpper,
                        lowerCount = sorobanLowerCount,
                        onToggleUpper = onToggleUpper,
                        onSetLowerCount = onSetLowerCount
                    )
                }
                ScreenShareContent.MATH_WORKSHEET -> {
                    LiveWorksheetShareCanvas()
                }
                ScreenShareContent.WHITEBOARD -> {
                    LiveWhiteboardCanvas()
                }
                ScreenShareContent.DEVICE_SCREEN -> {
                    LiveDeviceScreenMirror()
                }
            }
        }
    }
}

@Composable
fun LiveSorobanShareCanvas(
    upperActive: Boolean,
    lowerCount: Int,
    onToggleUpper: () -> Unit,
    onSetLowerCount: (Int) -> Unit
) {
    val totalValue = (if (upperActive) 5 else 0) + lowerCount

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, OrbitGold),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🧮 عداد السوربان التفاعلي المشترك",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = "اضغط على الخرزات لشرح كيفية تمثيل الأرقام وحل العمليات الحسابية مباشرة",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Value Display
            Surface(
                color = OrbitGold.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OrbitGold)
            ) {
                Text(
                    text = "القيمة الحالية على العمود = $totalValue",
                    color = OrbitGold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Interactive Column Abacus Frame
            Surface(
                color = Color(0xFF312E81),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(3.dp, Color(0xFFB45309)),
                modifier = Modifier
                    .width(180.dp)
                    .weight(1f)
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Upper Bead (Value = 5)
                    Text("خرزة السماء (5)", color = OrbitGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = onToggleUpper,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (upperActive) OrbitGold else Color(0xFF64748B)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_soroban_upper_bead")
                    ) {
                        Text(
                            text = if (upperActive) "خرزة 5 مفعّلة (نازلة للعارضة)" else "خرزة 5 مرفوعة (غير مفعّلة)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (upperActive) OrbitNavyDark else Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dividing Beam
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFFD97706))
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Lower Beads (Values = 1 each, 0 to 4)
                    Text("خرزات الأرض (1 كل خرزة)", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        (0..4).forEach { count ->
                            FilterChip(
                                selected = lowerCount == count,
                                onClick = { onSetLowerCount(count) },
                                label = { Text("$count", fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = OrbitEmerald,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Practice Example
                    Surface(
                        color = Color.Black.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = "💡 مثال للشرح: لجمع (4 + 3) نستخدم أصدقاء العشرة أو الخمسة: (+3 = +5 - 2).",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LiveWorksheetShareCanvas() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("📝 شيت التدريبات الحسابية المباشر", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("مسائل حساب ذهني مباشرة لحلها ومراجعتها أثناء المكالمة", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)

            Spacer(modifier = Modifier.height(16.dp))

            val exercises = listOf(
                Triple("12 + 25 - 6", "31", "تم الحل بنجاح ✅"),
                Triple("48 + 15 + 7", "70", "قيد الحل المباشر ⏳"),
                Triple("95 - 43 + 12", "64", "جاهزة للتطبيق 🎯")
            )

            exercises.forEach { (equation, answer, status) ->
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OrbitIndigo.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(equation, color = OrbitGold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("الناتج: $answer", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(status, color = OrbitEmerald, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveWhiteboardCanvas() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0F1D)),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, OrbitEmerald),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🎨 السبورة الذكية المشتركة", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("يمكنك تدوين الملاحظات التوضيحية وشرح الخطوات", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(16.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Draw, contentDescription = null, tint = OrbitEmerald, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("السبورة جاهزة للشرح التفاعلي باللمس والقلم", color = Color.White, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("مزامنة فورية لكل خطوة مع المشارك في المكالمة", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun LiveDeviceScreenMirror() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, OrbitIndigo),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Smartphone, contentDescription = null, tint = OrbitGold, modifier = Modifier.size(54.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text("بث شاشة الهاتف الحية قيد التشغيل", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text("يتم عرض شاشتك كاملة للطرف الآخر بأعلى معدل إطارات وبث مشفر", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun CallEndedView(state: ActiveCallState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDC2626)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CallEnd, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text("انتهت المكالمة", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "مدة الاتصال: ${state.durationSeconds / 60} دقيقة و ${state.durationSeconds % 60} ثانية",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("تم حفظ سجل المكالمة في قاعدة البيانات المحلية بنجاح ✅", color = OrbitEmerald, fontSize = 11.sp)
        }
    }
}
