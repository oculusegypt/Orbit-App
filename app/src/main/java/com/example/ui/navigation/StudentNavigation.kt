package com.example.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.activity.compose.BackHandler
import com.example.data.models.AgeTier
import com.example.data.models.PlatformCountry
import com.example.data.models.StudentProfile
import com.example.ui.theme.*

/**
 * 3 Discrete Age Categories for the Student Navigation Architecture in Compose Multiplatform:
 * 1. CHILD (<6 years): Early Childhood / براعم سارة
 * 2. YOUTH (7-8 years): Junior Champions / أبطال سارة
 * 3. PRE_TEEN (9-14 years): Young Geniuses / عباقرة سارة
 */
enum class StudentAgeCategory(
    val code: String,
    val titleEn: String,
    val titleAr: String,
    val ageRangeDisplay: String,
    val minAge: Int,
    val maxAge: Int,
    val badgeEmoji: String,
    val accentColor: Color,
    val descriptionAr: String
) {
    CHILD(
        code = "child_under_6",
        titleEn = "Child (<6)",
        titleAr = "براعم سارة (التأسيس المبكر)",
        ageRangeDisplay = "أقل من 6 سنوات",
        minAge = 3,
        maxAge = 6,
        badgeEmoji = "🌱",
        accentColor = Color(0xFFF59E0B),
        descriptionAr = "تأسيس الحروف والأرقام بنور البيان مع ألعاب تفاعلية وكروت مصورة سهلة الاستخدام للأطفال الصغار."
    ),
    YOUTH(
        code = "youth_7_8",
        titleEn = "Youth (7-8)",
        titleAr = "أبطال سارة (الحساب السريع والمهام)",
        ageRangeDisplay = "7 إلى 8 سنوات",
        minAge = 7,
        maxAge = 8,
        badgeEmoji = "🚀",
        accentColor = OrbitIndigo,
        descriptionAr = "بيئة ألعابية بنقاط XP وعداد الأيام المتتالية مع تمارين العداد الصيني (السوروبان) والتحديات اليومية."
    ),
    PRE_TEEN(
        code = "preteen_9_14",
        titleEn = "Pre-teen (9-14)",
        titleAr = "عباقرة سارة (الحساب الذهني المتقدم والأكاديمي)",
        ageRangeDisplay = "9 إلى 14 سنة",
        minAge = 9,
        maxAge = 14,
        badgeEmoji = "🧠",
        accentColor = OrbitEmerald,
        descriptionAr = "جدول أكاديمي متقدم، اختبارات رقمية، تحليلات سرعة الحساب الذهني التخيلي، وتتبع الشهادات المعتمدة."
    );

    companion object {
        /**
         * Resolves age category based on numeric age strictly:
         * - age < 6 (or <= 6) -> CHILD
         * - age in 7..8 -> YOUTH
         * - age in 9..14 (or >= 9) -> PRE_TEEN
         */
        fun fromAge(age: Int): StudentAgeCategory {
            return when {
                age <= 6 -> CHILD
                age in 7..8 -> YOUTH
                else -> PRE_TEEN
            }
        }

        /**
         * Resolves age category from birthdate string (format "YYYY-MM-DD")
         */
        fun fromBirthDate(birthDate: String, anchorYear: Int = 2026): StudentAgeCategory {
            return try {
                val parts = birthDate.trim().split("-")
                if (parts.isNotEmpty()) {
                    val birthYear = parts[0].toIntOrNull() ?: (anchorYear - 7)
                    val calculatedAge = (anchorYear - birthYear).coerceIn(3, 14)
                    fromAge(calculatedAge)
                } else {
                    YOUTH
                }
            } catch (e: Exception) {
                YOUTH
            }
        }
    }
}

/**
 * Type-Safe Navigation Destinations for Student Portal.
 */
sealed interface StudentRoute {
    val routeKey: String
    val category: StudentAgeCategory

    // Child (<6) Routes
    data object ChildPlayground : StudentRoute {
        override val routeKey = "student_child_playground"
        override val category = StudentAgeCategory.CHILD
    }
    data class ChildStorybook(val bookTitle: String = "نور البيان المصور") : StudentRoute {
        override val routeKey = "student_child_storybook"
        override val category = StudentAgeCategory.CHILD
    }
    data object ChildPhonicsLetters : StudentRoute {
        override val routeKey = "student_child_phonics"
        override val category = StudentAgeCategory.CHILD
    }

