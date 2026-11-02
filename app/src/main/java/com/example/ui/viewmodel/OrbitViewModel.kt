package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.*
import com.example.data.repository.OrbitRepository
import com.example.data.repository.OrbitRoomRepository
import com.example.services.AlertNotificationItem
import com.example.services.OrbitNotificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.example.data.local.*

enum class PortalTab(val labelAr: String) {
    HOME("الرئيسية"),
    COURSES("الدورات"),
    LIBRARY("المكتبة"),
    CLASSROOM("الفصل الحي"),
    CONNECT("اتصال خاص"),
    LEARNER("بوابتي"),
    INSTRUCTOR("المعلمة"),
    ADMIN("الإدارة")
}

enum class CourseSubView {
    CATALOG,
    DETAIL,
    PLAYER,
    QUIZ,
    CERTIFICATE,
    VERIFY
}

enum class MentalMathDifficulty(val labelAr: String, val count: Int, val min: Int, val max: Int, val delayMs: Long, val stars: Int) {
    BEGINNER("العصافير (6-8 سنوات)", 3, 1, 5, 1200L, 1),
    CHAMPION("الأبطال (8-11 سنة)", 5, 1, 9, 900L, 2),
    GENIUS("العباقرة (11-14 سنة)", 6, 2, 15, 700L, 3)
}

enum class MentalMathGameState {
    IDLE,
    PLAYING,
    ANSWERING,
    RESULT
}

class OrbitViewModel : ViewModel() {

    // Real Gemini AI engine (falls back to offline rule-based replies when no key is configured)
    private var geminiClient: com.example.core.GeminiClient? = null

    // Room Database Repository & Offline Cache State
    private var roomRepository: OrbitRoomRepository? = null

    private val _isOfflineCached = MutableStateFlow(false)
    val isOfflineCached: StateFlow<Boolean> = _isOfflineCached.asStateFlow()

    // Firebase Cloud Messaging (FCM) reactive flows
    val fcmToken: StateFlow<String> = OrbitNotificationManager.fcmToken
    val alertsHistory: StateFlow<List<AlertNotificationItem>> = OrbitNotificationManager.alertsHistory
    val subscribedTopics: StateFlow<Set<String>> = OrbitNotificationManager.subscribedTopics

    // Real User Accounts from Room SQLite
    private val _allRegisteredUsers = MutableStateFlow<List<UserAccount>>(emptyList())
    val allRegisteredUsers: StateFlow<List<UserAccount>> = _allRegisteredUsers.asStateFlow()

    private val _currentAccount = MutableStateFlow<UserAccount?>(null)
    val currentAccount: StateFlow<UserAccount?> = _currentAccount.asStateFlow()

    // 1-on-1 Private Call & Screen Sharing State (delegated controller)
    private val callController = PrivateCallController(
        scope = viewModelScope,
        currentUserProvider = { _currentAccount.value },
        fallbackUserProvider = { _allRegisteredUsers.value.firstOrNull() },
        onCallRecorded = { session -> roomRepository?.recordCallSession(session) }
    )
    val activeCall: StateFlow<ActiveCallState?> = callController.activeCall

    private val _callHistory = MutableStateFlow<List<CallSessionEntity>>(emptyList())
    val callHistory: StateFlow<List<CallSessionEntity>> = _callHistory.asStateFlow()

    fun initializeServices(context: Context) {
        // Wire the real Gemini client before any tutor query can be issued.
        val aiClient = com.example.core.GeminiClient(context.applicationContext)
        geminiClient = aiClient
        tutorController.attachGeminiClient(aiClient)

        val repo = OrbitRoomRepository.getInstance(context)
        roomRepository = repo
        viewModelScope.launch {
            repo.isOfflineReady.collect { ready ->
                _isOfflineCached.value = ready
            }
        }
        viewModelScope.launch {
            repo.getAllUserAccountsFlow().collect { accounts ->
                _allRegisteredUsers.value = accounts
                if (_currentAccount.value == null && accounts.isNotEmpty()) {
                    val defaultUser = accounts.firstOrNull { it.role == UserRole.LEARNER } ?: accounts.first()
                    _currentAccount.value = defaultUser
                    _currentUser.value = defaultUser.toUser()
                }
            }
        }
        viewModelScope.launch {
            repo.getCallHistoryFlow().collect { calls ->
                _callHistory.value = calls
            }
        }
        OrbitNotificationManager.initialize(context)
    }

    // Current User
    private val _currentUser = MutableStateFlow<User?>(OrbitRepository.PRESET_USERS[0])
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Navigation state
    private val _activePortal = MutableStateFlow(PortalTab.HOME)
    val activePortal: StateFlow<PortalTab> = _activePortal.asStateFlow()

