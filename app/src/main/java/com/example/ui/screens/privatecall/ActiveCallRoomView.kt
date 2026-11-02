package com.example.ui.screens.privatecall

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

