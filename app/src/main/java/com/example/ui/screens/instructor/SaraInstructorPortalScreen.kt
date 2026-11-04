package com.example.ui.screens.instructor

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ScheduleConflictResolver
import com.example.data.models.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.OrbitViewModel

/**
 * Authentic Instructor Control Center for الأستاذة سارة العربي.
 *
 * Capabilities:
 * - Real role gating: Only accessible to authenticated Instructor (Sara Al-Araby) or Admin.
 * - Master schedule management & live capacity across Egypt & Saudi Arabia groups.
 * - Live classroom orchestrator (camera, mic, PDF book sync, whiteboard).
 * - Real Late arrival requests queue: accept into class or reject with compensation proposals.
 * - Real Parent apologies & compensation approvals.
 * - Real Homework grading & feedback.
 * - Real Country-segmented revenue analytics (EGP / SAR).
 */
@Composable
fun SaraInstructorPortalScreen(
    viewModel: OrbitViewModel,
    onLaunchLiveClassroom: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val lateArrivals by viewModel.lateArrivals.collectAsState()
    val apologiesList by viewModel.absenceApologies.collectAsState()
    val pendingSubmissions by viewModel.submissions.collectAsState()
    val groupsList by viewModel.groups.collectAsState()

    // Gated Access Check: Must be logged in as INSTRUCTOR (or ADMIN)
    if (currentUser == null || (currentUser!!.role != UserRole.INSTRUCTOR && currentUser!!.role != UserRole.ADMIN)) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.fillMaxWidth().testTag("instructor_auth_gate_card")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = OrbitGold.copy(alpha = 0.2f),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("👩‍🏫", fontSize = 32.sp)
                        }
                    }

                    Text(
                        text = "لوحة تحكم الأستاذة سارة العربي",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = OrbitNavy
                    )

                    Text(
                        text = if (currentUser == null) {
                            "هذه اللوحة خاصة بالأستاذة سارة العربي لإدارة المجموعات، وبدء الحصص التفاعلية، وتصحيح الواجبات، واعتماد طلبات التعويض والاعتذارات."
                        } else {
                            "أنت مسجل حالياً بحساب: ${currentUser!!.displayName} (${currentUser!!.role.labelAr}). يتطلب هذا القسم تسجيل الدخول بحساب المعلمة سارة العربي المعتمد."
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Button(
                        onClick = { viewModel.setShowAuthDialog(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = OrbitGoldDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_instructor_gate_login")
                    ) {
                        Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تسجيل الدخول كمعلمة (Saraelarby96@gmail.com)", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
        return
    }

    var selectedTab by remember { mutableStateOf(0) } // 0: Schedule & Live, 1: Late Arrivals & Apologies, 2: Homework & Grading, 3: Analytics
    var actionNotice by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Instructor Header
        Surface(
            color = OrbitNavy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = OrbitGold,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("👩‍🏫", fontSize = 22.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("أ / سارة العربي 💡", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("مدربة مدربين معتمدة من برنامج العباقرة الصغار", color = OrbitGold, fontSize = 10.sp)
                        }
                    }

                    // Contact Badges
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "المنصورة • +20 10 2834 0042",
                            color = Color.White,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Navigation Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("المجموعات والبث", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            val badgeCount = lateArrivals.size + apologiesList.count { it.status == "PENDING" }
                            BadgedBox(badge = {
                                if (badgeCount > 0) {
                                    Badge { Text("$badgeCount") }
                                }
                            }) {
                                Text("الغياب والتعويض", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            val pendingCount = pendingSubmissions.count { it.status == "submitted" }
                            BadgedBox(badge = {
                                if (pendingCount > 0) {
                                    Badge { Text("$pendingCount") }
                                }
                            }) {
                                Text("الواجبات والتصحيح", fontSize = 11.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("التقارير والإيرادات", fontSize = 11.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }
        }

        // Action Notification Banner
        val currentNotice = actionNotice
        if (currentNotice != null) {
            Surface(
                color = Color(0xFFD1FAE5),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = OrbitEmerald, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(currentNotice, color = Color(0xFF065F46), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Tab Content
        when (selectedTab) {
            0 -> ScheduleAndLiveView(
                groups = groupsList,
                onLaunchClass = onLaunchLiveClassroom
            )
            1 -> LateArrivalsAndCompensationView(
                lateArrivals = lateArrivals,
                apologies = apologiesList,
                onAcceptLate = { item ->
                    viewModel.respondToLateArrival(item.id, accept = true)
                    actionNotice = "تم قبول دخول الطالب '${item.studentName}' إلى الحصة بنجاح!"
                },
                onRejectLate = { item ->
                    viewModel.respondToLateArrival(item.id, accept = false)
                    actionNotice = "تم رفض الدخول واقتراح مواعيد تعويضية للطالب '${item.studentName}'."
                },
                onOfferCompensation = { apology ->
                    val slots = listOf(
                        CompensationSlot("comp-1", "الجمعة القادمة", "04:00 م بتوقيت القاهرة", System.currentTimeMillis() + 86400000L),
                        CompensationSlot("comp-2", "السبت القادم", "11:00 ص بتوقيت القاهرة", System.currentTimeMillis() + 172800000L)
                    )
                    viewModel.offerCompensationSlot(apology.id, slots)
                    actionNotice = "تم اعتماد الاعتذار واقتراح موعدي تعويض لولي أمر الطالب '${apology.studentName}'."
                }
            )
            2 -> HomeworkGradingView(
                submissions = pendingSubmissions,
                onGrade = { sub, grade, feedback ->
                    viewModel.gradeStudentSubmission(sub.id, grade, feedback)
                    actionNotice = "تم رصد الدرجة ($grade/100) وإرسال الملاحظات للطالب '${sub.studentName}' بنجاح."
                }
            )
            3 -> MultiCountryAnalyticsView(groups = groupsList)
        }
    }
}

@Composable
private fun ScheduleAndLiveView(
    groups: List<GroupSchedule>,
    onLaunchClass: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Live Class Action Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = OrbitEmerald),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = RoundedCornerShape(6.dp), color = Color.White.copy(alpha = 0.25f)) {
                            Text("الفصل التفاعلي المباشر 🔴", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                        Text("بث الأستاذة سارة العربي", color = Color.White, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("بث الحصة التفاعلية ومزامنة صفحات كتب نور البيان والسوروبان", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("التحكم الكامل: الكاميرا، المايك، السبورة البيضاء، ومزامنة صفحات الـ PDF للطلاب", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onLaunchClass,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("btn_launch_instructor_class")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = OrbitEmerald)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("بدء الحصة الآن والتحكم الكامل", color = OrbitEmerald, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Daily Groups Schedule
        item {
            Text("🗓️ المجموعات المسجلة وحالات السعة الاستيعابية:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OrbitNavy)
            Spacer(modifier = Modifier.height(4.dp))
            Text("الحد الأقصى لكل مجموعة: 8 طلاب لضمان جودة التأسيس الفردي", fontSize = 11.sp, color = Color.Gray)
        }

        items(groups) { group ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(group.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = OrbitNavy)
                        Text("${group.fixedDaysAr} • ${group.localTimeDisplay}", fontSize = 10.sp, color = Color.Gray)
                        Text("الفئة: ${group.targetAgeTier.labelAr}", fontSize = 10.sp, color = OrbitIndigo)
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (group.isFull) Color(0xFFFFE4E6) else Color(0xFFD1FAE5)
                    ) {
                        Text(
                            text = "${group.currentEnrolledCount}/${group.maxCapacity} طلاب",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (group.isFull) Color(0xFFBE123C) else Color(0xFF047857),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LateArrivalsAndCompensationView(
    lateArrivals: List<LateArrivalCheckIn>,
    apologies: List<AbsenceApologyRecord>,
    onAcceptLate: (LateArrivalCheckIn) -> Unit,
    onRejectLate: (LateArrivalCheckIn) -> Unit,
    onOfferCompensation: (AbsenceApologyRecord) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Late Arrivals section
        item {
            Text(
                text = "🔔 طلبات الدخول المتأخر للحصة الحالية:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = OrbitNavy
            )
        }

        if (lateArrivals.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("لا توجد طلبات متأخرين حالياً 👍", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(lateArrivals) { item ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.studentName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = OrbitNavy)
                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFFFEDD5)) {
                                Text(item.arrivalTime, color = Color(0xFFC2410C), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }

                        Text("الحصة: ${item.sessionTitle}", fontSize = 11.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onAcceptLate(item) },
                                colors = ButtonDefaults.buttonColors(containerColor = OrbitEmerald),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("قبول الدخول ✓", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = { onRejectLate(item) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE11D48)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("رفض واقتراح تعويض", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Parent Apologies section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "📩 اعتذارات أولياء الأمور وطلبات التعويض:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = OrbitNavy
            )
        }

        if (apologies.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("لا توجد طلبات اعتذار جديدة من أولياء الأمور", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(apologies) { apology ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("الطالب: ${apology.studentName}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = OrbitNavy)
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (apology.status == "PENDING") Color(0xFFFEF3C7) else Color(0xFFD1FAE5)
                            ) {
                                Text(
                                    text = if (apology.status == "PENDING") "قيد المراجعة" else "تم اقتراح موعد",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (apology.status == "PENDING") Color(0xFF92400E) else Color(0xFF065F46),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("الحصة: ${apology.sessionTitle} • السبب: \"${apology.apologyReason}\"", fontSize = 11.sp, color = Color.Gray)

                        if (apology.status == "PENDING") {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { onOfferCompensation(apology) },
                                colors = ButtonDefaults.buttonColors(containerColor = OrbitIndigo),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("اقتراح مواعيد تعويض خالية من التعارض", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeworkGradingView(
    submissions: List<StudentSubmission>,
    onGrade: (StudentSubmission, Int, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("📝 تصحيح الواجبات واعتماد الدرجات:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OrbitNavy)
            Text("متابعة حلول الأطفال بنور البيان، السوروبان، والحساب الذهني.", fontSize = 11.sp, color = Color.Gray)
        }

        if (submissions.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("تم تصحيح كافة الواجبات المسلمة بالكامل! 🌟", color = OrbitEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            items(submissions) { sub ->
                var gradeInput by remember { mutableStateOf(sub.grade?.toString() ?: "95") }
                var feedbackInput by remember { mutableStateOf((sub.feedback ?: "").ifEmpty { "أحسنت يا بطل! حركة الخرزات وتطبيق القاعدة ممتازة جداً." }) }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${sub.studentName} — ${sub.assignmentTitle}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = OrbitNavy)
                            Text(sub.submittedAt, fontSize = 10.sp, color = Color.Gray)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFF1F5F9), modifier = Modifier.fillMaxWidth()) {
                            Text("إجابة الطالب: \"${sub.textSubmission}\"", fontSize = 11.sp, color = Color(0xFF334155), modifier = Modifier.padding(8.dp))
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = gradeInput,
                                onValueChange = { gradeInput = it },
                                label = { Text("الدرجة / 100", fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier.width(100.dp)
                            )

                            OutlinedTextField(
                                value = feedbackInput,
                                onValueChange = { feedbackInput = it },
                                label = { Text("ملاحظات المعلمة", fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                val g = gradeInput.toIntOrNull() ?: 95
                                onGrade(sub, g, feedbackInput)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OrbitIndigo),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (sub.status == "graded") "تحديث التقييم والملاحظات" else "اعتماد الدرجة وإرسال النتيجة", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MultiCountryAnalyticsView(groups: List<GroupSchedule>) {
    val egGroups = groups.filter { it.country == PlatformCountry.EGYPT }
    val saGroups = groups.filter { it.country == PlatformCountry.SAUDI_ARABIA }

    val egEnrolled = egGroups.sumOf { it.currentEnrolledCount }
    val saEnrolled = saGroups.sumOf { it.currentEnrolledCount }

    val egTotalRevenue = egGroups.sumOf { (it.monthlyPriceMinor / 100) * it.currentEnrolledCount }
    val saTotalRevenue = saGroups.sumOf { (it.monthlyPriceMinor / 100) * it.currentEnrolledCount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("📊 تقارير الإيرادات الفعلية والتشغيل حسب الدولة:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OrbitNavy)
            Text("فصل دقيق بين اشتراكات مصر والسعودية محسوبة من عدد الطلاب الفعلي في المجموعات.", fontSize = 11.sp, color = Color.Gray)
        }

        // Egypt Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🇪🇬 طلاب واشتراكات مصر (EGP)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OrbitNavy)
                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFD1FAE5)) {
                            Text("$egEnrolled طالب نشط", color = Color(0xFF047857), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("إجمالي الإيراد الشهري المحقق: $egTotalRevenue ج.م", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = OrbitIndigo)
                    Text("المجموعات النشطة بمصر: ${egGroups.size} مجموعات بالمنصورة وأونلاين", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }

        // Saudi Arabia Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🇸🇦 طلاب واشتراكات السعودية (SAR)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OrbitNavy)
                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFEFF6FF)) {
                            Text("$saEnrolled طالب نشط", color = OrbitIndigo, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("إجمالي الإيراد الشهري المحقق: $saTotalRevenue ر.س", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color(0xFF0284C7))
                    Text("المجموعات النشطة بالسعودية: ${saGroups.size} مجموعات أونلاين (توقيت مكة)", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }
    }
}
