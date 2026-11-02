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
fun CourseDetailView(
    course: Course,
    progressMap: Map<String, LessonProgress>,
    onBack: () -> Unit,
    onStartLesson: (Lesson) -> Unit,
    onEnroll: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                Text("تفاصيل الدورة التدريبية", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(48.dp))
            }
        }

        // Course Hero
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(course.title, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, lineHeight = 26.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(course.summary, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("الرسوم:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(course.priceFormatted, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OrbitIndigo)
                        }

                        Button(
                            onClick = onEnroll,
                            colors = ButtonDefaults.buttonColors(containerColor = OrbitEmerald),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("الاشتراك في الدورة", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Learning Outcomes
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Checklist, contentDescription = null, tint = OrbitIndigo)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ماذا سيتعلم الطفل في هذا الدبلوم؟", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    course.outcomes.forEach { outcome ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text("✓ ", color = OrbitEmerald, fontWeight = FontWeight.Bold)
                            Text(outcome, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Sections & Lessons Curriculum
        item {
            Text("محتوى المنهج والدروس (${course.sections.sumOf { it.lessons.size }} درساً)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        items(course.sections) { section ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(section.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OrbitNavy)
                    Spacer(modifier = Modifier.height(8.dp))

                    section.lessons.forEach { lesson ->
                        val isCompleted = progressMap[lesson.id]?.completed == true
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onStartLesson(lesson) }
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = if (lesson.quiz != null) Icons.Default.Quiz else Icons.Default.PlayCircle,
                                        contentDescription = null,
                                        tint = if (isCompleted) OrbitEmerald else OrbitIndigo,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(lesson.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("${lesson.durationSeconds / 60} دقيقة", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                if (isCompleted) {
                                    Surface(color = OrbitEmerald.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)) {
                                        Text("مكتمل ✓", fontSize = 10.sp, color = OrbitEmerald, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                } else if (lesson.isPreview) {
                                    Surface(color = OrbitGold.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)) {
                                        Text("معاينة مجانية", fontSize = 10.sp, color = OrbitGoldDark, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

