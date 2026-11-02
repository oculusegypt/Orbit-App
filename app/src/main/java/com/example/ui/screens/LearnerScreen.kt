package com.example.ui.screens

import androidx.compose.ui.res.stringResource
import com.example.R

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.models.Assignment
import com.example.data.models.Course
import com.example.ui.theme.*
import com.example.ui.viewmodel.OrbitViewModel
import com.example.ui.viewmodel.PortalTab

@Composable
fun LearnerScreen(
    viewModel: OrbitViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val enrollments by viewModel.enrollments.collectAsState()
    val certificates by viewModel.certificates.collectAsState()
    val progressMap by viewModel.progressMap.collectAsState()
    val assignments by viewModel.assignments.collectAsState()
    val submissions by viewModel.submissions.collectAsState()
    val streak by viewModel.mentalMathStreak.collectAsState()
    val xp by viewModel.mentalMathXp.collectAsState()
    val fcmToken by viewModel.fcmToken.collectAsState()
    val alertsHistory by viewModel.alertsHistory.collectAsState()
    val subscribedTopics by viewModel.subscribedTopics.collectAsState()

    var activeSubmittingAssignment by remember { mutableStateOf<Assignment?>(null) }
    var submissionText by remember { mutableStateOf("") }
    var submissionLink by remember { mutableStateOf("") }

    val myEnrollmentCourseIds = enrollments.map { it.courseId }.toSet()
    val myCourses = courses.filter { myEnrollmentCourseIds.contains(it.id) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Learner Header
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("learner_profile_card")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (currentUser?.avatarUrl.isNullOrBlank()) {
                        Box(
                            modifier = Modifier.size(56.dp).clip(CircleShape).background(OrbitIndigo),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                    } else {
                        AsyncImage(
                            model = currentUser?.avatarUrl,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp).clip(CircleShape)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser?.displayName ?: stringResource(R.string.ui_1e746ed),
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.ui_59e56f9),
                            fontSize = 11.sp,
                            color = OrbitIndigo
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(color = Color(0xFFFEF3C7), shape = RoundedCornerShape(8.dp)) {
                                Text("🔥 حماسة: $streak أيام", color = OrbitGoldDark, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            Surface(color = Color(0xFFEEF2FF), shape = RoundedCornerShape(8.dp)) {
                                Text("⚡ $xp XP", color = OrbitIndigo, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                }
            }
        }

        // Direct Private Tutor Call Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, OrbitGold),
                modifier = Modifier.fillMaxWidth().testTag("learner_private_call_action_card")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(OrbitGold.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = null, tint = OrbitGoldDark)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(stringResource(R.string.ui_b392aed), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(stringResource(R.string.ui_abc58d5), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Button(
                        onClick = { viewModel.setPortal(PortalTab.CONNECT) },
                        colors = ButtonDefaults.buttonColors(containerColor = OrbitIndigo),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_learner_to_connect")
                    ) {
                        Text(stringResource(R.string.ui_6f8dc6b), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Badges Showcase
        item {
            Text(stringResource(R.string.ui_3c64267), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val badges = listOf(
                    Triple(stringResource(R.string.ui_ecf23aa), stringResource(R.string.ui_9438850), Icons.Default.Calculate),
                    Triple(stringResource(R.string.ui_71ab7c9), stringResource(R.string.ui_b3260bb), Icons.Default.MenuBook),
                    Triple(stringResource(R.string.ui_9e74ea8), stringResource(R.string.ui_8de2283), Icons.Default.Bolt),
                    Triple(stringResource(R.string.ui_3618782), stringResource(R.string.ui_8989f93), Icons.Default.Verified)
                )

                badges.forEach { (title, desc, icon) ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(32.dp).clip(CircleShape).background(OrbitGold.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, contentDescription = null, tint = OrbitGoldDark, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            Text(desc, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }
            }
        }

        // Enrolled Courses
        item {
            Text("دوراتي التدريبية المعتمدة (${myCourses.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        if (myCourses.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(stringResource(R.string.ui_cd1129f), fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.ui_0bf5bc7), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(myCourses) { course ->
                val allLessons = course.sections.flatMap { it.lessons }
                val completedCount = allLessons.count { progressMap[it.id]?.completed == true }
                val progressPercent = if (allLessons.isNotEmpty()) (completedCount * 100) / allLessons.size else 0

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().testTag("learner_course_card_${course.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = course.thumbnailUrl,
                                contentDescription = null,
                                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(10.dp))
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(course.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("معلمتك: ${course.instructor.name}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("نسبة الإنجاز: $progressPercent%", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = OrbitIndigo)
                            Text("$completedCount من ${allLessons.size} درساً", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        LinearProgressIndicator(
                            progress = progressPercent / 100f,
                            color = OrbitEmerald,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).height(6.dp).clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.openCourseDetail(course) },
                                colors = ButtonDefaults.buttonColors(containerColor = OrbitIndigo),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(stringResource(R.string.ui_e4f8cd6), fontSize = 12.sp)
                            }

                            FilledTonalButton(
                                onClick = { viewModel.resumeOfflineCourse(course) },
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = OrbitEmerald.copy(alpha = 0.15f), contentColor = OrbitEmerald),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.3f).testTag("resume_offline_btn_${course.id}")
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.ui_6a520ee), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Assignments and Homework
        item {
            Text(stringResource(R.string.ui_699e23d), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        items(assignments) { asg ->
            val mySub = submissions.find { it.assignmentId == asg.id && it.studentId == (currentUser?.id ?: "") }
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(asg.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        Surface(
                            color = if (mySub?.status == "graded") OrbitEmerald.copy(alpha = 0.15f) else if (mySub != null) Color(0xFFEEF2FF) else Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (mySub?.status == "graded") "تم التصحيح (${mySub.grade}/${mySub.maxScore})" else if (mySub != null) stringResource(R.string.ui_df69277) else stringResource(R.string.ui_ca69196),
                                color = if (mySub?.status == "graded") OrbitEmerald else if (mySub != null) OrbitIndigo else OrbitGoldDark,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(asg.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    if (mySub?.feedback != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = OrbitGold.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "ملاحظات مس سارة: ${mySub.feedback}",
                                fontSize = 11.sp,
                                color = OrbitNavyDark,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (mySub == null) {
                        Button(
                            onClick = {
                                activeSubmittingAssignment = asg
                                submissionText = ""
                                submissionLink = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OrbitIndigo),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.ui_191eda4), fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // My Certificates
        if (certificates.isNotEmpty()) {
            item {
                Text(stringResource(R.string.ui_35223d4), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            items(certificates) { cert ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = OrbitGoldDark, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(cert.courseTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("كود: ${cert.verificationCode} • درجة: ${cert.gradePercentage}%", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Button(
                            onClick = { viewModel.openCertificate(cert) },
                            colors = ButtonDefaults.buttonColors(containerColor = OrbitIndigo),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(stringResource(R.string.ui_6e63a5f), fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Firebase Cloud Messaging (FCM) Live Alerts & Module Reminders Section
        item {
            Text(stringResource(R.string.ui_d3d5f12), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("fcm_alerts_hub_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(OrbitRose.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = OrbitRose, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(stringResource(R.string.ui_bb91d36), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(stringResource(R.string.ui_ca737d5), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = OrbitEmerald.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = stringResource(R.string.ui_24a8301),
                                color = OrbitEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // FCM Device Registration Token badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.ui_675deca), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = if (fcmToken.isNotBlank()) fcmToken.take(28) + "..." else stringResource(R.string.ui_f046aa7),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = OrbitIndigo
                                )
                            }
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = OrbitIndigo, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(stringResource(R.string.ui_16e6b5d), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.triggerLiveClassAlert(context) },
                            colors = ButtonDefaults.buttonColors(containerColor = OrbitRose),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("trigger_live_class_alert_btn")
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.ui_2d3753b), fontSize = 11.sp)
                        }

                        Button(
                            onClick = { viewModel.triggerModuleReminderAlert(context) },
                            colors = ButtonDefaults.buttonColors(containerColor = OrbitIndigo),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("trigger_module_reminder_btn")
                        ) {
                            Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.ui_bb3e833), fontSize = 11.sp)
                        }
                    }

                    // Alerts History List
                    if (alertsHistory.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(stringResource(R.string.ui_3644d9c), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        alertsHistory.take(3).forEach { alert ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (alert.type == "live_class") OrbitRose.copy(alpha = 0.08f) else OrbitIndigo.copy(alpha = 0.08f),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (alert.type == "live_class") Icons.Default.LiveTv else Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = if (alert.type == "live_class") OrbitRose else OrbitIndigo,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(alert.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(alert.body, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(alert.timestamp, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
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

    // Homework Submit Dialog
    activeSubmittingAssignment?.let { asg ->
        Dialog(onDismissRequest = { activeSubmittingAssignment = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("تسليم: ${asg.title}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(asg.courseTitle, fontSize = 11.sp, color = OrbitIndigo)
                    Divider(modifier = Modifier.padding(vertical = 10.dp))

                    OutlinedTextField(
                        value = submissionText,
                        onValueChange = { submissionText = it },
                        label = { Text(stringResource(R.string.ui_de79968)) },
                        placeholder = { Text(stringResource(R.string.ui_4da7fc4)) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(110.dp),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = submissionLink,
                        onValueChange = { submissionLink = it },
                        label = { Text(stringResource(R.string.ui_62b53d2)) },
                        placeholder = { Text(stringResource(R.string.ui_949ce06)) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (submissionText.isNotBlank()) {
                                viewModel.submitHomework(asg.id, asg.title, submissionText, submissionLink.ifBlank { null })
                                activeSubmittingAssignment = null
                            }
                        },
                        enabled = submissionText.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = OrbitEmerald),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(stringResource(R.string.ui_fae4f85), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
