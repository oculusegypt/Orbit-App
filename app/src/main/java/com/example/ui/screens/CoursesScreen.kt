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
import com.example.ui.screens.courses.*

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
