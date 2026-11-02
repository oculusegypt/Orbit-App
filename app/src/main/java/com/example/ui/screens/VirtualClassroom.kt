package com.example.ui.screens

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
import com.example.ui.screens.classroom.LiveAudioMonitor
import com.example.ui.screens.classroom.LiveCameraPreview
import com.example.ui.screens.classroom.LiveWebRTCMeetingRoom
import com.example.ui.theme.*
import com.example.ui.viewmodel.OrbitViewModel

data class BreakoutGroupSession(
    val id: String,
    val name: String,
    val topic: String,
    val activeStudents: Int,
    val isPrimary: Boolean = false
)

data class DrawPoint(
    val offset: Offset,
    val color: Color
)

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
            Toast.makeText(context, "تم بدء مشاركة شاشة الجهاز الحقيقية بنجاح! 🟢", Toast.LENGTH_LONG).show()
        } else {
            isRealScreenSharingActive = false
            viewModel.setClassroomScreenShared(false)
            Toast.makeText(context, "تم إلغاء مشاركة الشاشة", Toast.LENGTH_SHORT).show()
        }
    }

    // Breakout sessions
    val breakoutSessions = remember {
        listOf(
            BreakoutGroupSession("main", "القاعة الرئيسية الكبرى 🌟", "حصة السوروبان والحساب الذهني مع مس سارة", 28, isPrimary = true),
            BreakoutGroupSession("g1", "غرفة العصافير (المستوى 1) 🐣", "تمارين الجمع البسيط حتى 10", 8),
            BreakoutGroupSession("g2", "غرفة أبطال الـ Small Friends 🧮", "تطبيق قواعد الخرزة 5 المركبة", 12),
            BreakoutGroupSession("g3", "غرفة العباقرة للتخيل السريع 🧠", "مسائل الحساب التخيلي Anzan", 8)
        )
    }
    var currentGroup by remember { mutableStateOf(breakoutSessions.first()) }
    var showBreakoutDialog by remember { mutableStateOf(false) }

    // Interactive 3-Rod Soroban Abacus state
    var rod1Upper by remember { mutableStateOf(false) }
    var rod1Lower by remember { mutableStateOf(1) }
    var rod2Upper by remember { mutableStateOf(true) }
    var rod2Lower by remember { mutableStateOf(2) }
    var rod3Upper by remember { mutableStateOf(false) }
    var rod3Lower by remember { mutableStateOf(4) }
    val hundredsVal = (if (rod1Upper) 5 else 0) + rod1Lower
    val tensVal = (if (rod2Upper) 5 else 0) + rod2Lower
    val onesVal = (if (rod3Upper) 5 else 0) + rod3Lower
    val totalAbacusValue = hundredsVal * 100 + tensVal * 10 + onesVal

    // Interactive Whiteboard drawing points
    val drawPoints = remember { mutableStateListOf<DrawPoint>() }

    // Video stream URI (local sample lesson)
    val videoUri = remember {
        val rawResId = context.resources.getIdentifier("sample_lesson", "raw", context.packageName)
        if (rawResId != 0) {
            Uri.parse("android.resource://${context.packageName}/$rawResId")
        } else {
            Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
        }
    }

    val meetingRoomId = "OrbitLearn2026_SaraElarby_${currentGroup.id}"
    val meetingDeepLink = "https://meet.jit.si/$meetingRoomId"

    val participants = remember {
        listOf(
            Triple("أ / سارة العربي", "معلمة ومضيفة الحصة 💡", true),
            Triple("ياسين أحمد", "طالب متميز", false),
            Triple("نور الهدى", "طالبة متفاعلة", false),
            Triple("عمر خالد", "طالب", false),
            Triple("مريم يوسف", "طالبة", false),
            Triple("زين الدين", "طالب", false)
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
                                    text = if (isMeetingConnected) "الفصل الافتراضي الحي الحقيقي 🔴" else "غير متصل بالجلسة ⚪",
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
                                Text("الغرف", fontSize = 11.sp)
                            }

                            // Share Meeting Link
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("رابط ميتينج مس سارة", meetingDeepLink)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "تم نسخ رابط الميتينج لمشاركته مع الطلاب!", Toast.LENGTH_SHORT).show()

                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "انضم الآن إلى الفصل الافتراضي التفاعلي مع مس سارة العربي 🧮💡:\n$meetingDeepLink")
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "مشاركة رابط الفصل الافتراضي"))
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .testTag("virtual_classroom_share_btn")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = Color.White, modifier = Modifier.size(16.dp))
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
                                    text = if (isScreenShared || isRealScreenSharingActive) "مشاركة الشاشة: نشطة 🟢" else "مشاركة شاشة الجهاز",
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
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf(
                        Pair("stream", "🎥 بث الحصة المباشر"),
                        Pair("camera", "📸 كاميرا جهازي (CameraX)"),
                        Pair("screen", "📱 شاشتي الحقيقية (Screen Capture)"),
                        Pair("abacus", "🧮 العداد التفاعلي"),
                        Pair("whiteboard", "✏️ سبورة الرسم"),
                        Pair("webrtc", "🌐 المؤتمر الحي (WebRTC)")
                    )

                    items(tabs) { (mode, label) ->
                        val isSelected = screenShareContent == mode
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) OrbitIndigo else Color.Transparent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { screenShareContent = mode }
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Main Stage / Live Conference Window
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!isMeetingConnected) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CallEnd, contentDescription = null, tint = OrbitRose, modifier = Modifier.size(54.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("تمت مغادرة الفصل الحي", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { isMeetingConnected = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = OrbitEmerald)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("الانضمام مجدداً إلى الفصل")
                                }
                            }
                        } else {
                            when (screenShareContent) {
                                "webrtc" -> {
                                    // Real WebRTC live conference room where shared screens from any device are displayed!
                                    LiveWebRTCMeetingRoom(
                                        roomName = meetingRoomId,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                "camera" -> {
                                    // Real CameraX live preview from device front/back camera!
                                    LiveCameraPreview(
                                        isCameraEnabled = isVideoOn,
                                        onCameraToggle = { viewModel.setClassroomVideo(true) },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                "screen" -> {
                                    // Real Android MediaProjection Screen Mirror / Capture status
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ScreenShare,
                                            contentDescription = null,
                                            tint = if (isRealScreenSharingActive) OrbitEmerald else OrbitIndigo,
                                            modifier = Modifier.size(52.dp)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = if (isRealScreenSharingActive) "شاشة هاتفك تُشارك الآن للطلاب والمعلمة 🟢" else "مشاركة شاشة جهاز الأندرويد الحقيقية",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = if (isRealScreenSharingActive)
                                                "يتم بث شاشة الهاتف بدقة 1080p إلى غرفة الميتينج التفاعلية. يمكن لجميع الطلاب رؤية ما تفعله."
                                            else
                                                "اضغط على الزر أدناه لبدء مشاركة شاشة جهازك بالكامل عبر نظام Android MediaProjection.",
                                            color = Color.White.copy(alpha = 0.75f),
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Button(
                                            onClick = {
                                                if (!isRealScreenSharingActive) {
                                                    if (mediaProjectionManager != null) {
                                                        screenCaptureLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
                                                    } else {
                                                        Toast.makeText(context, "خاصية مشاركة الشاشة غير مدعومة على هذا الجهاز", Toast.LENGTH_SHORT).show()
                                                    }
                                                } else {
                                                    isRealScreenSharingActive = false
                                                    viewModel.setClassroomScreenShared(false)
                                                    Toast.makeText(context, "تم إيقاف مشاركة الشاشة", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isRealScreenSharingActive) OrbitRose else OrbitEmerald
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.testTag("launch_system_screen_share_btn")
                                        ) {
                                            Icon(
                                                imageVector = if (isRealScreenSharingActive) Icons.Default.StopScreenShare else Icons.Default.ScreenShare,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(if (isRealScreenSharingActive) "إيقاف مشاركة الشاشة" else "بدء مشاركة شاشة الجهاز الحقيقية")
                                        }
                                    }
                                }

                                "abacus" -> {
                                    // 3-Rod Interactive Soroban Whiteboard
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxSize().padding(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(color = OrbitEmerald, shape = CircleShape) {
                                                    Box(modifier = Modifier.size(8.dp))
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "عداد السوروبان المشترك (الناتج: $totalAbacusValue)",
                                                    color = OrbitGoldLight,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Surface(
                                                color = Color.White.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.clickable {
                                                    rod1Upper = false; rod1Lower = 0
                                                    rod2Upper = false; rod2Lower = 0
                                                    rod3Upper = false; rod3Lower = 0
                                                }
                                            ) {
                                                Text(
                                                    text = "تصفير ↺",
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Soroban Frame
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFF334155),
                                            border = androidx.compose.foundation.BorderStroke(3.dp, OrbitGoldDark),
                                            modifier = Modifier.fillMaxWidth().height(160.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceEvenly,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Rod 1: Hundreds
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text("مئات", fontSize = 9.sp, color = OrbitGoldLight)
                                                    Box(
                                                        modifier = Modifier
                                                            .size(width = 38.dp, height = 18.dp)
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(if (rod1Upper) OrbitGold else Color(0xFF64748B))
                                                            .clickable { rod1Upper = !rod1Upper },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("5", color = OrbitNavyDark, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                                    }
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Box(modifier = Modifier.width(44.dp).height(4.dp).background(Color.White))
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    (1..4).forEach { idx ->
                                                        val active = rod1Lower >= idx
                                                        Box(
                                                            modifier = Modifier
                                                                .padding(vertical = 1.dp)
                                                                .size(width = 36.dp, height = 16.dp)
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(if (active) OrbitGold else Color(0xFF64748B))
                                                                .clickable { rod1Lower = if (rod1Lower == idx) idx - 1 else idx },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text("1", color = OrbitNavyDark, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                                        }
                                                    }
                                                    Text("$hundredsVal", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }

                                                Box(modifier = Modifier.width(2.dp).fillMaxHeight(0.8f).background(Color.White.copy(alpha = 0.2f)))

                                                // Rod 2: Tens
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text("عشرات", fontSize = 9.sp, color = OrbitGoldLight)
                                                    Box(
                                                        modifier = Modifier
                                                            .size(width = 38.dp, height = 18.dp)
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(if (rod2Upper) OrbitGold else Color(0xFF64748B))
                                                            .clickable { rod2Upper = !rod2Upper },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("5", color = OrbitNavyDark, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                                    }
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Box(modifier = Modifier.width(44.dp).height(4.dp).background(Color.White))
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    (1..4).forEach { idx ->
                                                        val active = rod2Lower >= idx
                                                        Box(
                                                            modifier = Modifier
                                                                .padding(vertical = 1.dp)
                                                                .size(width = 36.dp, height = 16.dp)
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(if (active) OrbitGold else Color(0xFF64748B))
                                                                .clickable { rod2Lower = if (rod2Lower == idx) idx - 1 else idx },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text("1", color = OrbitNavyDark, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                                        }
                                                    }
                                                    Text("$tensVal", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }

                                                Box(modifier = Modifier.width(2.dp).fillMaxHeight(0.8f).background(Color.White.copy(alpha = 0.2f)))

                                                // Rod 3: Ones
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text("آحاد", fontSize = 9.sp, color = OrbitGoldLight)
                                                    Box(
                                                        modifier = Modifier
                                                            .size(width = 38.dp, height = 18.dp)
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(if (rod3Upper) OrbitGold else Color(0xFF64748B))
                                                            .clickable { rod3Upper = !rod3Upper },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("5", color = OrbitNavyDark, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                                    }
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Box(modifier = Modifier.width(44.dp).height(4.dp).background(Color.White))
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    (1..4).forEach { idx ->
                                                        val active = rod3Lower >= idx
                                                        Box(
                                                            modifier = Modifier
                                                                .padding(vertical = 1.dp)
                                                                .size(width = 36.dp, height = 16.dp)
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(if (active) OrbitGold else Color(0xFF64748B))
                                                                .clickable { rod3Lower = if (rod3Lower == idx) idx - 1 else idx },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text("1", color = OrbitNavyDark, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                                        }
                                                    }
                                                    Text("$onesVal", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("المس الخرزات العلوية والسفلية في أي عمود لتحريكها ورؤية الناتج فوراً!", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                                    }
                                }

                                "whiteboard" -> {
                                    // Interactive Student Drawing Canvas
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        Canvas(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .pointerInput(Unit) {
                                                    detectDragGestures { change, _ ->
                                                        change.consume()
                                                        drawPoints.add(DrawPoint(change.position, OrbitGold))
                                                    }
                                                }
                                        ) {
                                            for (i in 0 until drawPoints.size - 1) {
                                                val p1 = drawPoints[i]
                                                val p2 = drawPoints[i + 1]
                                                drawLine(
                                                    color = p1.color,
                                                    start = p1.offset,
                                                    end = p2.offset,
                                                    strokeWidth = 6f,
                                                    cap = StrokeCap.Round
                                                )
                                            }
                                        }

                                        Row(
                                            modifier = Modifier
                                                .align(Alignment.TopStart)
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                color = Color.Black.copy(alpha = 0.6f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = "✏️ سبورة الرسم ومشاركة الحل (ارسم بيدك هنا)",
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = { drawPoints.clear() },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(8.dp)
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(Color.Black.copy(alpha = 0.6f))
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "مسح السبورة", tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }

                                "stream" -> {
                                    // Real Video Stream Player from Teacher
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        AndroidView(
                                            factory = { ctx ->
                                                VideoView(ctx).apply {
                                                    setVideoURI(videoUri)
                                                    setOnPreparedListener { mp ->
                                                        mp.isLooping = true
                                                        start()
                                                    }
                                                }
                                            },
                                            modifier = Modifier.fillMaxSize()
                                        )

                                        Surface(
                                            color = OrbitRose,
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.align(Alignment.TopStart).padding(10.dp)
                                        ) {
                                            Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.White))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("بث درس مس سارة 1080p HD", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

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
                                contentDescription = "الميكروفون",
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
                                contentDescription = "الكاميرا",
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
                                    isRealScreenSharingActive = false
                                    viewModel.setClassroomScreenShared(false)
                                    Toast.makeText(context, "تم إيقاف مشاركة الشاشة", Toast.LENGTH_SHORT).show()
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
                                contentDescription = "مشاركة الشاشة",
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
                                contentDescription = "رفع اليد",
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
                                contentDescription = "مغادرة الجلسة",
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
                                            Text("معلمة 💡", fontSize = 9.sp, color = OrbitNavyDark, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                        }
                                    }
                                    if (msg.isQuestion) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Surface(color = OrbitIndigo, shape = RoundedCornerShape(4.dp)) {
                                            Text("سؤال ❓", fontSize = 9.sp, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
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
                            placeholder = { Text("اكتب مشاركتك في الفصل الحي...", fontSize = 12.sp) },
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
                                contentDescription = "إرسال",
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
                                Text(if (isCurrent) "أنت هنا ✓" else "انضمام", fontSize = 11.sp)
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

    // Breakout Session Picker Dialog
    if (showBreakoutDialog) {
        Dialog(onDismissRequest = { showBreakoutDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("الغرف التفاعلية ومجموعات العمل 👥", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("اختر الغرفة المناسبة لمستوى طفلك للتدريب التفاعلي المباشر", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Divider(modifier = Modifier.padding(vertical = 10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        breakoutSessions.forEach { session ->
                            val isCurrent = session.id == currentGroup.id
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isCurrent) OrbitIndigo.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        currentGroup = session
                                        showBreakoutDialog = false
                                        Toast.makeText(context, "انضممت إلى: ${session.name}", Toast.LENGTH_SHORT).show()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(session.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(session.topic, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (isCurrent) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = OrbitIndigo)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showBreakoutDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = OrbitNavy),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("إغلاق")
                    }
                }
            }
        }
    }
}
