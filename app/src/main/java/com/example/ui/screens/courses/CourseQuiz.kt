package com.example.ui.screens.courses

import androidx.compose.ui.res.stringResource
import com.example.R

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
fun QuizView(
    course: Course,
    lesson: Lesson,
    viewModel: OrbitViewModel,
    modifier: Modifier = Modifier
) {
    val quiz = lesson.quiz ?: return
    val currentIdx by viewModel.currentQuizIndex.collectAsState()
    val selectedOption by viewModel.quizSelectedOption.collectAsState()
    val isSubmitted by viewModel.quizSubmitted.collectAsState()
    val scorePercent by viewModel.quizScorePercent.collectAsState()
    val questions = quiz.questions

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
                IconButton(onClick = { viewModel.backToCourseDetail() }) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.ui_6080666))
                }
                Text(quiz.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.width(48.dp))
            }
        }

        if (isSubmitted) {
            item {
                val passed = scorePercent >= quiz.passingScore
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = if (passed) Icons.Default.EmojiEvents else Icons.Default.Replay,
                            contentDescription = null,
                            tint = if (passed) OrbitGold else OrbitIndigo,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (passed) stringResource(R.string.ui_a1b7f67) else stringResource(R.string.ui_2a32a18),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("درجتك: $scorePercent%", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = OrbitIndigo)

                        if (passed) {
                            Text(
                                text = stringResource(R.string.ui_ddc69b4),
                                fontSize = 12.sp,
                                color = OrbitEmerald,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    val cert = viewModel.selectedCertificate.value
                                    if (cert != null) viewModel.openCertificate(cert)
                                    else viewModel.backToCatalog()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = OrbitEmerald),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.WorkspacePremium, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.ui_753a9cd), fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.openQuiz(course, lesson) },
                                colors = ButtonDefaults.buttonColors(containerColor = OrbitIndigo),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.ui_fb452d3))
                            }
                        }
                    }
                }
            }
        } else {
            val q = questions.getOrNull(currentIdx)
            if (q != null) {
                item {
                    // Question Progress
                    Text(
                        text = "السؤال ${currentIdx + 1} من ${questions.size}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    LinearProgressIndicator(
                        progress = (currentIdx + 1).toFloat() / questions.size,
                        color = OrbitIndigo,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).height(6.dp).clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(q.prompt, fontSize = 16.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp)
                            Spacer(modifier = Modifier.height(16.dp))

                            q.options.forEachIndexed { i, opt ->
                                val isSelected = selectedOption == i
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) OrbitIndigo.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, OrbitIndigo) else null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.selectQuizOption(i) }
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = isSelected, onClick = { viewModel.selectQuizOption(i) })
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(opt, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { viewModel.confirmQuizAnswer() },
                                enabled = selectedOption != null,
                                colors = ButtonDefaults.buttonColors(containerColor = OrbitIndigo),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text(
                                    text = if (currentIdx + 1 < questions.size) stringResource(R.string.ui_2553160) else stringResource(R.string.ui_5f5e8fc),
                                    fontWeight = FontWeight.Bold
                                )
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

