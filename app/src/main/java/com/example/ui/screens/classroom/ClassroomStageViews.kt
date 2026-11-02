package com.example.ui.screens.classroom

import android.content.Context
import android.net.Uri
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import android.app.Activity
import android.media.projection.MediaProjectionManager
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.example.ui.theme.OrbitEmerald
import com.example.ui.theme.OrbitGold
import com.example.ui.theme.OrbitGoldDark
import com.example.ui.theme.OrbitGoldLight
import com.example.ui.theme.OrbitIndigo
import com.example.ui.theme.OrbitNavyDark
import com.example.ui.theme.OrbitRose

/**
 * A single interactive point drawn on the live whiteboard canvas.
 */
data class DrawPoint(
    val offset: Offset,
    val color: Color
)

/**
 * The main "stage" window of the virtual classroom. Renders whichever live
 * mode is currently selected: direct video stream, real CameraX preview,
 * real MediaProjection screen-share status, the interactive soroban abacus,
 * the free-hand whiteboard, or the live WebRTC conference room.
 *
 * Extracted from VirtualClassroom to keep each file focused and reviewable.
 */
@Composable
fun ClassroomStageWindow(
    stageMode: String,
    isMeetingConnected: Boolean,
    onReconnect: () -> Unit,
    isVideoOn: Boolean,
    onCameraToggle: () -> Unit,
    isRealScreenSharingActive: Boolean,
    onToggleRealScreenShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
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
                    onClick = onReconnect,
                    colors = ButtonDefaults.buttonColors(containerColor = OrbitEmerald)
                ) {
                    Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("الانضمام مجدداً إلى الفصل")
                }
            }
        } else {
            when (stageMode) {
                "webrtc" -> LiveWebRTCMeetingRoom(
                    roomName = remember(stageMode) { currentRoomName() },
                    modifier = Modifier.fillMaxSize()
                )
                "camera" -> LiveCameraPreview(
                    isCameraEnabled = isVideoOn,
                    onCameraToggle = onCameraToggle,
                    modifier = Modifier.fillMaxSize()
                )
                "screen" -> RealScreenShareStatusPanel(
                    isRealScreenSharingActive = isRealScreenSharingActive,
                    onToggleRealScreenShare = onToggleRealScreenShare,
                    modifier = Modifier.fillMaxSize()
                )
                "abacus" -> InteractiveSorobanStage(modifier = Modifier.fillMaxSize())
                "whiteboard" -> InteractiveWhiteboardStage(modifier = Modifier.fillMaxSize())
                else -> TeacherLiveStreamStage(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

/** Room-name provider hook so the stage stays decoupled from parent state. */
private var roomNameHolder: String = "OrbitLearn2026_SaraElarby_main"

fun setCurrentMeetingRoomName(name: String) {
    roomNameHolder = name
}

private fun currentRoomName(): String = roomNameHolder

/**
 * Real Android MediaProjection screen-mirror status panel with system-dialog launcher.
 */
@Composable
fun RealScreenShareStatusPanel(
    isRealScreenSharingActive: Boolean,
    onToggleRealScreenShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
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
            onClick = onToggleRealScreenShare,
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

/**
 * Interactive 3-rod Soroban abacus (hundreds / tens / ones). All bead state is
 * owned locally by this composable so the parent screen no longer needs to
 * track six separate mutable states.
 */
@Composable
fun InteractiveSorobanStage(modifier: Modifier = Modifier) {
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

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(8.dp)
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

        // Soroban frame — one reusable Rod composable instead of three copy-pasted blocks
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF334155),
            border = BorderStroke(3.dp, OrbitGoldDark),
            modifier = Modifier.fillMaxWidth().height(160.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SorobanRod(label = "مئات", upperUp = rod1Upper, lowerUp = rod1Lower,
                    digitValue = hundredsVal,
                    onToggleUpper = { rod1Upper = !rod1Upper },
                    onSelectLower = { idx -> rod1Lower = if (rod1Lower == idx) idx - 1 else idx })

                RodDivider()

                SorobanRod(label = "عشرات", upperUp = rod2Upper, lowerUp = rod2Lower,
                    digitValue = tensVal,
                    onToggleUpper = { rod2Upper = !rod2Upper },
                    onSelectLower = { idx -> rod2Lower = if (rod2Lower == idx) idx - 1 else idx })

                RodDivider()

                SorobanRod(label = "آحاد", upperUp = rod3Upper, lowerUp = rod3Lower,
                    digitValue = onesVal,
                    onToggleUpper = { rod3Upper = !rod3Upper },
                    onSelectLower = { idx -> rod3Lower = if (rod3Lower == idx) idx - 1 else idx })
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text("المس الخرزات العلوية والسفلية في أي عمود لتحريكها ورؤية الناتج فوراً!", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
    }
}

@Composable
private fun RodDivider() {
    Box(modifier = Modifier.width(2.dp).fillMaxHeight(0.8f).background(Color.White.copy(alpha = 0.2f)))
}

/** One clickable rod column of the soroban (1 heaven bead + 4 earth beads). */
@Composable
private fun SorobanRod(
    label: String,
    upperUp: Boolean,
    lowerUp: Int,
    digitValue: Int,
    onToggleUpper: () -> Unit,
    onSelectLower: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 9.sp, color = OrbitGoldLight)
        Box(
            modifier = Modifier
                .size(width = 38.dp, height = 18.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (upperUp) OrbitGold else Color(0xFF64748B))
                .clickable(onClick = onToggleUpper),
            contentAlignment = Alignment.Center
        ) {
            Text("5", color = OrbitNavyDark, fontWeight = FontWeight.Bold, fontSize = 9.sp)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Box(modifier = Modifier.width(44.dp).height(4.dp).background(Color.White))
        Spacer(modifier = Modifier.height(2.dp))
        (1..4).forEach { idx ->
            val active = lowerUp >= idx
            Box(
                modifier = Modifier
                    .padding(vertical = 1.dp)
                    .size(width = 36.dp, height = 16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (active) OrbitGold else Color(0xFF64748B))
                    .clickable { onSelectLower(idx) },
                contentAlignment = Alignment.Center
            ) {
                Text("1", color = OrbitNavyDark, fontWeight = FontWeight.Bold, fontSize = 9.sp)
            }
        }
        Text("$digitValue", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

/**
 * Free-hand drawing whiteboard backed by a Compose Canvas. Stroke points are
 * kept in local mutable state owned by this composable.
 */
@Composable
fun InteractiveWhiteboardStage(modifier: Modifier = Modifier) {
    val drawPoints = remember { androidx.compose.runtime.mutableStateListOf<DrawPoint>() }

    Box(modifier = modifier.fillMaxSize()) {
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

/**
 * Real VideoView-backed teacher lesson stream (local raw sample when present,
 * remote MP4 otherwise).
 */
@Composable
fun TeacherLiveStreamStage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val videoUri: Uri = remember {
        val rawResId = context.resources.getIdentifier("sample_lesson", "raw", context.packageName)
        if (rawResId != 0) {
            Uri.parse("android.resource://${context.packageName}/$rawResId")
        } else {
            Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
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

/**
 * Stage-mode selector tabs extracted from VirtualClassroom.
 */
@Composable
fun ClassroomStageTabs(
    selectedMode: String,
    onModeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = remember {
        listOf(
            Pair("stream", "🎥 بث الحصة المباشر"),
            Pair("camera", "📸 كاميرا جهازي (CameraX)"),
            Pair("screen", "📱 شاشتي الحقيقية (Screen Capture)"),
            Pair("abacus", "🧮 العداد التفاعلي"),
            Pair("whiteboard", "✏️ سبورة الرسم"),
            Pair("webrtc", "🌐 المؤتمر الحي (WebRTC)")
        )
    }

    androidx.compose.material3.Card(
        shape = RoundedCornerShape(14.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth()
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(tabs) { (mode, label) ->
                val isSelected = selectedMode == mode
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) OrbitIndigo else Color.Transparent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onModeSelected(mode) }
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}
