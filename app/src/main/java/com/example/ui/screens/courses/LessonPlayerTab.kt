package com.example.ui.screens.courses

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.models.*
import com.example.ui.components.CertificateCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.CourseSubView
import com.example.ui.viewmodel.OrbitViewModel

@Composable
fun LessonPlayerView(
    course: Course,
    lesson: Lesson,
    progressMap: Map<String, LessonProgress>,
    onBack: () -> Unit,
    onCompleteLesson: (String) -> Unit,
    onOpenQuiz: () -> Unit,
    onOpenTutor: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentSeekSec by remember { mutableStateOf(45) }
    val isCompleted = progressMap[lesson.id]?.completed == true

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "رجوع")
                }
                Text(lesson.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                IconButton(onClick = onOpenTutor) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "المساعد الذكي", tint = OrbitIndigo)
                }
            }
        }

        // Video Player Screen
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = course.thumbnailUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)))

                    // Central Play Button
                    IconButton(
                        onClick = { isPlaying = !isPlaying },
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(OrbitGold)
                            .align(Alignment.Center)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "تشغيل/إيقاف",
                            tint = OrbitNavyDark,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Bottom Player Controls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${currentSeekSec / 60}:${String.format("%02d", currentSeekSec % 60)} / ${lesson.durationSeconds / 60}:00",
                            color = Color.White,
                            fontSize = 11.sp
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Icon(Icons.Default.Fullscreen, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // Quick Socratic Floating Bar
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = OrbitIndigo.copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(1.dp, OrbitIndigo.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenTutor() }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = OrbitIndigo)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("المساعد السقراطي لمس سارة 💡", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("انقر هنا للتفكير الذكي وطلب تلميحات المسائل", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = OrbitIndigo)
                }
            }
        }

        // Lesson Notes & Body
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ملخص الدرس والقواعد الذهبية 📖", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = lesson.bodyMarkdown.ifBlank { "شاهد الفيديو بتركيز، وطبق الحركات بإصبعي الإبهام والسبابة على العداد كما تشرح مس سارة." },
                        fontSize = 13.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Live Transcript Lines
        if (lesson.transcript.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("النص المسجل والتوقيتات التفاعلية ⏱️", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        lesson.transcript.forEach { line ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable { currentSeekSec = line.timeSeconds }
                            ) {
                                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${line.timeSeconds / 60}:${String.format("%02d", line.timeSeconds % 60)}",
                                        fontWeight = FontWeight.Bold,
                                        color = OrbitIndigo,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(line.text, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons: Mark completed & Quiz
        item {
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
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isCompleted) "الدرس مكتمل ✓" else "تحديد كمكتمل")
                }

                if (lesson.quiz != null) {
                    Button(
                        onClick = onOpenQuiz,
                        colors = ButtonDefaults.buttonColors(containerColor = OrbitGold, contentColor = OrbitNavyDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("بدء الاختبار 📝", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

