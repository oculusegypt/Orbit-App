package com.example.ui.screens

import androidx.compose.ui.res.stringResource
import com.example.R

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.models.ClassroomMessage
import com.example.data.repository.OrbitRepository
import com.example.ui.screens.classroom.BreakoutGroupSession
import com.example.ui.screens.classroom.BreakoutRoomPickerDialog
import com.example.ui.screens.classroom.ClassroomStageTabs
import com.example.ui.screens.classroom.ClassroomStageWindow
import com.example.ui.screens.classroom.LiveAudioMonitor
import com.example.ui.screens.classroom.setCurrentMeetingRoomName
import com.example.ui.theme.*
import com.example.ui.viewmodel.OrbitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VirtualClassroom(
    viewModel: OrbitViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isMuted by viewModel.classroomMuted.collectAsState()
    val isVideoOn by viewModel.classroomVideoOn.collectAsState()
    val isHandRaised by viewModel.classroomHandRaised.collectAsState()
    val isScreenShared by viewModel.classroomScreenShared.collectAsState()
    val chatInput by viewModel.classroomChatText.collectAsState()
    val messages by viewModel.classroomMessages.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var isMeetingConnected by remember { mutableStateOf(true) }
    // Stage view modes: "stream" | "camera" | "screen" | "abacus" | "whiteboard" | "webrtc"
    var screenShareContent by remember { mutableStateOf("stream") }
    var activeBottomTab by remember { mutableStateOf("chat") } // "chat" | "participants" | "breakout"
    var askAsQuestion by remember { mutableStateOf(false) }

    // Real Android MediaProjection Screen Sharing Launcher
    var isRealScreenSharingActive by remember { mutableStateOf(false) }
    val mediaProjectionManager = remember {
        context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
    }

    val screenCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            isRealScreenSharingActive = true
            viewModel.setClassroomScreenShared(true)
            screenShareContent = "screen"
            Toast.makeText(context, stringResource(R.string.ui_f0e94e0), Toast.LENGTH_LONG).show()
        } else {
            isRealScreenSharingActive = false
            viewModel.setClassroomScreenShared(false)
            Toast.makeText(context, stringResource(R.string.ui_5ec193b), Toast.LENGTH_SHORT).show()
        }
    }

    // Shared handler: launches the real Android MediaProjection system dialog
    // or stops an active share. Used by the stage panel and the control bar.
    val toggleRealScreenShare: () -> Unit = {
        if (!isRealScreenSharingActive) {
            if (mediaProjectionManager != null) {
                screenCaptureLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
            } else {
                Toast.makeText(context, stringResource(R.string.ui_52fe2ee), Toast.LENGTH_SHORT).show()
            }
        } else {
            isRealScreenSharingActive = false
            viewModel.setClassroomScreenShared(false)
            Toast.makeText(context, stringResource(R.string.ui_c5dc7f0), Toast.LENGTH_SHORT).show()
        }
    }

    // Keep the extracted stage window in sync with the currently joined breakout room
    LaunchedEffect(currentGroup.id) {
        setCurrentMeetingRoomName("OrbitLearn2026_SaraElarby_${currentGroup.id}")
    }

    // Breakout sessions
    val breakoutSessions = remember {
        listOf(
            BreakoutGroupSession("main", stringResource(R.string.ui_01956d5), stringResource(R.string.ui_7e160b7), 28, isPrimary = true),
            BreakoutGroupSession("g1", stringResource(R.string.ui_c32355b), stringResource(R.string.ui_970f743), 8),
            BreakoutGroupSession("g2", stringResource(R.string.ui_0cba708), stringResource(R.string.ui_e7c0c88), 12),
            BreakoutGroupSession("g3", stringResource(R.string.ui_451e7b1), stringResource(R.string.ui_227fc56), 8)
        )
    }
    var currentGroup by remember { mutableStateOf(breakoutSessions.first()) }
    var showBreakoutDialog by remember { mutableStateOf(false) }

    val meetingRoomId = "OrbitLearn2026_SaraElarby_${currentGroup.id}"
    val meetingDeepLink = "https://meet.jit.si/$meetingRoomId"

    val participants = remember {
        listOf(
            Triple(stringResource(R.string.ui_cec9cf0), stringResource(R.string.ui_ca65086), true),
            Triple(stringResource(R.string.ui_f685e01), stringResource(R.string.ui_50517af), false),
            Triple(stringResource(R.string.ui_8912465), stringResource(R.string.ui_1893f40), false),
            Triple(stringResource(R.string.ui_80680b8), stringResource(R.string.ui_79c983e), false),
            Triple(stringResource(R.string.ui_a1e2f3f), stringResource(R.string.ui_1518d4f), false),
            Triple(stringResource(R.string.ui_6b8e7a9), stringResource(R.string.ui_79c983e), false)
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("virtual_classroom_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Meeting Status & Header Banner
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = OrbitNavy,
                modifier = Modifier.fillMaxWidth().testTag("virtual_classroom_header")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = if (isMeetingConnected) OrbitEmerald else OrbitRose,
                                shape = CircleShape
                            ) {
                                Box(modifier = Modifier.size(10.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isMeetingConnected) stringResource(R.string.ui_27d3f21) else stringResource(R.string.ui_d14161f),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${currentGroup.name} • ${currentGroup.activeStudents} متصلاً",
                                    color = OrbitGoldLight,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Breakout Room Switcher Button
                            Button(
                                onClick = { showBreakoutDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = OrbitIndigo),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp).testTag("switch_breakout_session_btn")
                            ) {
                                Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.ui_c718afb), fontSize = 11.sp)
                            }

                            // Share Meeting Link
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText(stringResource(R.string.ui_6ebe7f1), meetingDeepLink)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, stringResource(R.string.ui_01d0d1d), Toast.LENGTH_SHORT).show()

                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "انضم الآن إلى الفصل الافتراضي التفاعلي مع مس سارة العربي 🧮💡:\n$meetingDeepLink")
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, stringResource(R.string.ui_a2bb682)))
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .testTag("virtual_classroom_share_btn")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = stringResource(R.string.ui_81a50c9), tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // Audio Monitor Status Bar
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LiveAudioMonitor(
                            isMuted = isMuted,
                            pauseRecording = (screenShareContent == "webrtc"),
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            color = if (isScreenShared || isRealScreenSharingActive) OrbitEmerald.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.clickable {
                                if (!isRealScreenSharingActive && mediaProjectionManager != null) {
                                    screenCaptureLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
                                } else {
                                    isRealScreenSharingActive = false
                                    viewModel.setClassroomScreenShared(false)
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ScreenShare,
                                    contentDescription = null,
                                    tint = if (isScreenShared || isRealScreenSharingActive) OrbitEmerald else Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isScreenShared || isRealScreenSharingActive) stringResource(R.string.ui_86f7282) else stringResource(R.string.ui_0c26748),
                                    fontSize = 10.sp,
                                    color = if (isScreenShared || isRealScreenSharingActive) OrbitEmeraldLight else Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Live Stage Mode Selector Tabs
        item {
            ClassroomStageTabs(
                selectedMode = screenShareContent,
                onModeSelected = { screenShareContent = it }
            )
        }

        // Main Stage / Live Conference Window (extracted to ClassroomStageWindow)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    ClassroomStageWindow(
                        stageMode = screenShareContent,
                        isMeetingConnected = isMeetingConnected,
                        onReconnect = { isMeetingConnected = true },
                        isVideoOn = isVideoOn,
                        onCameraToggle = { viewModel.setClassroomVideo(true) },
                        isRealScreenSharingActive = isRealScreenSharingActive,
                        onToggleRealScreenShare = toggleRealScreenShare,
                        modifier = Modifier.padding(bottom = 0.dp)
                    )


                    // Video Conference Controls Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mic Toggle (controls hardware mic)
                        IconButton(
                            onClick = { viewModel.toggleClassroomMute() },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isMuted) Color(0xFFEF4444) else Color(0xFF334155))
                                .testTag("virtual_classroom_mic_btn")
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = stringResource(R.string.ui_072b93d),
                                tint = Color.White
                            )
                        }

                        // Video / Camera Toggle (controls real CameraX)
                        IconButton(
                            onClick = {
                                viewModel.toggleClassroomVideo()
                                if (!isVideoOn) {
                                    screenShareContent = "camera"
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isVideoOn) OrbitEmerald else Color(0xFFEF4444))
                                .testTag("virtual_classroom_camera_btn")
                        ) {
                            Icon(
                                imageVector = if (isVideoOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
                                contentDescription = stringResource(R.string.ui_1e8ec14),
                                tint = Color.White
                            )
                        }

                        // Screen Share Launcher (triggers Android MediaProjection system dialog)
                        IconButton(
                            onClick = {
                                if (!isRealScreenSharingActive) {
                                    if (mediaProjectionManager != null) {
                                        screenCaptureLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
                                    } else {
                                        screenShareContent = "screen"
                                    }
                                } else {
                                    toggleRealScreenShare()
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isRealScreenSharingActive || isScreenShared) OrbitEmerald else Color(0xFF334155))
                                .testTag("virtual_classroom_screen_share_btn")
                        ) {
                            Icon(
                                imageVector = if (isRealScreenSharingActive) Icons.Default.StopScreenShare else Icons.Default.ScreenShare,
                                contentDescription = stringResource(R.string.ui_b0b5a2c),
                                tint = Color.White
                            )
                        }

                        // Raise Hand Toggle
                        IconButton(
                            onClick = { viewModel.toggleClassroomHandRaise() },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isHandRaised) OrbitGold else Color(0xFF334155))
                                .testTag("virtual_classroom_hand_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FrontHand,
                                contentDescription = stringResource(R.string.ui_95d6a9a),
                                tint = if (isHandRaised) OrbitNavyDark else Color.White
                            )
                        }

                        // Leave / End Meeting
                        IconButton(
                            onClick = { isMeetingConnected = !isMeetingConnected },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDC2626))
                                .testTag("virtual_classroom_leave_btn")
                        ) {
                            Icon(
                                imageVector = if (isMeetingConnected) Icons.Default.CallEnd else Icons.Default.Call,
                                contentDescription = stringResource(R.string.ui_8731974),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Sub Tabs: Live Chat vs Participants vs Breakout Groups
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = activeBottomTab == "chat",
                    onClick = { activeBottomTab = "chat" },
                    label = { Text("المحادثة والأسئلة (${messages.size})", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = OrbitIndigo, selectedLabelColor = Color.White),
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = activeBottomTab == "participants",
                    onClick = { activeBottomTab = "participants" },
                    label = { Text("المتصلون (${participants.size})", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = OrbitIndigo, selectedLabelColor = Color.White),
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = activeBottomTab == "breakout",
                    onClick = { activeBottomTab = "breakout" },
                    label = { Text("الغرف (${breakoutSessions.size})", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = OrbitIndigo, selectedLabelColor = Color.White),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        when (activeBottomTab) {
            "chat" -> {
                items(messages) { msg ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (msg.senderRole == "instructor") OrbitGold.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(msg.senderName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    if (msg.senderRole == "instructor") {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Surface(color = OrbitGold, shape = RoundedCornerShape(4.dp)) {
                                            Text(stringResource(R.string.ui_b5190a9), fontSize = 9.sp, color = OrbitNavyDark, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                        }
                                    }
                                    if (msg.isQuestion) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Surface(color = OrbitIndigo, shape = RoundedCornerShape(4.dp)) {
                                            Text(stringResource(R.string.ui_45c51e4), fontSize = 9.sp, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                        }
                                    }
                                }
                                Text(msg.timestamp, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(msg.text, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)

                            if (msg.isQuestion) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { viewModel.upvoteClassroomQuestion(msg.id) }
                                        .padding(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.ThumbUp, contentDescription = null, tint = OrbitIndigo, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("أؤيد هذا السؤال (${msg.upvotes})", fontSize = 11.sp, color = OrbitIndigo, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Chat Input Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = chatInput,
                            onValueChange = { viewModel.setClassroomChatText(it) },
                            placeholder = { Text(stringResource(R.string.ui_7cd9315), fontSize = 12.sp) },
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.weight(1f).testTag("virtual_classroom_chat_input"),
                            singleLine = true
                        )

                        IconButton(
                            onClick = { viewModel.sendClassroomMessage(isQuestion = askAsQuestion) },
                            enabled = chatInput.isNotBlank(),
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (chatInput.isNotBlank()) OrbitIndigo else MaterialTheme.colorScheme.surfaceVariant)
                                .testTag("virtual_classroom_send_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = stringResource(R.string.ui_6dbd36c),
                                tint = if (chatInput.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            "participants" -> {
                items(participants) { (name, role, isInstructor) ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isInstructor) {
                                    AsyncImage(
                                        model = OrbitRepository.INSTRUCTOR_SARA.avatarUrl,
                                        contentDescription = null,
                                        modifier = Modifier.size(36.dp).clip(CircleShape)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.size(36.dp).clip(CircleShape).background(OrbitIndigo.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = OrbitIndigo, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(role, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Mic, contentDescription = null, tint = OrbitEmerald, modifier = Modifier.size(18.dp))
                                Icon(Icons.Default.Videocam, contentDescription = null, tint = OrbitIndigo, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            "breakout" -> {
                items(breakoutSessions) { session ->
                    val isCurrent = session.id == currentGroup.id
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrent) OrbitIndigo.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isCurrent) androidx.compose.foundation.BorderStroke(2.dp, OrbitIndigo) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                currentGroup = session
                                Toast.makeText(context, "انتقلت إلى: ${session.name}", Toast.LENGTH_SHORT).show()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(session.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(session.topic, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("عدد الطلاب الآن: ${session.activeStudents}", fontSize = 10.sp, color = OrbitIndigo)
                            }

                            Button(
                                onClick = {
                                    currentGroup = session
                                    Toast.makeText(context, "انتقلت إلى: ${session.name}", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isCurrent) OrbitEmerald else OrbitNavy
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (isCurrent) stringResource(R.string.ui_dffc622) else stringResource(R.string.ui_c060541), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Breakout Session Picker Dialog (extracted to classroom/BreakoutRoomPickerDialog.kt)
    if (showBreakoutDialog) {
        BreakoutRoomPickerDialog(
            sessions = breakoutSessions,
            currentGroup = currentGroup,
            onJoin = { session ->
                currentGroup = session
                Toast.makeText(context, "انضممت إلى: ${session.name}", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showBreakoutDialog = false }
        )
    }
}
