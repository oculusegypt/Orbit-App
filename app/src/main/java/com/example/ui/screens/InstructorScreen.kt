package com.example.ui.screens

import androidx.compose.ui.res.stringResource
import com.example.R

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.models.StudentSubmission
import com.example.data.repository.OrbitRepository
import com.example.ui.theme.OrbitEmerald
import com.example.ui.theme.OrbitGold
import com.example.ui.theme.OrbitGoldDark
import com.example.ui.theme.OrbitIndigo
import com.example.ui.theme.OrbitNavy
import com.example.ui.theme.OrbitRose
import com.example.ui.viewmodel.OrbitViewModel
import com.example.ui.viewmodel.PortalTab

@Composable
fun InstructorScreen(
    viewModel: OrbitViewModel,
    modifier: Modifier = Modifier
) {
    val submissions by viewModel.submissions.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val instructor = OrbitRepository.INSTRUCTOR_SARA

    var activeGradingSub by remember { mutableStateOf<StudentSubmission?>(null) }
    var gradeInput by remember { mutableStateOf("95") }
    val initialFeedback = stringResource(R.string.ui_372887e)
    val fallbackFeedback = stringResource(R.string.ui_94f96b5)
    var feedbackInput by remember { mutableStateOf(initialFeedback) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Studio Header
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = OrbitNavy),
                modifier = Modifier.fillMaxWidth().testTag("instructor_studio_header")
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    AsyncImage(
                        model = instructor.avatarUrl,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp).clip(CircleShape)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(instructor.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(color = OrbitGold, shape = RoundedCornerShape(4.dp)) {
                                Text(stringResource(R.string.ui_52f843c), color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                        Text(stringResource(R.string.ui_880bda3), color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                    }
                }
            }
        }

        // Stats Overview Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("184", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = OrbitIndigo)
                        Text(stringResource(R.string.ui_5a3397f), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${submissions.size}", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = OrbitGold)
                        Text(stringResource(R.string.ui_c92c6df), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${courses.size}", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = OrbitEmerald)
                        Text(stringResource(R.string.ui_bbbafeb), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Live Classroom Quick Launch
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.ui_50548e4), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(stringResource(R.string.ui_36037bd), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Button(
                        onClick = { viewModel.setPortal(PortalTab.CLASSROOM) },
                        colors = ButtonDefaults.buttonColors(containerColor = OrbitRose),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.ui_da9ebf2), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Private 1-on-1 Student Calling Action
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, OrbitGold),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("instructor_private_call_card")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.ui_6b30dc4), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(stringResource(R.string.ui_f1aca94), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Button(
                        onClick = { viewModel.setPortal(PortalTab.CONNECT) },
                        colors = ButtonDefaults.buttonColors(containerColor = OrbitGoldDark),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_instructor_to_connect")
                    ) {
                        Icon(Icons.Default.PhoneInTalk, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.ui_6f8dc6b), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Submissions Grading Queue
        item {
            Text(stringResource(R.string.ui_c8a34dd), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        items(submissions) { sub ->
            val isGraded = sub.status == "graded"
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("instructor_sub_${sub.id}")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(sub.studentName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("تاريخ التسليم: ${sub.submittedAt}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Surface(
                            color = if (isGraded) OrbitEmerald.copy(alpha = 0.15f) else Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (isGraded) "تم التصحيح (${sub.grade}/${sub.maxScore}) ✓" else stringResource(R.string.ui_ae1b6ab),
                                color = if (isGraded) OrbitEmerald else OrbitGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("واجب: ${sub.assignmentTitle}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = OrbitIndigo)

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "\"${sub.textSubmission}\"",
                            fontSize = 12.sp,
                            modifier = Modifier.padding(8.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (sub.feedback != null) {
                        Text(
                            text = "ملاحظاتك للطفل: ${sub.feedback}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = OrbitNavy
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            activeGradingSub = sub
                            gradeInput = (sub.grade ?: 95).toString()
                            feedbackInput = sub.feedback ?: fallbackFeedback
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OrbitIndigo),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isGraded) stringResource(R.string.ui_eb50ccd) else stringResource(R.string.ui_e7e2a43))
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Grading Dialog
    activeGradingSub?.let { sub ->
        Dialog(onDismissRequest = { activeGradingSub = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("تصحيح واجب الطالب: ${sub.studentName}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(sub.assignmentTitle, fontSize = 11.sp, color = OrbitIndigo)
                    Divider(modifier = Modifier.padding(vertical = 10.dp))

                    OutlinedTextField(
                        value = gradeInput,
                        onValueChange = { gradeInput = it },
                        label = { Text("الدرجة من ${sub.maxScore}") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = feedbackInput,
                        onValueChange = { feedbackInput = it },
                        label = { Text(stringResource(R.string.ui_95b7790)) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val gradeNum = gradeInput.toIntOrNull() ?: 90
                            viewModel.gradeStudentSubmission(sub.id, gradeNum, feedbackInput)
                            activeGradingSub = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OrbitEmerald),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(stringResource(R.string.ui_c45eae5), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