    // Youth (7-8) Routes
    data object YouthHub : StudentRoute {
        override val routeKey = "student_youth_hub"
        override val category = StudentAgeCategory.YOUTH
    }
    data object YouthSorobanTrainer : StudentRoute {
        override val routeKey = "student_youth_soroban"
        override val category = StudentAgeCategory.YOUTH
    }
    data object YouthMissions : StudentRoute {
        override val routeKey = "student_youth_missions"
        override val category = StudentAgeCategory.YOUTH
    }

    // Pre-Teen (9-14) Routes
    data object PreTeenDashboard : StudentRoute {
        override val routeKey = "student_preteen_dashboard"
        override val category = StudentAgeCategory.PRE_TEEN
    }
    data object PreTeenExams : StudentRoute {
        override val routeKey = "student_preteen_exams"
        override val category = StudentAgeCategory.PRE_TEEN
    }
    data object PreTeenSpeedTelemetry : StudentRoute {
        override val routeKey = "student_preteen_telemetry"
        override val category = StudentAgeCategory.PRE_TEEN
    }
}

/**
 * Stateful Controller managing Student Navigation Stack and Age Tier Transitions in Compose Multiplatform.
 */
@Stable
class StudentNavController(
    initialStudent: StudentProfile
) {
    var studentProfile by mutableStateOf(initialStudent)
        private set

    val currentCategory: StudentAgeCategory
        get() = StudentAgeCategory.fromAge(studentProfile.age)

    private val backStack = mutableStateListOf<StudentRoute>()

    val currentRoute: StudentRoute
        get() = backStack.lastOrNull() ?: defaultRouteFor(currentCategory)

    val canNavigateBack: Boolean
        get() = backStack.size > 1

    init {
        backStack.add(defaultRouteFor(currentCategory))
    }

    fun navigate(destination: StudentRoute) {
        if (backStack.lastOrNull() != destination) {
            backStack.add(destination)
        }
    }

    fun popBackStack(): Boolean {
        return if (backStack.size > 1) {
            backStack.removeAt(backStack.size - 1)
            true
        } else {
            false
        }
    }

    fun switchAgeCategory(newCategory: StudentAgeCategory) {
        val targetAge = when (newCategory) {
            StudentAgeCategory.CHILD -> 5
            StudentAgeCategory.YOUTH -> 8
            StudentAgeCategory.PRE_TEEN -> 11
        }
        val targetAgeTier = when (newCategory) {
            StudentAgeCategory.CHILD -> AgeTier.EARLY_CHILDHOOD
            StudentAgeCategory.YOUTH -> AgeTier.JUNIOR_CHAMPIONS
            StudentAgeCategory.PRE_TEEN -> AgeTier.YOUNG_GENIUSES
        }
        studentProfile = studentProfile.copy(
            age = targetAge,
            ageTier = targetAgeTier,
            avatarEmoji = newCategory.badgeEmoji
        )
        resetToCategoryRoot(newCategory)
    }

    fun switchStudent(newProfile: StudentProfile) {
        studentProfile = newProfile
        resetToCategoryRoot(StudentAgeCategory.fromAge(newProfile.age))
    }

    fun resetToCategoryRoot(category: StudentAgeCategory) {
        backStack.clear()
        backStack.add(defaultRouteFor(category))
    }

    private fun defaultRouteFor(category: StudentAgeCategory): StudentRoute {
        return when (category) {
            StudentAgeCategory.CHILD -> StudentRoute.ChildPlayground
            StudentAgeCategory.YOUTH -> StudentRoute.YouthHub
            StudentAgeCategory.PRE_TEEN -> StudentRoute.PreTeenDashboard
        }
    }
}

@Composable
fun rememberStudentNavController(
    initialStudent: StudentProfile = StudentProfile(
        id = "std-default",
        parentId = "parent-1",
        parentName = "أحمد محمد",
        name = "ياسين أحمد",
        birthDate = "2018-05-12",
        age = 8,
        ageTier = AgeTier.JUNIOR_CHAMPIONS,
        country = PlatformCountry.EGYPT,
        avatarEmoji = "🚀",
        completedLessonsCount = 14,
        currentStreakDays = 5,
        totalXp = 620
    )
): StudentNavController {
    return remember { StudentNavController(initialStudent) }
}

