package com.example.ui.screens

import androidx.compose.ui.res.stringResource

import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.data.models.Course
import com.example.data.models.Lesson
import com.example.data.models.LessonProgress
import com.example.ui.theme.*
import com.example.ui.viewmodel.OrbitViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonPlayerScreen(
    course: Course,
    lesson: Lesson,
    progressMap: Map<String, LessonProgress>,
    viewModel: OrbitViewModel,
    onBack: () -> Unit,
    onCompleteLesson: (String) -> Unit,
    onOpenQuiz: () -> Unit,
    onOpenTutor: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableStateOf(0) }
    var durationMs by remember { mutableStateOf(lesson.durationSeconds * 1000) }
    var playbackSpeed by remember { mutableStateOf(1.0f) }
    var isMuted by remember { mutableStateOf(false) }
    var activeTab by remember { mutableStateOf("notes") } // "notes" | "transcript" | "resources"
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }

    val isCompleted = progressMap[lesson.id]?.completed == true
    val progressPercent = if (durationMs > 0) ((currentPositionMs.toFloat() / durationMs.toFloat()) * 100).toInt().coerceIn(0, 100) else 0

    // Video resource URI: use local raw video sample if available, or online videoUrl
    val videoUri = remember(lesson.id) {
        val rawResId = context.resources.getIdentifier("sample_lesson", "raw", context.packageName)
        if (rawResId != 0) {
            Uri.parse("android.resource://${context.packageName}/$rawResId")
        } else if (lesson.videoUrl.isNotBlank()) {
            Uri.parse(lesson.videoUrl)
        } else {
            Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
        }
    }

    // Periodic progress tracker & Room Database caching
    LaunchedEffect(isPlaying, videoViewRef) {
        var tick = 0
        while (isActive) {
            videoViewRef?.let { vv ->
                if (vv.isPlaying) {
                    currentPositionMs = vv.currentPosition
                    if (vv.duration > 0) durationMs = vv.duration

                    // Periodically cache playback position to Room database for offline resume
                    tick++
                    if (tick % 4 == 0 && currentPositionMs > 1000) {
                        viewModel.saveLessonWatchProgress(lesson.id, course.id, currentPositionMs / 1000)
                    }

                    // Automatically mark completed if watched >= 90%
                    if (durationMs > 0 && currentPositionMs >= durationMs * 0.9 && !isCompleted) {
                        onCompleteLesson(lesson.id)
                    }
                }
            }
            delay(500L)
        }
    }

    // All lessons in course for next/prev navigation
    val allLessons = remember(course) { course.sections.flatMap { it.lessons } }
    val currentIndex = allLessons.indexOfFirst { it.id == lesson.id }
    val prevLesson = if (currentIndex > 0) allLessons[currentIndex - 1] else null
    val nextLesson = if (currentIndex in 0 until allLessons.size - 1) allLessons[currentIndex + 1] else null

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Navigation Bar
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("lesson_player_back_btn")) {
                    Icon(Icons.Default.ArrowForward, contentDescription = stringResource(R.string.ui_11e8376))
                }

                Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                    Text(
                        text = lesson.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = course.title,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = onOpenTutor, modifier = Modifier.testTag("lesson_player_tutor_btn")) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = stringResource(R.string.ui_9ec9cea), tint = OrbitIndigo)
                }
            }
        }

        // Offline Room Cache Status Badge
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = OrbitEmerald.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth().testTag("room_offline_cache_badge")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CloudDone, contentDescription = null, tint = OrbitEmerald, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.ui_4349ddc),
                            color = OrbitEmerald,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (isCompleted) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = OrbitEmerald
                        ) {
                            Text(stringResource(R.string.ui_59488a5), color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }
        }

        // Dedicated Video Player View with Real Android VideoView
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .testTag("video_player_container")
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                setVideoURI(videoUri)
                                setOnPreparedListener { mp ->
                                    durationMs = mp.duration
                                    mp.isLooping = true
                                    start()
                                    isPlaying = true
                                }
                                setOnErrorListener { _, _, _ ->
                                    // Fallback gracefully
                                    true
                                }
                                videoViewRef = this
                            }
                        },
                        update = { vv ->
                            videoViewRef = vv
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Overlay Controls (Gradient)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (!isPlaying) Color.Black.copy(alpha = 0.4f) else Color.Transparent
                            )
                    )

                    // Center Play/Pause button
                    IconButton(
                        onClick = {
                            videoViewRef?.let { vv ->
                                if (vv.isPlaying) {
                                    vv.pause()
                                    isPlaying = false
                                } else {
                                    vv.start()
                                    isPlaying = true
                                }
                            }
                        },
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(OrbitGold.copy(alpha = 0.9f))
                            .align(Alignment.Center)
                            .testTag("video_play_pause_btn")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) stringResource(R.string.ui_e409dbb) else stringResource(R.string.ui_0d24133),
                            tint = OrbitNavyDark,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Fast Forward & Rewind 10s buttons
                    Row(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                videoViewRef?.let { vv ->
                                    val newPos = (vv.currentPosition - 10000).coerceAtLeast(0)
                                    vv.seekTo(newPos)
                                    currentPositionMs = newPos
                                }
                            },
                            modifier = Modifier.size(38.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Replay10, contentDescription = stringResource(R.string.ui_1d31442), tint = Color.White)
                        }

                        IconButton(
                            onClick = {
                                videoViewRef?.let { vv ->
                                    val newPos = (vv.currentPosition + 10000).coerceAtMost(durationMs)
                                    vv.seekTo(newPos)
                                    currentPositionMs = newPos
                                }
                            },
                            modifier = Modifier.size(38.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Forward10, contentDescription = stringResource(R.string.ui_9328fc8), tint = Color.White)
                        }
                    }

                    // Bottom Player Bar with Slider & Timestamps
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color.Black.copy(alpha = 0.75f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        // Slider
                        Slider(
                            value = currentPositionMs.toFloat(),
                            onValueChange = { newPos ->
                                currentPositionMs = newPos.toInt()
                                videoViewRef?.seekTo(newPos.toInt())
                            },
                            valueRange = 0f..durationMs.toFloat().coerceAtLeast(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = OrbitGold,
                                activeTrackColor = OrbitGold,
                                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth().height(20.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val currentSec = currentPositionMs / 1000
                            val totalSec = durationMs / 1000
                            Text(
                                text = "${currentSec / 60}:${String.format("%02d", currentSec % 60)} / ${totalSec / 60}:${String.format("%02d", totalSec % 60)} ($progressPercent%)",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Speed button
                                Surface(
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.clickable {
                                        playbackSpeed = when (playbackSpeed) {
                                            1.0f -> 1.25f
                                            1.25f -> 1.5f
                                            1.5f -> 2.0f
                                            else -> 1.0f
                                        }
                                    }
                                ) {
                                    Text(
                                        text = "${playbackSpeed}x",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { isMuted = !isMuted },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                        contentDescription = stringResource(R.string.ui_1caf4c1),
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Real-Time Progress Bar & Milestones Card
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timeline, contentDescription = null, tint = OrbitIndigo, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.ui_6c2e06d), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Surface(
                            color = if (isCompleted) OrbitEmerald.copy(alpha = 0.15f) else OrbitGold.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (isCompleted) stringResource(R.string.ui_f64d000) else "قيد المشاهدة: $progressPercent%",
                                color = if (isCompleted) OrbitEmerald else OrbitGoldDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = (progressPercent / 100f).coerceIn(0f, 1f),
                        color = if (isCompleted) OrbitEmerald else OrbitIndigo,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(stringResource(R.string.ui_4385315), "25%", "50%", "75%", stringResource(R.string.ui_c7d1f41)).forEach { milestone ->
                            Text(milestone, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Quick Socratic AI Assistant Banner
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = OrbitIndigo.copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(1.dp, OrbitIndigo.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenTutor() }
                    .testTag("socratic_banner_in_player")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(34.dp).clip(CircleShape).background(OrbitIndigo),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(stringResource(R.string.ui_b99c810), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(stringResource(R.string.ui_0e06efe), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = OrbitIndigo)
                }
            }
        }

        // Sub Tabs: Notes, Transcript, Resources
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = activeTab == "notes",
                    onClick = { activeTab = "notes" },
                    label = { Text(stringResource(R.string.ui_a47434c), fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = OrbitIndigo, selectedLabelColor = Color.White),
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = activeTab == "transcript",
                    onClick = { activeTab = "transcript" },
                    label = { Text(stringResource(R.string.ui_f4dbc4a), fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = OrbitIndigo, selectedLabelColor = Color.White),
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = activeTab == "resources",
                    onClick = { activeTab = "resources" },
                    label = { Text("المرفقات (${lesson.resources.size}) 📎", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = OrbitIndigo, selectedLabelColor = Color.White),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Tab Content
        when (activeTab) {
            "notes" -> {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(stringResource(R.string.ui_4d21e76), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = lesson.bodyMarkdown.ifBlank {
                                    stringResource(R.string.ui_a2f4bef)
                                },
                                fontSize = 13.sp,
                                lineHeight = 22.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            "transcript" -> {
                if (lesson.transcript.isEmpty()) {
                    item {
                        Text(stringResource(R.string.ui_13805d6), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    items(lesson.transcript) { line ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val targetMs = line.timeSeconds * 1000
                                    videoViewRef?.seekTo(targetMs)
                                    currentPositionMs = targetMs
                                }
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = OrbitIndigo.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${line.timeSeconds / 60}:${String.format("%02d", line.timeSeconds % 60)}",
                                        fontWeight = FontWeight.Bold,
                                        color = OrbitIndigo,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(line.speaker, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = OrbitNavy)
                                    Text(line.text, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            "resources" -> {
                if (lesson.resources.isEmpty()) {
                    item {
                        Text(stringResource(R.string.ui_b6ef100), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    items(lesson.resources) { res ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = OrbitRose)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(res.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("حجم الملف: ${res.fileSize} • نوع: ${res.fileType.uppercase()}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Button(
                                    onClick = { /* downloaded / viewed */ },
                                    colors = ButtonDefaults.buttonColors(containerColor = OrbitIndigo),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(R.string.ui_969879d), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Action Controls: Complete & Next/Previous Lesson Navigation
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onCompleteLesson(lesson.id) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCompleted) OrbitEmerald else OrbitNavy
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).testTag("mark_lesson_completed_btn")
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isCompleted) stringResource(R.string.ui_c4e74f5) else stringResource(R.string.ui_a0ec785))
                }

                if (lesson.quiz != null) {
                    Button(
                        onClick = onOpenQuiz,
                        colors = ButtonDefaults.buttonColors(containerColor = OrbitGold, contentColor = OrbitNavyDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("start_lesson_quiz_btn")
                    ) {
                        Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.ui_5264003), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Prev & Next Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (prevLesson != null) {
                    OutlinedButton(
                        onClick = { viewModel.openLessonPlayer(course, prevLesson) },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("← الدرس السابق: ${prevLesson.title.take(15)}...", fontSize = 11.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (nextLesson != null) {
                    Button(
                        onClick = { viewModel.openLessonPlayer(course, nextLesson) },
                        colors = ButtonDefaults.buttonColors(containerColor = OrbitIndigo),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("الدرس التالي: ${nextLesson.title.take(15)}... →", fontSize = 11.sp)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
