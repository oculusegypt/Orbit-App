package com.example.ui.screens.student

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.*
import com.example.ui.navigation.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.OrbitViewModel

/**
 * Age-Adaptive Student Dashboard for Sara Al-Araby Educational Ecosystem.
 *
 * Automatically adapts layout, typography, interaction density, and visual aesthetics
 * based on the student's age tier:
 * - Tier 1: Early Childhood (up to 6 years) -> Visual, cartoonish, ultra-simplified.
 * - Tier 2: Junior Champions (7-8 years) -> Gamified, streak counter, XP stars, quick challenges.
 * - Tier 3: Young Geniuses (9-14 years) -> Mature academic schedule, exams, speed telemetry.
 */
@Composable
fun StudentAdaptiveDashboard(
    viewModel: OrbitViewModel,
    onNavigateToClassroom: () -> Unit,
    onOpenBookPdf: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allStudentProfiles by viewModel.studentProfiles.collectAsState()

    val activeProfile = remember(currentUser, allStudentProfiles) {
        if (currentUser?.role == UserRole.STUDENT) {
            val existing = allStudentProfiles.find { it.id == currentUser?.id }
            existing ?: StudentProfile(
                id = currentUser!!.id,
                parentId = "self",
                parentName = currentUser!!.displayName,
                name = currentUser!!.displayName,
                birthDate = "2018-05-12",
                age = 8,
                ageTier = AgeTier.JUNIOR_CHAMPIONS,
                country = PlatformCountry.EGYPT,
                avatarEmoji = "🚀",
                completedLessonsCount = 4,
                currentStreakDays = 2,
                totalXp = 320
            )
        } else if (currentUser?.role == UserRole.PARENT) {
            val children = allStudentProfiles.filter { it.parentId == currentUser?.id }
            children.firstOrNull() ?: StudentProfile(
                id = "std-preview",
                parentId = currentUser?.id ?: "guest",
                parentName = currentUser?.displayName ?: "ولي أمر",
                name = "طالب جديد",
                birthDate = "2018-05-12",
                age = 8,
                ageTier = AgeTier.JUNIOR_CHAMPIONS,
                country = PlatformCountry.EGYPT,
                avatarEmoji = "🚀"
            )
        } else {
            StudentProfile(
                id = "std-guest",
                parentId = "guest",
                parentName = "زائر",
                name = "بطل سارة المستقبلي 🌟",
                birthDate = "2018-05-12",
                age = 8,
                ageTier = AgeTier.JUNIOR_CHAMPIONS,
                country = PlatformCountry.EGYPT,
                avatarEmoji = "🚀"
            )
        }
    }

    val navController = rememberStudentNavController(initialStudent = activeProfile)

    LaunchedEffect(activeProfile) {
        navController.switchStudent(activeProfile)
    }

    var selectedBookModal by remember { mutableStateOf<String?>(null) }
    var currentBookPage by remember { mutableStateOf(1) }
    val totalBookPages = 18

    Box(modifier = modifier.fillMaxSize()) {
        StudentNavHost(
            navController = navController,
            onNavigateToClassroom = onNavigateToClassroom,
            onOpenGlobalBook = { selectedBookModal = it }
        )

    // Modal PDF Viewer with Page Synchronization Simulation
    if (selectedBookModal != null) {
        AlertDialog(
            onDismissRequest = { selectedBookModal = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "📖 $selectedBookModal",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "صفحة $currentBookPage من $totalBookPages",
                        fontSize = 12.sp,
                        color = OrbitIndigo,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Simulated resilient page rendering
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFEF3C7),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .border(2.dp, Color(0xFFF59E0B), RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "حصة مس سارة العربي 💡",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E),
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = when (currentBookPage) {
                                    1 -> "الدرس الأول: حرف الألف وصوت الفتحة (أَ - أَرْنَبٌ) 🐇"
                                    2 -> "الدرس الثاني: حرف الباء وتطبيقات نور البيان (بَ - بَطَّةٌ) 🦆"
                                    3 -> "الدرس الثالث: تمارين العداد الصيني — تمثيل الرقم 3 🧮"
                                    else -> "تمرين صفحة $currentBookPage: مسألة ذهنية وحروف نور البيان الممتعة 🌟"
                                },
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF78350F)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "✓ محتوى محفوظ محلياً (يعمل حتى لو انقطع الإنترنت)",
                                fontSize = 10.sp,
                                color = OrbitEmerald,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Page Navigation Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { if (currentBookPage > 1) currentBookPage-- },
                            enabled = currentBookPage > 1,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("السابق")
                        }

                        Button(
                            onClick = { if (currentBookPage < totalBookPages) currentBookPage++ },
                            enabled = currentBookPage < totalBookPages,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("التالي")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedBookModal = null }) {
                    Text("إغلاق الكتاب")
                }
            }
        )
    }
    }
}