/**
 * Core Compose Multiplatform Navigation Host for Students.
 * Evaluates the student's age category and conditionally renders the age-tailored UI tree:
 * - Child (<6): Playful, big visual cards, phonics soundboards, storybook.
 * - Youth (7-8): Gamified hub, Soroban abacus trainer, XP streaks.
 * - Pre-teen (9-14): Academic schedule, exams, mental arithmetic speed telemetry.
 */
@Composable
fun StudentNavHost(
    navController: StudentNavController,
    onNavigateToClassroom: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenGlobalBook: (String) -> Unit = {}
) {
    // Intercept hardware/system back button
    BackHandler(enabled = navController.canNavigateBack) {
        navController.popBackStack()
    }

    val currentCategory = navController.currentCategory
    val currentRoute = navController.currentRoute
    val student = navController.studentProfile

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Top Navigation Header: Profile Badge, Age Breakdown, and Age Category Switcher
        Surface(
            color = Color.White,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (navController.canNavigateBack) {
                            IconButton(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1F5F9))
                                    .testTag("student_nav_back_button")
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "الرجوع",
                                    tint = OrbitNavy,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Text(
                            text = student.avatarEmoji,
                            fontSize = 24.sp,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(currentCategory.accentColor.copy(alpha = 0.15f))
                                .wrapContentSize(Alignment.Center)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = student.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = OrbitNavy
                            )
                            Text(
                                text = "${currentCategory.titleAr} (${student.age} سنوات) • ${student.country.flag}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    // Active Category Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = currentCategory.accentColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = currentCategory.titleEn,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentCategory.accentColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Age Category Switcher Pills (Child <6, Youth 7-8, Pre-teen 9-14)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StudentAgeCategory.values().forEach { cat ->
                        val isSelected = cat == currentCategory
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) cat.accentColor else Color(0xFFF1F5F9),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { navController.switchAgeCategory(cat) }
                                .testTag("nav_cat_${cat.code}")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(cat.badgeEmoji, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = cat.titleEn,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else OrbitNavy
                                    )
                                }
                                Text(
                                    text = cat.ageRangeDisplay,
                                    fontSize = 9.sp,
                                    color = if (isSelected) Color.White.copy(alpha = 0.9f) else Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }

        // Conditional Rendering of Student UI based on Student's Age Category
        AnimatedContent(
            targetState = currentCategory,
            transitionSpec = {
                fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
            },
            label = "student_category_transition",
            modifier = Modifier.fillMaxSize()
        ) { category ->
            when (category) {
                StudentAgeCategory.CHILD -> {
                    ChildCategoryNavHost(
                        currentRoute = currentRoute,
                        student = student,
                        onNavigate = { navController.navigate(it) },
                        onEnterClass = onNavigateToClassroom,
                        onOpenBook = onOpenGlobalBook
                    )
                }
                StudentAgeCategory.YOUTH -> {
                    YouthCategoryNavHost(
                        currentRoute = currentRoute,
                        student = student,
                        onNavigate = { navController.navigate(it) },
                        onEnterClass = onNavigateToClassroom,
                        onOpenBook = onOpenGlobalBook
                    )
                }
                StudentAgeCategory.PRE_TEEN -> {
                    PreTeenCategoryNavHost(
                        currentRoute = currentRoute,
                        student = student,
                        onNavigate = { navController.navigate(it) },
                        onEnterClass = onNavigateToClassroom,
                        onOpenBook = onOpenGlobalBook
                    )
                }
            }
        }
    }
}

/**
 * 1. CHILD (<6 Years) Navigation Sub-Host:
 * Features: Big visual tap targets, phonics letters, illustrated storybook, audio prompts.
 */