    private val _courseView = MutableStateFlow(CourseSubView.CATALOG)
    val courseView: StateFlow<CourseSubView> = _courseView.asStateFlow()

    // Selections
    private val _selectedCourse = MutableStateFlow<Course?>(null)
    val selectedCourse: StateFlow<Course?> = _selectedCourse.asStateFlow()

    private val _selectedLesson = MutableStateFlow<Lesson?>(null)
    val selectedLesson: StateFlow<Lesson?> = _selectedLesson.asStateFlow()

    private val _selectedCertificate = MutableStateFlow<Certificate?>(null)
    val selectedCertificate: StateFlow<Certificate?> = _selectedCertificate.asStateFlow()

    private val _verifierCode = MutableStateFlow("")
    val verifierCode: StateFlow<String> = _verifierCode.asStateFlow()

    private val _verifiedCertificate = MutableStateFlow<Certificate?>(null)
    val verifiedCertificate: StateFlow<Certificate?> = _verifiedCertificate.asStateFlow()

    // Search and Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("الكل")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Dialogs
    private val _showAuthDialog = MutableStateFlow(false)
    val showAuthDialog: StateFlow<Boolean> = _showAuthDialog.asStateFlow()

    private val _showBookingDialog = MutableStateFlow(false)
    val showBookingDialog: StateFlow<Boolean> = _showBookingDialog.asStateFlow()

    private val _showCheckoutDialog = MutableStateFlow(false)
    val showCheckoutDialog: StateFlow<Boolean> = _showCheckoutDialog.asStateFlow()

    private val _showTutorSheet = MutableStateFlow(false)
    val showTutorSheet: StateFlow<Boolean> = _showTutorSheet.asStateFlow()

    // Repository Flows
    val courses get() = OrbitRepository.courses
    val books get() = OrbitRepository.books
    val liveSessions get() = OrbitRepository.liveSessions
    val assignments get() = OrbitRepository.assignments
    val submissions get() = OrbitRepository.submissions
    val certificates get() = OrbitRepository.certificates
    val enrollments get() = OrbitRepository.enrollments
    val progressMap get() = OrbitRepository.progressMap
    val auditLogs get() = OrbitRepository.auditLogs
    val classroomMessages get() = OrbitRepository.classroomMessages

    // Mental Math Challenge State (delegated controller)
    private val mentalMathController = MentalMathController()
    val mentalMathDifficulty: StateFlow<MentalMathDifficulty> = mentalMathController.difficulty
    val mentalMathGameState: StateFlow<MentalMathGameState> = mentalMathController.gameState
    val mentalMathSequence: StateFlow<List<Int>> = mentalMathController.sequence
    val mentalMathCurrentNumber: StateFlow<Int?> = mentalMathController.currentNumber
    val mentalMathUserAnswer: StateFlow<String> = mentalMathController.userAnswer
    val mentalMathIsCorrect: StateFlow<Boolean?> = mentalMathController.isCorrect
    val mentalMathStreak: StateFlow<Int> = mentalMathController.streak
    val mentalMathXp: StateFlow<Int> = mentalMathController.xp

    // Quiz State
    private val _currentQuizIndex = MutableStateFlow(0)
    val currentQuizIndex: StateFlow<Int> = _currentQuizIndex.asStateFlow()

    private val _quizSelectedOption = MutableStateFlow<Int?>(null)
    val quizSelectedOption: StateFlow<Int?> = _quizSelectedOption.asStateFlow()

    private val _quizAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val quizAnswers: StateFlow<Map<Int, Int>> = _quizAnswers.asStateFlow()

    private val _quizSubmitted = MutableStateFlow(false)
    val quizSubmitted: StateFlow<Boolean> = _quizSubmitted.asStateFlow()

    private val _quizScorePercent = MutableStateFlow(0)
    val quizScorePercent: StateFlow<Int> = _quizScorePercent.asStateFlow()

    // Live Classroom Controls
    private val _classroomMuted = MutableStateFlow(false)
    val classroomMuted: StateFlow<Boolean> = _classroomMuted.asStateFlow()

    private val _classroomVideoOn = MutableStateFlow(true)
    val classroomVideoOn: StateFlow<Boolean> = _classroomVideoOn.asStateFlow()

    private val _classroomHandRaised = MutableStateFlow(false)
    val classroomHandRaised: StateFlow<Boolean> = _classroomHandRaised.asStateFlow()

    private val _classroomScreenShared = MutableStateFlow(true)
    val classroomScreenShared: StateFlow<Boolean> = _classroomScreenShared.asStateFlow()