/**
 * Tier 1: Early Childhood (حتى 6 سنوات)
 * Very playful, minimal text, big friendly buttons, high contrast, warm colors.
 */
@Composable
private fun EarlyChildhoodView(
    student: StudentProfile,
    onEnterClass: () -> Unit,
    onOpenBook: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Banner with big cheerful elements
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🌟 أهلاً بك يا بطل!", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF92400E))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "مع مس سارة العربي.. نتعلم الحروف والأرقام باللعب والمرح! 🎈",
                        fontSize = 13.sp,
                        color = Color(0xFFB45309),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Live Class Hero Button (Large, unmissable 64dp+ tap target)
        item {
            Button(
                onClick = onEnterClass,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrbitEmerald),
                contentPadding = PaddingValues(vertical = 18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tier1_enter_class_btn")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎥", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("ادخل حصة مس سارة الآن!", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("فصل تفاعلي حي مباشر 💡", fontSize = 11.sp, color = Color.White.copy(alpha = 0.9f))
                    }
                }
            }
        }

        // Picture Books Collection (Oversized cards)
        item {
            Text(
                text = "📚 كتبي المصورة الجميلة:",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = OrbitNavy
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BookCardVisual(
                    title = "كتاب نور البيان المصور",
                    emoji = "📖",
                    color = Color(0xFFDBEAFE),
                    onClick = { onOpenBook("كتاب نور البيان المصور للأطفال") },
                    modifier = Modifier.weight(1f)
                )
                BookCardVisual(
                    title = "أرقامي والعداد الصغير",
                    emoji = "🧮",
                    color = Color(0xFFD1FAE5),
                    onClick = { onOpenBook("كتاب أرقامي والعداد الصغير") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Reward Stars
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("⭐ نجوم البطل هذا الأسبوع:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("شاطر جداً في الحضور والمشاركة!", fontSize = 11.sp, color = Color.Gray)
                    }
                    Text("18 نجمة 🌟", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFFD97706))
                }
            }
        }
    }
}

/**
 * Tier 2: Junior Champions (7-8 سنوات)
 * Gamified layout with streak counter, XP stars, quick challenges, and daily tasks.
 */