@Composable
private fun ChildCategoryNavHost(
    currentRoute: StudentRoute,
    student: StudentProfile,
    onNavigate: (StudentRoute) -> Unit,
    onEnterClass: () -> Unit,
    onOpenBook: (String) -> Unit
) {
    when (currentRoute) {
        is StudentRoute.ChildStorybook -> {
            ChildStorybookView(
                bookTitle = currentRoute.bookTitle,
                onBack = { /* parent handled */ }
            )
        }
        is StudentRoute.ChildPhonicsLetters -> {
            ChildPhonicsLettersView(
                onBack = { /* parent handled */ }
            )
        }
        else -> {
            ChildPlaygroundView(
                student = student,
                onEnterClass = onEnterClass,
                onOpenStorybook = { onNavigate(StudentRoute.ChildStorybook(it)) },
                onOpenPhonics = { onNavigate(StudentRoute.ChildPhonicsLetters) },
                onOpenBook = onOpenBook
            )
        }
    }
}

/**
 * 2. YOUTH (7-8 Years) Navigation Sub-Host:
 * Features: Streak counters, XP badges, interactive Soroban abacus trainer, quests.
 */
@Composable
private fun YouthCategoryNavHost(
    currentRoute: StudentRoute,
    student: StudentProfile,
    onNavigate: (StudentRoute) -> Unit,
    onEnterClass: () -> Unit,
    onOpenBook: (String) -> Unit
) {
    when (currentRoute) {
        is StudentRoute.YouthSorobanTrainer -> {
            YouthSorobanTrainerView()
        }
        is StudentRoute.YouthMissions -> {
            YouthMissionsView(student = student)
        }
        else -> {
            YouthHubView(
                student = student,
                onEnterClass = onEnterClass,
                onOpenSorobanTrainer = { onNavigate(StudentRoute.YouthSorobanTrainer) },
                onOpenMissions = { onNavigate(StudentRoute.YouthMissions) },
                onOpenBook = onOpenBook
            )
        }
    }
}

/**
 * 3. PRE-TEEN (9-14 Years) Navigation Sub-Host:
 * Features: Academic schedule, speed arithmetic telemetry, digital exams, certificates.
 */
@Composable
private fun PreTeenCategoryNavHost(
    currentRoute: StudentRoute,
    student: StudentProfile,
    onNavigate: (StudentRoute) -> Unit,
    onEnterClass: () -> Unit,
    onOpenBook: (String) -> Unit
) {
    when (currentRoute) {
        is StudentRoute.PreTeenSpeedTelemetry -> {
            PreTeenSpeedTelemetryView()
        }
        is StudentRoute.PreTeenExams -> {
            PreTeenExamsView()
        }
        else -> {
            PreTeenDashboardView(
                student = student,
                onEnterClass = onEnterClass,
                onOpenTelemetry = { onNavigate(StudentRoute.PreTeenSpeedTelemetry) },
                onOpenExams = { onNavigate(StudentRoute.PreTeenExams) },
                onOpenBook = onOpenBook
            )
        }
    }
}

/* =========================================================================================
   SUB-SCREENS FOR CHILD (<6)
   ========================================================================================= */