    private val _classroomChatText = MutableStateFlow("")
    val classroomChatText: StateFlow<String> = _classroomChatText.asStateFlow()

    // Socratic AI Tutor (delegated controller; backed by real Gemini once initializeServices runs)
    private val tutorController = SocraticTutorController()
    val tutorMessages: StateFlow<List<TutorMessage>> = tutorController.messages
    val tutorInput: StateFlow<String> = tutorController.input
    val tutorLoading: StateFlow<Boolean> = tutorController.loading

    /** Exposes whether the live AI engine (Gemini) or the offline fallback is answering. */
    val isAiTutorLive: Boolean get() = tutorController.isAiEngineActive

    // Navigation methods
    fun setPortal(portal: PortalTab) {
        _activePortal.value = portal
        if (portal == PortalTab.COURSES && _selectedCourse.value == null) {
            _courseView.value = CourseSubView.CATALOG
        }
    }

    fun openCourseDetail(course: Course) {
        _selectedCourse.value = course
        _courseView.value = CourseSubView.DETAIL
        _activePortal.value = PortalTab.COURSES
    }

    fun openLessonPlayer(course: Course, lesson: Lesson) {
        _selectedCourse.value = course
        _selectedLesson.value = lesson
        _courseView.value = CourseSubView.PLAYER
        _activePortal.value = PortalTab.COURSES
    }

    fun openQuiz(course: Course, lesson: Lesson) {
        _selectedCourse.value = course
        _selectedLesson.value = lesson
        _currentQuizIndex.value = 0
        _quizSelectedOption.value = null
        _quizAnswers.value = emptyMap()
        _quizSubmitted.value = false
        _quizScorePercent.value = 0
        _courseView.value = CourseSubView.QUIZ
        _activePortal.value = PortalTab.COURSES
    }

    fun openCertificate(cert: Certificate) {
        _selectedCertificate.value = cert
        _courseView.value = CourseSubView.CERTIFICATE
        _activePortal.value = PortalTab.COURSES
    }

    fun openVerifier(code: String = "") {
        _verifierCode.value = code
        if (code.isNotBlank()) {
            _verifiedCertificate.value = OrbitRepository.verifyCertificate(code)
        } else {
            _verifiedCertificate.value = null
        }
        _courseView.value = CourseSubView.VERIFY
        _activePortal.value = PortalTab.COURSES
    }

    fun backToCatalog() {
        _selectedCourse.value = null
        _selectedLesson.value = null
        _courseView.value = CourseSubView.CATALOG
    }

    fun backToCourseDetail() {
        _selectedLesson.value = null
        _courseView.value = CourseSubView.DETAIL
    }

    // Search & Filter
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(cat: String) {
        _selectedCategory.value = cat
    }

    // Dialog controls
    fun setShowAuthDialog(show: Boolean) {
        _showAuthDialog.value = show
    }

    fun setShowBookingDialog(show: Boolean) {
        _showBookingDialog.value = show
    }

    fun setShowCheckoutDialog(show: Boolean) {
        _showCheckoutDialog.value = show
    }

    fun setShowTutorSheet(show: Boolean) {
        _showTutorSheet.value = show
    }

    // User management
    fun switchUser(user: User?) {
        _currentUser.value = user
        if (user != null) {
            when (user.role) {
                UserRole.INSTRUCTOR -> _activePortal.value = PortalTab.INSTRUCTOR
                UserRole.ADMIN -> _activePortal.value = PortalTab.ADMIN
                UserRole.LEARNER -> _activePortal.value = PortalTab.LEARNER
            }
        } else {
            _activePortal.value = PortalTab.HOME
        }
    }

    // Enrollment & Checkout
    fun enrollInCourse(course: Course) {
        val user = _currentUser.value
        if (user == null) {
            _showAuthDialog.value = true
            return
        }
        if (course.priceMinor == 0) {
            OrbitRepository.enrollCourse(user.id, course.id, "free")
            openCourseDetail(course)
        } else {
            _selectedCourse.value = course
            _showCheckoutDialog.value = true
        }
    }

    fun completeCheckout(course: Course) {
        val user = _currentUser.value ?: return
        OrbitRepository.enrollCourse(user.id, course.id, "purchase")
        _showCheckoutDialog.value = false
        openCourseDetail(course)
    }

    // Lesson completion & Room Database Offline Caching
    fun markLessonCompleted(lessonId: String) {
        val user = _currentUser.value ?: return
        val course = _selectedCourse.value ?: return
        OrbitRepository.completeLesson(user.id, lessonId, course.id)
        viewModelScope.launch {
            roomRepository?.markLessonCompleted(user.id, lessonId, course.id)
        }
    }

