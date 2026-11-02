package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.models.UserRole
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.CourseSubView
import com.example.ui.viewmodel.OrbitViewModel
import com.example.ui.viewmodel.PortalTab

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val context = androidx.compose.ui.platform.LocalContext.current
                val viewModel: OrbitViewModel = viewModel()

                LaunchedEffect(Unit) {
                    viewModel.initializeServices(context.applicationContext)
                }

                // Android 13+ (API 33+) POST_NOTIFICATIONS Permission Flow
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
                    ) { /* permission result handled */ }

                    LaunchedEffect(Unit) {
                        permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                OrbitApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun OrbitApp(viewModel: OrbitViewModel) {
    val activePortal by viewModel.activePortal.collectAsState()
    val courseView by viewModel.courseView.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val showAuthDialog by viewModel.showAuthDialog.collectAsState()
    val showBookingDialog by viewModel.showBookingDialog.collectAsState()
    val showCheckoutDialog by viewModel.showCheckoutDialog.collectAsState()
    val showTutorSheet by viewModel.showTutorSheet.collectAsState()
    val selectedCourse by viewModel.selectedCourse.collectAsState()
    val selectedLesson by viewModel.selectedLesson.collectAsState()
    val tutorMessages by viewModel.tutorMessages.collectAsState()
    val tutorInput by viewModel.tutorInput.collectAsState()
    val tutorLoading by viewModel.tutorLoading.collectAsState()
    val activeCall by viewModel.activeCall.collectAsState()
    val allRegisteredUsers by viewModel.allRegisteredUsers.collectAsState()

    // BackHandler for smooth nested navigation & in-call handling
    BackHandler(enabled = true) {
        if (activeCall != null) {
            viewModel.endCall()
        } else if (activePortal == PortalTab.COURSES && courseView != CourseSubView.CATALOG) {
            when (courseView) {
                CourseSubView.DETAIL -> viewModel.backToCatalog()
                CourseSubView.PLAYER, CourseSubView.QUIZ -> viewModel.backToCourseDetail()
                CourseSubView.CERTIFICATE, CourseSubView.VERIFY -> viewModel.backToCatalog()
                else -> viewModel.backToCatalog()
            }
        } else if (activePortal != PortalTab.HOME) {
            viewModel.setPortal(PortalTab.HOME)
        }
    }

    // If a 1-on-1 private call is active (ringing, connected, or ended), display full-screen call room
    if (activeCall != null) {
        PrivateCallScreen(viewModel = viewModel)
        return
    }

    Scaffold(
        topBar = {
            if (activePortal != PortalTab.CLASSROOM) {
                OrbitTopBar(
                    currentUser = currentUser,
                    onOpenAuth = { viewModel.setShowAuthDialog(true) },
                    onOpenBooking = { viewModel.setShowBookingDialog(true) },
                    onOpenVerifier = { viewModel.openVerifier() }
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                // 1. Home
                NavigationBarItem(
                    selected = activePortal == PortalTab.HOME,
                    onClick = { viewModel.setPortal(PortalTab.HOME) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "الرئيسية") },
                    label = { Text("الرئيسية", fontSize = 10.sp, fontWeight = if (activePortal == PortalTab.HOME) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OrbitIndigo,
                        selectedTextColor = OrbitIndigo,
                        indicatorColor = OrbitIndigo.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_home")
                )

                // 2. Courses
                NavigationBarItem(
                    selected = activePortal == PortalTab.COURSES,
                    onClick = { viewModel.setPortal(PortalTab.COURSES) },
                    icon = { Icon(Icons.Default.School, contentDescription = "الدورات") },
                    label = { Text("الدورات", fontSize = 10.sp, fontWeight = if (activePortal == PortalTab.COURSES) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OrbitIndigo,
                        selectedTextColor = OrbitIndigo,
                        indicatorColor = OrbitIndigo.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_courses")
                )

                // 3. Connect (1-on-1 Private Call & Screen Sharing)
                NavigationBarItem(
                    selected = activePortal == PortalTab.CONNECT,
                    onClick = { viewModel.setPortal(PortalTab.CONNECT) },
                    icon = { Icon(Icons.Default.PhoneInTalk, contentDescription = "اتصال خاص") },
                    label = { Text("اتصال خاص 📞", fontSize = 10.sp, fontWeight = if (activePortal == PortalTab.CONNECT) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OrbitGoldDark,
                        selectedTextColor = OrbitGoldDark,
                        indicatorColor = OrbitGold.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_item_connect")
                )

                // 4. Classroom
                NavigationBarItem(
                    selected = activePortal == PortalTab.CLASSROOM,
                    onClick = { viewModel.setPortal(PortalTab.CLASSROOM) },
                    icon = { Icon(Icons.Default.Videocam, contentDescription = "الفصل الحي") },
                    label = { Text("الفصل الحي", fontSize = 10.sp, fontWeight = if (activePortal == PortalTab.CLASSROOM) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OrbitRose,
                        selectedTextColor = OrbitRose,
                        indicatorColor = OrbitRose.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_classroom")
                )

                // 5. Learner Portal (بوابتي)
                NavigationBarItem(
                    selected = activePortal == PortalTab.LEARNER,
                    onClick = { viewModel.setPortal(PortalTab.LEARNER) },
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "بوابتي") },
                    label = { Text("بوابتي", fontSize = 10.sp, fontWeight = if (activePortal == PortalTab.LEARNER) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OrbitIndigo,
                        selectedTextColor = OrbitIndigo,
                        indicatorColor = OrbitIndigo.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_learner")
                )

                // 6. Role-dependent Portal (المعلمة أو الإدارة)
                if (currentUser?.role == UserRole.INSTRUCTOR) {
                    NavigationBarItem(
                        selected = activePortal == PortalTab.INSTRUCTOR,
                        onClick = { viewModel.setPortal(PortalTab.INSTRUCTOR) },
                        icon = { Icon(Icons.Default.Psychology, contentDescription = "المعلمة") },
                        label = { Text("المعلمة 💡", fontSize = 10.sp, fontWeight = if (activePortal == PortalTab.INSTRUCTOR) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OrbitGoldDark,
                            selectedTextColor = OrbitGoldDark,
                            indicatorColor = OrbitGold.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag("nav_item_instructor")
                    )
                } else if (currentUser?.role == UserRole.ADMIN) {
                    NavigationBarItem(
                        selected = activePortal == PortalTab.ADMIN,
                        onClick = { viewModel.setPortal(PortalTab.ADMIN) },
                        icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "الإدارة") },
                        label = { Text("الإدارة", fontSize = 10.sp, fontWeight = if (activePortal == PortalTab.ADMIN) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFDC2626),
                            selectedTextColor = Color(0xFFDC2626),
                            indicatorColor = Color(0xFFFEE2E2)
                        ),
                        modifier = Modifier.testTag("nav_item_admin")
                    )
                }
            }
        },
        floatingActionButton = {
            // Socratic AI Tutor Quick Floating Button
            FloatingActionButton(
                onClick = { viewModel.setShowTutorSheet(true) },
                containerColor = OrbitGold,
                contentColor = OrbitNavyDark,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .testTag("fab_socratic_tutor")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "المساعد السقراطي لمس سارة",
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activePortal) {
                PortalTab.HOME -> HomeScreen(viewModel = viewModel)
                PortalTab.COURSES -> CoursesScreen(viewModel = viewModel)
                PortalTab.LIBRARY -> LibraryScreen(viewModel = viewModel)
                PortalTab.CONNECT -> PrivateConnectScreen(viewModel = viewModel)
                PortalTab.CLASSROOM -> VirtualClassroom(viewModel = viewModel)
                PortalTab.LEARNER -> LearnerScreen(viewModel = viewModel)
                PortalTab.INSTRUCTOR -> InstructorScreen(viewModel = viewModel)
                PortalTab.ADMIN -> AdminScreen(viewModel = viewModel)
            }
        }
    }

    // Modal Dialogs
    if (showAuthDialog) {
        AuthDialog(
            currentUser = currentUser,
            allRegisteredUsers = allRegisteredUsers,
            onSelectUser = { viewModel.switchUser(it) },
            onLogin = { email, pass, cb -> viewModel.loginUser(email, pass, cb) },
            onRegister = { name, email, pass, role, phone, bio, cb ->
                viewModel.registerUser(name, email, pass, role, phone, bio, cb)
            },
            onDismiss = { viewModel.setShowAuthDialog(false) }
        )
    }

    if (showBookingDialog) {
        BookingConsultationDialog(
            onDismiss = { viewModel.setShowBookingDialog(false) }
        )
    }

    if (showCheckoutDialog && selectedCourse != null) {
        CheckoutDialog(
            course = selectedCourse!!,
            onConfirm = { viewModel.completeCheckout(it) },
            onDismiss = { viewModel.setShowCheckoutDialog(false) }
        )
    }

    if (showTutorSheet) {
        SocraticTutorSheet(
            lessonTitle = selectedLesson?.title ?: selectedCourse?.title ?: "الحساب الذهني وتنمية الذكاء",
            messages = tutorMessages,
            inputText = tutorInput,
            isLoading = tutorLoading,
            onInputChanged = { viewModel.setTutorInput(it) },
            onSendMessage = { viewModel.sendTutorQuery(it) },
            onDismiss = { viewModel.setShowTutorSheet(false) }
        )
    }
}
