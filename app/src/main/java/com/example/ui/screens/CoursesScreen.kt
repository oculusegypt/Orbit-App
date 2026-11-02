package com.example.ui.screens

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesScreen(
    viewModel: OrbitViewModel,
    modifier: Modifier = Modifier
) {
    val courseView by viewModel.courseView.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val selectedCourse by viewModel.selectedCourse.collectAsState()
    val selectedLesson by viewModel.selectedLesson.collectAsState()
    val selectedCertificate by viewModel.selectedCertificate.collectAsState()
    val progressMap by viewModel.progressMap.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Sub-view router
    when (courseView) {
        CourseSubView.CATALOG -> {
            CatalogView(
                courses = courses,
                searchQuery = searchQuery,
                selectedCategory = selectedCategory,
                onSearchChanged = { viewModel.setSearchQuery(it) },
                onCategorySelected = { viewModel.setSelectedCategory(it) },
                onOpenCourse = { viewModel.openCourseDetail(it) },
                onQuickEnroll = { viewModel.enrollInCourse(it) },
                onOpenVerifier = { viewModel.openVerifier() },
                modifier = modifier
            )
        }

        CourseSubView.DETAIL -> {
            selectedCourse?.let { course ->
                CourseDetailView(
                    course = course,
                    progressMap = progressMap,
                    onBack = { viewModel.backToCatalog() },
                    onStartLesson = { lesson -> viewModel.openLessonPlayer(course, lesson) },
                    onEnroll = { viewModel.enrollInCourse(course) },
                    modifier = modifier
                )
            } ?: viewModel.backToCatalog()
        }

        CourseSubView.PLAYER -> {
            if (selectedCourse != null && selectedLesson != null) {
                LessonPlayerScreen(
                    course = selectedCourse!!,
                    lesson = selectedLesson!!,
                    progressMap = progressMap,
                    viewModel = viewModel,
                    onBack = { viewModel.backToCourseDetail() },
                    onCompleteLesson = { viewModel.markLessonCompleted(it) },
                    onOpenQuiz = { viewModel.openQuiz(selectedCourse!!, selectedLesson!!) },
                    onOpenTutor = { viewModel.setShowTutorSheet(true) },
                    modifier = modifier
                )
            } else {
                viewModel.backToCatalog()
            }
        }

        CourseSubView.QUIZ -> {
            if (selectedCourse != null && selectedLesson != null) {
                QuizView(
                    course = selectedCourse!!,
                    lesson = selectedLesson!!,
                    viewModel = viewModel,
                    modifier = modifier
                )
            } else {
                viewModel.backToCatalog()
            }
        }

        CourseSubView.CERTIFICATE -> {
            selectedCertificate?.let { cert ->
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.backToCatalog() }) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "رجوع")
                        }
                        Text("الشهادة الرقمية المعتمدة", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(48.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    CertificateCard(certificate = cert)
                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.openVerifier(cert.verificationCode) },
                        colors = ButtonDefaults.buttonColors(containerColor = OrbitIndigo),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("التحقق من كود الشهادة في السجل العام")
                    }
                }
            } ?: viewModel.backToCatalog()
        }

        CourseSubView.VERIFY -> {
            VerifierView(
                viewModel = viewModel,
                onBack = { viewModel.backToCatalog() },
                modifier = modifier
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogView(
    courses: List<Course>,
    searchQuery: String,
    selectedCategory: String,
    onSearchChanged: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onOpenCourse: (Course) -> Unit,
    onQuickEnroll: (Course) -> Unit,
    onOpenVerifier: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        "الكل",
        "الحساب الذهني وتنمية الذكاء",
        "تأسيس اللغة العربية والقرائية",
        "الرياضيات المدرسية والتفكير المنطقي",
        "إعداد وتأهيل المدربين والمعلمات"
    )

    val filtered = courses.filter { c ->
        val matchesCategory = selectedCategory == "الكل" || c.category.contains(selectedCategory)
        val matchesSearch = searchQuery.isBlank() || c.title.contains(searchQuery, ignoreCase = true) || c.summary.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChanged,
                placeholder = { Text("ابحث في الدورات والمهارات الحسابية...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("courses_search_input"),
                singleLine = true
            )
        }

        // Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategorySelected(cat) },
                        label = { Text(cat, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OrbitIndigo,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Course List
        items(filtered) { course ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenCourse(course) }
                    .testTag("catalog_course_card_${course.id}")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                        AsyncImage(
                            model = course.thumbnailUrl,
                            contentDescription = course.title,
                            modifier = Modifier.fillMaxSize()
                        )
                        Surface(
                            color = OrbitNavy.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(bottomStart = 12.dp),
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Text(
                                text = course.level,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Surface(
                            color = OrbitIndigo.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = course.category,
                                color = OrbitIndigo,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = course.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = course.summary,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Outcomes Preview Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            course.outcomes.take(2).forEach { outcome ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "✓ $outcome",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(12.dp))

                        // Price and Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("رسوم الاشتراك المعتمد", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = course.priceFormatted,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = OrbitIndigo
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { onOpenCourse(course) },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("التفاصيل", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = { onQuickEnroll(course) },
                                    colors = ButtonDefaults.buttonColors(containerColor = OrbitEmerald),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("اشتراك فوري", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
}

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
                    Icon(Icons.Default.Close, contentDescription = "خروج")
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
                            text = if (passed) "مبروك يا بطل! لقد اجتزت الاختبار بنجاح 🌟" else "محاولة جيدة، يمكنك إعادة الاختبار!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("درجتك: $scorePercent%", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = OrbitIndigo)

                        if (passed) {
                            Text(
                                text = "تم إصدار شهادتك الرقمية المعتمدة تلقائياً ويمكنك استعراضها الآن!",
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
                                Text("عرض الشهادة المعتمدة", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.openQuiz(course, lesson) },
                                colors = ButtonDefaults.buttonColors(containerColor = OrbitIndigo),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("إعادة المحاولة")
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
                                    text = if (currentIdx + 1 < questions.size) "السؤال التالي ←" else "إنهاء الاختبار ورصد النتيجة ✓",
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

@Composable
fun VerifierView(
    viewModel: OrbitViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val verifierCode by viewModel.verifierCode.collectAsState()
    val verifiedCert by viewModel.verifiedCertificate.collectAsState()
    var inputCode by remember(verifierCode) { mutableStateOf(verifierCode) }

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
                Text("نظام التحقق من الشهادات المعتمدة", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(48.dp))
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("أدخل كود التحقق الرقمي:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("يمكنك تجربة كود مس سارة الجاهز: CERT-2026-SOROBAN-001", fontSize = 11.sp, color = OrbitIndigo)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = inputCode,
                            onValueChange = { inputCode = it },
                            placeholder = { Text("مثال: CERT-2026-...", fontSize = 12.sp) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        Button(
                            onClick = { viewModel.verifyCertificateInput(inputCode) },
                            colors = ButtonDefaults.buttonColors(containerColor = OrbitIndigo),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("تحقق")
                        }
                    }
                }
            }
        }

        if (verifiedCert != null) {
            item {
                Surface(
                    color = OrbitEmerald.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = OrbitEmerald, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("شهادة أصلية معتمدة وموثقة رسمياً ✓", fontWeight = FontWeight.Bold, color = OrbitEmerald, fontSize = 14.sp)
                            Text("مسجلة في سجل الخريجين لعام 2026 لأكاديمية مس سارة العربي", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                CertificateCard(certificate = verifiedCert!!)
            }
        } else if (inputCode.isNotBlank()) {
            item {
                Surface(
                    color = Color(0xFFFEE2E2),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFDC2626))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("لم يتم العثور على شهادة بهذا الكود. يرجى التحقق من الأحرف والأرقام.", fontSize = 12.sp, color = Color(0xFF991B1B))
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