    fun saveLessonWatchProgress(lessonId: String, courseId: String, seconds: Int) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            roomRepository?.saveWatchProgress(user.id, lessonId, courseId, seconds)
        }
    }

    /**
     * Resumes course from cached Room progress so student can learn without internet
     */
    fun resumeOfflineCourse(course: Course) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val (lessonId, _) = roomRepository?.getResumePoint(user.id, course.id) ?: Pair(null, 0)
            val allLessons = course.sections.flatMap { it.lessons }
            val targetLesson = allLessons.find { it.id == lessonId } ?: allLessons.firstOrNull()
            if (targetLesson != null) {
                _selectedCourse.value = course
                _selectedLesson.value = targetLesson
                _courseView.value = CourseSubView.PLAYER
                _activePortal.value = PortalTab.COURSES
            }
        }
    }

    // Firebase Cloud Messaging Alert Triggers
    fun triggerLiveClassAlert(context: Context) {
        val course = _selectedCourse.value
        OrbitNotificationManager.triggerLiveClassAlert(
            context = context,
            courseTitle = course?.title ?: "دبلوم الحساب الذهني والسوروبان للأطفال",
            instructorName = course?.instructor?.name ?: "أ / سارة العربي"
        )
    }

    fun triggerModuleReminderAlert(context: Context) {
        val course = _selectedCourse.value
        val sectionTitle = course?.sections?.firstOrNull()?.title ?: "الوحدة الأولى: التعرف على العداد الصيني"
        OrbitNotificationManager.triggerModuleReminderAlert(
            context = context,
            moduleTitle = sectionTitle,
            remainingLessons = 2
        )
    }

    // Quiz answers & Certificate issuance
    fun selectQuizOption(index: Int) {
        _quizSelectedOption.value = index
    }

    fun confirmQuizAnswer() {
        val selected = _quizSelectedOption.value ?: return
        val currentIdx = _currentQuizIndex.value
        val updatedAnswers = _quizAnswers.value.toMutableMap()
        updatedAnswers[currentIdx] = selected
        _quizAnswers.value = updatedAnswers

        val lesson = _selectedLesson.value ?: return
        val quiz = lesson.quiz ?: return
        val questions = quiz.questions

        if (currentIdx + 1 < questions.size) {
            _currentQuizIndex.value = currentIdx + 1
            _quizSelectedOption.value = updatedAnswers[currentIdx + 1]
        } else {
            // Finish quiz
            var correctCount = 0
            questions.forEachIndexed { i, q ->
                if (updatedAnswers[i] == q.correctIndex) {
                    correctCount++
                }
            }
            val percent = if (questions.isNotEmpty()) (correctCount * 100) / questions.size else 0
            _quizScorePercent.value = percent
            _quizSubmitted.value = true

            if (percent >= quiz.passingScore) {
                val user = _currentUser.value
                val course = _selectedCourse.value
                if (user != null && course != null) {
                    val cert = OrbitRepository.issueCertificate(
                        courseId = course.id,
                        courseTitle = course.title,
                        userId = user.id,
                        userName = user.displayName,
                        grade = percent,
                        skills = course.outcomes.take(4)
                    )
                    _selectedCertificate.value = cert
                }
            }
        }
    }

    // Certificate verification
    fun verifyCertificateInput(code: String) {
        _verifierCode.value = code
        _verifiedCertificate.value = OrbitRepository.verifyCertificate(code)
    }

    // Mental Math Game Logic (delegates to MentalMathController)
    fun setMentalMathDifficulty(difficulty: MentalMathDifficulty) {
        mentalMathController.setDifficulty(difficulty)
    }

    fun startMentalMathChallenge() {
        mentalMathController.startChallenge(viewModelScope)
    }

    fun submitMentalMathAnswer(answerStr: String) {
        mentalMathController.submitAnswer(answerStr)
    }

    // Classroom
    fun toggleClassroomMute() {
        _classroomMuted.value = !_classroomMuted.value
    }

    fun setClassroomMuted(muted: Boolean) {
        _classroomMuted.value = muted
    }

    fun toggleClassroomVideo() {
        _classroomVideoOn.value = !_classroomVideoOn.value
    }

    fun setClassroomVideo(videoOn: Boolean) {
        _classroomVideoOn.value = videoOn
    }

    fun toggleClassroomHandRaise() {
        _classroomHandRaised.value = !_classroomHandRaised.value
    }

    fun toggleClassroomScreenShare() {
        _classroomScreenShared.value = !_classroomScreenShared.value
    }

    fun setClassroomScreenShared(shared: Boolean) {
        _classroomScreenShared.value = shared
    }

    fun setClassroomChatText(t: String) {
        _classroomChatText.value = t
    }

    fun sendClassroomMessage(isQuestion: Boolean = false) {
        val text = _classroomChatText.value.trim()
        if (text.isBlank()) return
        val user = _currentUser.value ?: return
        OrbitRepository.sendClassroomMessage(
            senderId = user.id,
            senderName = user.displayName,
            role = if (user.role == UserRole.INSTRUCTOR) "instructor" else "learner",
            text = text,
            isQuestion = isQuestion
        )
        _classroomChatText.value = ""
    }

    fun upvoteClassroomQuestion(msgId: String) {
        OrbitRepository.upvoteQuestion(msgId)
    }

    // Socratic AI Tutor Logic (delegates to SocraticTutorController)
    fun setTutorInput(text: String) {
        tutorController.setInput(text)
    }

    fun sendTutorQuery(queryText: String? = null) {
        tutorController.sendQuery(
            scope = viewModelScope,
            queryText = queryText,
            lessonTitleProvider = {
                _selectedLesson.value?.title ?: "الحساب الذهني والسوروبان"
            }
        )
    }

    // Submissions
    fun submitHomework(assignmentId: String, assignmentTitle: String, text: String, repoUrl: String?) {
        val user = _currentUser.value ?: return
        OrbitRepository.submitAssignment(assignmentId, assignmentTitle, user.id, user.displayName, text, repoUrl)
    }

    fun gradeStudentSubmission(subId: String, grade: Int, feedback: String) {
        val user = _currentUser.value ?: return
        OrbitRepository.gradeSubmission(subId, grade, feedback, user.displayName)
    }

    // -------------------------------------------------------------
    // Real User Management & Authentication (Room Database)
    // -------------------------------------------------------------
    fun loginUser(email: String, pass: String, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val repo = roomRepository
            if (repo == null) {
                callback(false, "قاعدة البيانات غير مهيأة")
                return@launch
            }
            val account = repo.authenticateUser(email, pass)
            if (account != null) {
                _currentAccount.value = account
                _currentUser.value = account.toUser()
                callback(true, "تم تسجيل الدخول بنجاح")
            } else {
                callback(false, "البريد الإلكتروني أو كلمة المرور غير صحيحة")
            }
        }
    }

    fun registerUser(
        name: String,
        email: String,
        pass: String,
        role: UserRole,
        phone: String,
        bioOrGrade: String,
        callback: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val repo = roomRepository
            if (repo == null) {
                callback(false, "قاعدة البيانات غير مهيأة")
                return@launch
            }
            val result = repo.registerNewAccount(name, email, pass, role, phone, bioOrGrade)
            result.onSuccess { account ->
                _currentAccount.value = account
                _currentUser.value = account.toUser()
                callback(true, "تم إنشاء الحساب وحفظه في قاعدة البيانات بنجاح")
            }.onFailure { err ->
                callback(false, err.message ?: "فشل إنشاء الحساب")
            }
        }
    }

    fun switchUserAccount(account: UserAccount) {
        _currentAccount.value = account
        _currentUser.value = account.toUser()
        viewModelScope.launch {
            roomRepository?.setUserOnlineStatus(account.id, true)
        }
    }

    // -------------------------------------------------------------
    // 1-on-1 Private Video/Audio Calling & Screen Sharing
    // (delegates to PrivateCallController)
    // -------------------------------------------------------------
    fun startPrivateCall(targetUser: UserAccount, callType: CallType) {
        callController.startOutgoingCall(targetUser, callType)
    }

    fun simulateIncomingCall(fromUser: UserAccount, callType: CallType) {
        callController.simulateIncomingCall(fromUser, callType)
    }

    fun acceptCall() {
        callController.accept()
    }

    fun rejectCall() {
        callController.reject()
    }

    fun toggleMute() {
        callController.toggleMute()
    }

    fun toggleCamera() {
        callController.toggleCamera()
    }

    fun flipCamera() {
        callController.flipCamera()
    }

    fun toggleSpeaker() {
        callController.toggleSpeaker()
    }

    fun toggleScreenShare() {
        callController.toggleScreenShare()
    }

    fun setScreenShareContent(content: ScreenShareContent) {
        callController.setScreenShareContent(content)
    }

    fun toggleSorobanUpperBead() {
        callController.toggleSorobanUpperBead()
    }

    fun setSorobanLowerBeads(count: Int) {
        callController.setSorobanLowerBeads(count)
    }

    fun endCall() {
        callController.end()
    }
}