@Composable
fun ChildPlaygroundView(
    student: StudentProfile,
    onEnterClass: () -> Unit,
    onOpenStorybook: (String) -> Unit,
    onOpenPhonics: () -> Unit,
    onOpenBook: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🎈 مرحباً يا بطل سارة الصغير!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                Spacer(modifier = Modifier.height(4.dp))
                Text("نتعلم الحروف والأرقام باللعب والمرح مع مس سارة 🌟", fontSize = 12.sp, color = Color(0xFFB45309))
            }
        }

        // Giant Classroom Launch Button (Touch Target > 60dp)
        Button(
            onClick = onEnterClass,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OrbitEmerald),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .testTag("child_classroom_enter_button")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🎥", fontSize = 26.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("ادخل حصة مس سارة الآن!", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("فصل تفاعلي مباشر للأطفال 🌱", fontSize = 11.sp, color = Color.White.copy(alpha = 0.9f))
                }
            }
        }

        // Child Activity Cards: Phonics and Storybook
        Text("🎨 استكشف وتعلم:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = OrbitNavy)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E7FF)),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpenPhonics() }
                    .testTag("child_phonics_card")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🔤", fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("أصوات الحروف", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = OrbitIndigo)
                    Text("نور البيان بالصوت", fontSize = 10.sp, color = Color.Gray)
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpenStorybook("كتاب نور البيان المصور للأطفال") }
                    .testTag("child_storybook_card")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📖", fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("كتبي المصورة", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF166534))
                    Text("صور ملونة وواضحة", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun ChildPhonicsLettersView(onBack: () -> Unit) {
    var selectedLetter by remember { mutableStateOf("أ") }
    val letters = listOf(
        Triple("أ", "أرنب 🐇", "أَ - أَرْنَبٌ يَقْفِزُ فِي الْحَقْلِ"),
        Triple("ب", "بطة 🦆", "بَ - بَطَّةٌ تَسْبَحُ فِي الْمَاءِ"),
        Triple("ت", "تفاح 🍎", "تَ - تُفَّاحٌ لَذِيذٌ وَمُفِيدٌ"),
        Triple("ث", "ثعلب 🦊", "ثَ - ثَعْلَبٌ ذَكِيٌّ وَسَرِيعٌ"),
        Triple("ج", "جمل 🐪", "جَ - جَمَلٌ صَبُورٌ فِي الصَّحْرَاءِ"),
        Triple("ح", "حصان 🐎", "حَ - حِصَانٌ قَوِيٌّ وَأَصِيلٌ")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("🔤 لوحة أصوات حروف نور البيان", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OrbitNavy)

        // Selected Letter Showcase Card
        val active = letters.find { it.first == selectedLetter } ?: letters.first()
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFFEF3C7),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(active.first, fontSize = 48.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF92400E))
                Text(active.second, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                Spacer(modifier = Modifier.height(6.dp))
                Text(active.third, fontSize = 14.sp, color = Color(0xFF78350F), fontWeight = FontWeight.SemiBold)
            }
        }

        // Letters Grid (Big Tap Targets)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            letters.forEach { (letter, _, _) ->
                val isSelected = letter == selectedLetter
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) OrbitIndigo else Color.White,
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clickable { selectedLetter = letter }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = letter,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else OrbitNavy
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChildStorybookView(bookTitle: String, onBack: () -> Unit) {
    var pageNumber by remember { mutableStateOf(1) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("📖 $bookTitle", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OrbitNavy)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFDCFCE7),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("الصفحة $pageNumber", fontSize = 14.sp, color = Color(0xFF166534), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = when (pageNumber) {
                        1 -> "أَنَا أَتَعَلَّمُ مَعَ مِسْ سَارَة الْعَرَبِي 🍎"
                        2 -> "الْحُرُوفُ الْمَفْتُوحَةُ: قَ - كَ - تَ - طَ 🌟"
                        else -> "أَرْقَامِي الْجَمِيلَةُ: 1 - 2 - 3 بِالْعَدَّادِ 🧮"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF14532D)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { if (pageNumber > 1) pageNumber-- },
                enabled = pageNumber > 1
            ) {
                Text("السابق")
            }
            Button(
                onClick = { if (pageNumber < 3) pageNumber++ },
                enabled = pageNumber < 3
            ) {
                Text("التالي")
            }
        }
    }
}

/* =========================================================================================
   SUB-SCREENS FOR YOUTH (7-8)
   ========================================================================================= */

@Composable
fun YouthHubView(
    student: StudentProfile,
    onEnterClass: () -> Unit,
    onOpenSorobanTrainer: () -> Unit,
    onOpenMissions: () -> Unit,
    onOpenBook: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Gamified Streak and XP Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFEF3C7),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔥", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("${student.currentStreakDays} أيام متتالية", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF92400E))
                        Text("حافظ على حماسك!", fontSize = 10.sp, color = Color(0xFFB45309))
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFEFF6FF),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⭐", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("${student.totalXp} نقطة XP", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = OrbitIndigo)
                        Text("مستوى البطل الصاعد", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }

        // Live Class Enter Card
        Button(
            onClick = onEnterClass,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OrbitIndigo),
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .testTag("youth_classroom_enter_button")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🚀", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("انضم لحصة الحساب الذهني المباشرة", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Navigation Action Hub
        Text("🎯 أنشطة الأبطال:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = OrbitNavy)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpenSorobanTrainer() }
                    .testTag("youth_soroban_trainer_btn")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🧮", fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("مدرب السوروبان", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = OrbitNavy)
                    Text("تحدي العداد الصيني", fontSize = 9.sp, color = Color.Gray)
                }
            }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpenMissions() }
                    .testTag("youth_missions_btn")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🏆", fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("المهام اليومية", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = OrbitNavy)
                    Text("3 مهام بانتظارك", fontSize = 9.sp, color = OrbitEmerald)
                }
            }
        }
    }
}