@Composable
private fun JuniorChampionsView(
    student: StudentProfile,
    onEnterClass: () -> Unit,
    onOpenBook: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Gamified Streak & XP Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Streak Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFFEDD5),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔥", fontSize = 26.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("${student.currentStreakDays} أيام متتالية", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF9A3412))
                            Text("استمر في التميز!", fontSize = 10.sp, color = Color(0xFFC2410C))
                        }
                    }
                }

                // XP Stars Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFE0E7FF),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⭐", fontSize = 26.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("${student.totalXp} نقطة XP", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF3730A3))
                            Text("المستوى 2: بطل السوروبان", fontSize = 10.sp, color = Color(0xFF4338CA))
                        }
                    }
                }
            }
        }

        // Live Class Card with Group Info
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = OrbitIndigo),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text("حصة مباشرة الآن 🔴", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                        Text("الأحد والثلاثاء 4:00 م", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("فصل الأبطال: جمع وطرح السوروبان السريع 🧮", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    Text("مع مس سارة العربي • مدربة معتمدة", color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onEnterClass,
                        colors = ButtonDefaults.buttonColors(containerColor = OrbitEmerald),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tier2_enter_class_btn")
                    ) {
                        Icon(Icons.Default.VideoCall, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("الانضمام للحصة المباشرة والتفاعل", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Quick Daily Challenge
        item {
            Text("🎯 مهام وتحديات الأبطال اليومية:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OrbitNavy)
            Spacer(modifier = Modifier.height(8.dp))
            TaskItemRow(
                title = "واجب السوروبان: حل 5 مسائل بالعداد وتصوير الحل",
                dueText = "متبقي 6 ساعات",
                isCompleted = false,
                onOpen = { onOpenBook("كتاب تمارين السوروبان للأبطال") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            TaskItemRow(
                title = "قراءة قصة 'الأرنب السريع' بنور البيان",
                dueText = "تم التسليم بنجاح ✓",
                isCompleted = true,
                onOpen = { onOpenBook("قصص نور البيان المستوى الثاني") }
            )
        }
    }
}

/**
 * Tier 3: Young Geniuses (9-14 سنوات)
 * Mature academic view with timetable, speed telemetry, unit exams, and certificates.
 */
@Composable
private fun YoungGeniusesView(
    student: StudentProfile,
    onEnterClass: () -> Unit,
    onOpenBook: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Academic Status Bar
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = OrbitNavy),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("بوابة العباقرة الأكاديمية 🧠", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = OrbitGold.copy(alpha = 0.2f)
                        ) {
                            Text("المستوى المتقدم 3", color = OrbitGold, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        MetricColumn(label = "سرعة الحل الذهني", value = "0.75 ث/مسألة", color = OrbitEmerald)
                        MetricColumn(label = "نسبة الحضور", value = "96%", color = Color.White)
                        MetricColumn(label = "متوسط الاختبارات", value = "98 / 100", color = OrbitGold)
                    }
                }
            }
        }

        // Live Class Quick Entry
        item {
            Button(
                onClick = onEnterClass,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrbitIndigo),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tier3_enter_class_btn")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("الدخول إلى قاعة الحساب الذهني المتقدم (بث مباشر)", fontWeight = FontWeight.Bold)
            }
        }

        // Academic Timetable
        item {
            Text("📅 الجدول الدراسي الأسبوعي المعتمد:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OrbitNavy)
            Spacer(modifier = Modifier.height(8.dp))
            TimetableRow(
                subject = "حساب ذهني تخيلي متقدم (ضرب وقسمة)",
                time = "الأحد 06:00 م (بتوقيت القاهرة)",
                type = "حصة مجموعة رسمية",
                status = "قادمة"
            )
            Spacer(modifier = Modifier.height(6.dp))
            TimetableRow(
                subject = "اختبار الوحدة الثالثة: سرعة العمليات المتزامنة",
                time = "الثلاثاء 06:00 م (بتوقيت القاهرة)",
                type = "اختبار إلكتروني",
                status = "مجدول"
            )
        }

        // Books & Curriculum
        item {
            Text("📚 المناهج والمراجع المعتمدة لمس سارة:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OrbitNavy)
            Spacer(modifier = Modifier.height(8.dp))
            CurriculumBookRow(
                title = "موسوعة السوروبان الشاملة: المستوى الثالث للعباقرة",
                author = "أ / سارة العربي",
                totalPages = 48,
                onClick = { onOpenBook("موسوعة السوروبان الشاملة: المستوى الثالث") }
            )
        }
    }
}

@Composable
private fun BookCardVisual(
    title: String,
    emoji: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = color,
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag("book_card_$emoji")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 36.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = OrbitNavy
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("افتح الكتاب 👆", fontSize = 10.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun TaskItemRow(
    title: String,
    dueText: String,
    isCompleted: Boolean,
    onOpen: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isCompleted) OrbitEmerald else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = OrbitNavy)
                    Text(dueText, fontSize = 10.sp, color = if (isCompleted) OrbitEmerald else Color(0xFFE11D48))
                }
            }

            Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun MetricColumn(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
        Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
    }
}

@Composable
private fun TimetableRow(subject: String, time: String, type: String, status: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(subject, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = OrbitNavy)
                Text("$type • $time", fontSize = 10.sp, color = Color.Gray)
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFEFF6FF)
            ) {
                Text(status, color = OrbitIndigo, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
        }
    }
}

@Composable
private fun CurriculumBookRow(title: String, author: String, totalPages: Int, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📘", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = OrbitNavy)
                    Text("$author • $totalPages صفحة PDF", fontSize = 10.sp, color = Color.Gray)
                }
            }
            TextButton(onClick = onClick) {
                Text("تصفح", fontSize = 11.sp)
            }
        }
    }
}