@Composable
fun YouthSorobanTrainerView() {
    var beadCount by remember { mutableStateOf(3) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("🧮 محاكي العداد الصيني (السوروبان)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OrbitNavy)
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFFFEF3C7),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("القيمة الحالية على العمود:", fontSize = 12.sp, color = Color(0xFF92400E))
                Text("$beadCount", fontSize = 42.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF78350F))
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(beadCount) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFD97706),
                            modifier = Modifier.size(24.dp)
                        ) {}
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { if (beadCount < 9) beadCount++ },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("+ خرزة")
            }
            Button(
                onClick = { if (beadCount > 0) beadCount-- },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("- خرزة")
            }
        }
    }
}

@Composable
fun YouthMissionsView(student: StudentProfile) {
    val missions = listOf(
        Pair("حضور الحصة المباشرة اليوم", true),
        Pair("حل تدريب العداد الصيني 5 مسائل", true),
        Pair("مراجعة صفحة 4 من كتاب السوروبان", false)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("🏆 مهام وتحديات الأبطال", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OrbitNavy)
        missions.forEach { (task, isDone) ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(task, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = OrbitNavy)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDone) Color(0xFFD1FAE5) else Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = if (isDone) "مكتمل ✓" else "قيد الإنجاز",
                            color = if (isDone) OrbitEmerald else Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

/* =========================================================================================
   SUB-SCREENS FOR PRE-TEEN (9-14)
   ========================================================================================= */

@Composable
fun PreTeenDashboardView(
    student: StudentProfile,
    onEnterClass: () -> Unit,
    onOpenTelemetry: () -> Unit,
    onOpenExams: () -> Unit,
    onOpenBook: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Academic Status Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = OrbitNavy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("المسار الأكاديمي المتقدم 🧠", color = OrbitGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Surface(shape = RoundedCornerShape(6.dp), color = OrbitEmerald) {
                        Text("معدل 98%", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("برنامج الحساب الذهني التخيلي السريع والشهادات المعتمدة", color = Color.White, fontSize = 12.sp)
            }
        }

        // Live Class Launch
        Button(
            onClick = onEnterClass,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OrbitEmerald),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("preteen_classroom_enter_button")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🎥", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("دخول قاعة البث المباشر المتقدمة", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        // Action Hub (Telemetry & Exams)
        Text("📊 الأدوات والتقارير الأكاديمية:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = OrbitNavy)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpenTelemetry() }
                    .testTag("preteen_telemetry_btn")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("⚡", fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("تحليلات السرعة", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = OrbitNavy)
                    Text("قياس دقة وزمن الحل", fontSize = 9.sp, color = Color.Gray)
                }
            }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpenExams() }
                    .testTag("preteen_exams_btn")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📝", fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("الاختبارات والواجبات", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = OrbitNavy)
                    Text("بنك المسائل السريعة", fontSize = 9.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun PreTeenSpeedTelemetryView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("⚡ قياسات سرعة الحساب الذهني التخيلي", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OrbitNavy)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("معدل العمليات في الثانية:", fontSize = 12.sp, color = Color.Gray)
                Text("1.8 عملية / ثانية 🚀", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OrbitEmerald)
                Spacer(modifier = Modifier.height(10.dp))
                Text("دقة الإجابات بالمسائل التخيلية:", fontSize = 12.sp, color = Color.Gray)
                Text("96.5% دقة ممتازة", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = OrbitIndigo)
            }
        }
    }
}

@Composable
fun PreTeenExamsView() {
    val exams = listOf(
        Triple("اختبار السرعة التخيلي للمستوى الثاني", "98/100", "ناجح بامتياز"),
        Triple("مسائل الجمع والطرح الخماسي المركب", "95/100", "ناجح بامتياز"),
        Triple("تطبيق جدول الضرب بالعداد الصيني", "100/100", "درجة كاملة ⭐")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("📝 سجل الاختبارات والواجبات المعتمدة", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OrbitNavy)
        exams.forEach { (title, grade, remark) ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OrbitNavy)
                        Text(remark, fontSize = 10.sp, color = OrbitEmerald)
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEFF6FF)
                    ) {
                        Text(
                            text = grade,
                            fontWeight = FontWeight.Bold,
                            color = OrbitIndigo,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
