package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.*
import com.example.data.repository.OrbitRepository
import com.example.data.repository.OrbitRoomRepository
import com.example.services.AlertNotificationItem
import com.example.services.OrbitNotificationManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.example.data.local.*
import kotlinx.coroutines.Job

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

    // 1-on-1 Private Call & Screen Sharing State
    private var callDurationJob: Job? = null
    private val _activeCall = MutableStateFlow<ActiveCallState?>(null)
    val activeCall: StateFlow<ActiveCallState?> = _activeCall.asStateFlow()

    private val _callHistory = MutableStateFlow<List<CallSessionEntity>>(emptyList())
    val callHistory: StateFlow<List<CallSessionEntity>> = _callHistory.asStateFlow()

    fun initializeServices(context: Context) {
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
    val courses = OrbitRepository.courses
    val books = OrbitRepository.books
    val liveSessions = OrbitRepository.liveSessions
    val assignments = OrbitRepository.assignments
    val submissions = OrbitRepository.submissions
    val certificates = OrbitRepository.certificates
    val enrollments = OrbitRepository.enrollments
    val progressMap = OrbitRepository.progressMap
    val auditLogs = OrbitRepository.auditLogs
    val classroomMessages = OrbitRepository.classroomMessages

    // Mental Math Challenge State
    private val _mentalMathDifficulty = MutableStateFlow(MentalMathDifficulty.BEGINNER)
    val mentalMathDifficulty: StateFlow<MentalMathDifficulty> = _mentalMathDifficulty.asStateFlow()

    private val _mentalMathGameState = MutableStateFlow(MentalMathGameState.IDLE)
    val mentalMathGameState: StateFlow<MentalMathGameState> = _mentalMathGameState.asStateFlow()

    private val _mentalMathSequence = MutableStateFlow<List<Int>>(emptyList())
    val mentalMathSequence: StateFlow<List<Int>> = _mentalMathSequence.asStateFlow()

    private val _mentalMathCurrentNumber = MutableStateFlow<Int?>(null)
    val mentalMathCurrentNumber: StateFlow<Int?> = _mentalMathCurrentNumber.asStateFlow()

    private val _mentalMathUserAnswer = MutableStateFlow("")
    val mentalMathUserAnswer: StateFlow<String> = _mentalMathUserAnswer.asStateFlow()

    private val _mentalMathIsCorrect = MutableStateFlow<Boolean?>(null)
    val mentalMathIsCorrect: StateFlow<Boolean?> = _mentalMathIsCorrect.asStateFlow()

    private val _mentalMathStreak = MutableStateFlow(0)
    val mentalMathStreak: StateFlow<Int> = _mentalMathStreak.asStateFlow()

    private val _mentalMathXp = MutableStateFlow(120)
    val mentalMathXp: StateFlow<Int> = _mentalMathXp.asStateFlow()

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

    // Socratic AI Tutor Messages
    private val _tutorMessages = MutableStateFlow(listOf(
        TutorMessage(
            id = "msg-welcome",
            role = "assistant",
            content = "أهلاً بك يا بطل! 🌟\nأنا المساعد السقراطي الذكي لمس سارة العربي 💡\nهنا بنتعلم سوا بطريقة ممتعة: \"تأسيس أطفال . حساب ذهني\"! لو في خطوة أو مسألة مش واضحة، اسألني وهفكر معاك ونديك تلميحات حلوة توصلك للحل بنفسك! 🧠✨"
        )
    ))
    val tutorMessages: StateFlow<List<TutorMessage>> = _tutorMessages.asStateFlow()

    private val _tutorInput = MutableStateFlow("")
    val tutorInput: StateFlow<String> = _tutorInput.asStateFlow()

    private val _tutorLoading = MutableStateFlow(false)
    val tutorLoading: StateFlow<Boolean> = _tutorLoading.asStateFlow()

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

    // Mental Math Game Logic
    fun setMentalMathDifficulty(difficulty: MentalMathDifficulty) {
        _mentalMathDifficulty.value = difficulty
        _mentalMathGameState.value = MentalMathGameState.IDLE
    }

    fun startMentalMathChallenge() {
        val diff = _mentalMathDifficulty.value
        val seq = mutableListOf<Int>()
        var runningSum = 0
        for (i in 0 until diff.count) {
            var num = (diff.min..diff.max).random()
            if (i > 0 && Math.random() > 0.5 && runningSum - num >= 0) {
                num = -num
            }
            runningSum += num
            seq.add(num)
        }
        _mentalMathSequence.value = seq
        _mentalMathUserAnswer.value = ""
        _mentalMathIsCorrect.value = null
        _mentalMathGameState.value = MentalMathGameState.PLAYING

        viewModelScope.launch {
            for (num in seq) {
                _mentalMathCurrentNumber.value = num
                delay(diff.delayMs)
                _mentalMathCurrentNumber.value = null
                delay(200L)
            }
            _mentalMathGameState.value = MentalMathGameState.ANSWERING
        }
    }

    fun submitMentalMathAnswer(answerStr: String) {
        val expected = _mentalMathSequence.value.sum()
        val parsed = answerStr.toIntOrNull()
        val correct = (parsed == expected)
        _mentalMathIsCorrect.value = correct
        _mentalMathGameState.value = MentalMathGameState.RESULT
        if (correct) {
            _mentalMathStreak.value += 1
            _mentalMathXp.value += 20 * _mentalMathDifficulty.value.stars
        } else {
            _mentalMathStreak.value = 0
        }
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

    // Socratic AI Tutor Logic
    fun setTutorInput(text: String) {
        _tutorInput.value = text
    }

    fun sendTutorQuery(queryText: String? = null) {
        val query = (queryText ?: _tutorInput.value).trim()
        if (query.isBlank()) return

        val userMsg = TutorMessage(
            id = "usr-" + System.currentTimeMillis(),
            role = "user",
            content = query
        )
        _tutorMessages.value = _tutorMessages.value + listOf(userMsg)
        _tutorInput.value = ""
        _tutorLoading.value = true

        viewModelScope.launch {
            delay(800L) // Socratic thinking animation
            val currentLesson = _selectedLesson.value
            val lessonTitle = currentLesson?.title ?: "الحساب الذهني والسوروبان"

            val reply = when {
                query.contains("أصدقاء") || query.contains("5") -> {
                    "أحسنت السؤال يا بطل! 💡\nفي قاعدة أصدقاء العدد 5، لو عندنا 4 وعاوزين نضيف 1، إيه أقرب رقم صديق للـ 5 نقدر نستخدمه؟\nالمفتاح السقراطي: الخرزة العلوية بتساوي 5، يبقى بننزل الـ 5 ونطرح كام؟ شاطر، بنطرح 4! (+1 = +5 - 4). جربها بإيدك على العداد وشوف!"
                }
                query.contains("خرزة") || query.contains("خرزات") -> {
                    "سؤال ذكي جداً! 🧮\nافتكر دايماً: خرزة السماء العلوية فوق العارضة بتساوي 5، والأربع خرزات الأرضيات تحت كل واحدة بتساوي 1.\nأي خرزة بتلمس العارضة الفاصلة، بيتم حسابها فوراً. جرب تحرك خرزة السماء مع خرزتين تحت وشوف الناتج هيكون 7!"
                }
                query.contains("تلميح") || query.contains("حل") -> {
                    "أنا معاك خطوة بخطوة! 🌟\nالتلميح الذهبي لمس سارة: اتخيل العداد قدامك وافتكر حركة الأصابع.. الجمع بإصبع الإبهام نحو العارضة، والطرح بالسبابة! جرب تحسبها الأول بعينك وبعدين بإيدك."
                }
                query.contains("لغز") || query.contains("سؤال") -> {
                    "جاهز للتحدي يا عبقري؟ 🎯\nلو عندك 3 خرزات لامسين العارضة، ونزلت خرزة السماء (5)، وبعدين طرحت 2.. الناتج النهائي في عقلك هيكون كام؟ فكر وقولي!"
                }
                else -> {
                    "فكرة ممتازة وتفكير رائع يا بطل! 💡\nفي منهج مدار لتأسيس الأطفال والحساب الذهني، السر كله في 'الفهم مش الحفظ'.. اتخيل حركة الخرزات السحرية في عقلك وخلّي أصابعك خفيفة وسريعة. إيه الجزء اللي حابب نجربه سوا دلوقتي؟"
                }
            }

            val assistantMsg = TutorMessage(
                id = "tutor-" + System.currentTimeMillis(),
                role = "assistant",
                content = reply,
                citations = listOf(TutorCitation(lessonTitle, "مرجع من منهج مس سارة العربي 2026"))
            )
            _tutorMessages.value = _tutorMessages.value + listOf(assistantMsg)
            _tutorLoading.value = false
        }
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
    // -------------------------------------------------------------
    fun startPrivateCall(targetUser: UserAccount, callType: CallType) {
        val caller = _currentAccount.value ?: _allRegisteredUsers.value.firstOrNull() ?: return
        val newCallId = "call_" + java.util.UUID.randomUUID().toString().take(8)

        _activeCall.value = ActiveCallState(
            callId = newCallId,
            otherUser = targetUser,
            isOutgoing = true,
            callType = callType,
            status = CallStatus.OUTGOING_RINGING,
            isScreenSharing = (callType == CallType.VIDEO_SCREEN)
        )

        // Realistic outgoing ring delay, then connect
        viewModelScope.launch {
            delay(2200L)
            if (_activeCall.value?.callId == newCallId && _activeCall.value?.status == CallStatus.OUTGOING_RINGING) {
                _activeCall.value = _activeCall.value?.copy(status = CallStatus.CONNECTED)
                startCallTimer()
            }
        }
    }

    fun simulateIncomingCall(fromUser: UserAccount, callType: CallType) {
        val newCallId = "call_" + java.util.UUID.randomUUID().toString().take(8)
        _activeCall.value = ActiveCallState(
            callId = newCallId,
            otherUser = fromUser,
            isOutgoing = false,
            callType = callType,
            status = CallStatus.INCOMING_RINGING,
            isScreenSharing = (callType == CallType.VIDEO_SCREEN)
        )
    }

    fun acceptCall() {
        val current = _activeCall.value ?: return
        _activeCall.value = current.copy(status = CallStatus.CONNECTED)
        startCallTimer()
    }

    fun rejectCall() {
        val current = _activeCall.value ?: return
        val caller = _currentAccount.value
        viewModelScope.launch {
            roomRepository?.recordCallSession(
                CallSessionEntity(
                    callId = current.callId,
                    callerId = current.otherUser.id,
                    callerName = current.otherUser.displayName,
                    callerRole = current.otherUser.role.name,
                    callerAvatar = current.otherUser.avatarUrl,
                    receiverId = caller?.id ?: "current",
                    receiverName = caller?.displayName ?: "المستخدم",
                    receiverRole = caller?.role?.name ?: "LEARNER",
                    receiverAvatar = caller?.avatarUrl ?: "",
                    callType = current.callType.name,
                    durationSeconds = 0,
                    hadScreenShare = false,
                    status = "REJECTED"
                )
            )
        }
        _activeCall.value = current.copy(status = CallStatus.ENDED)
        viewModelScope.launch {
            delay(900L)
            _activeCall.value = null
        }
    }

    private fun startCallTimer() {
        callDurationJob?.cancel()
        callDurationJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val call = _activeCall.value
                if (call != null && call.status == CallStatus.CONNECTED) {
                    _activeCall.value = call.copy(durationSeconds = call.durationSeconds + 1)
                } else {
                    break
                }
            }
        }
    }

    fun toggleMute() {
        val call = _activeCall.value ?: return
        _activeCall.value = call.copy(isMuted = !call.isMuted)
    }

    fun toggleCamera() {
        val call = _activeCall.value ?: return
        _activeCall.value = call.copy(isCameraOff = !call.isCameraOff)
    }

    fun flipCamera() {
        val call = _activeCall.value ?: return
        _activeCall.value = call.copy(isFrontCamera = !call.isFrontCamera)
    }

    fun toggleSpeaker() {
        val call = _activeCall.value ?: return
        _activeCall.value = call.copy(isSpeakerOn = !call.isSpeakerOn)
    }

    fun toggleScreenShare() {
        val call = _activeCall.value ?: return
        _activeCall.value = call.copy(isScreenSharing = !call.isScreenSharing)
    }

    fun setScreenShareContent(content: ScreenShareContent) {
        val call = _activeCall.value ?: return
        _activeCall.value = call.copy(screenShareContent = content, isScreenSharing = true)
    }

    fun toggleSorobanUpperBead() {
        val call = _activeCall.value ?: return
        _activeCall.value = call.copy(sorobanUpperBeadActive = !call.sorobanUpperBeadActive)
    }

    fun setSorobanLowerBeads(count: Int) {
        val call = _activeCall.value ?: return
        _activeCall.value = call.copy(sorobanLowerBeadsCount = count.coerceIn(0, 4))
    }

    fun endCall() {
        callDurationJob?.cancel()
        val current = _activeCall.value ?: return
        val caller = _currentAccount.value

        viewModelScope.launch {
            roomRepository?.recordCallSession(
                CallSessionEntity(
                    callId = current.callId,
                    callerId = if (current.isOutgoing) (caller?.id ?: "current") else current.otherUser.id,
                    callerName = if (current.isOutgoing) (caller?.displayName ?: "أنا") else current.otherUser.displayName,
                    callerRole = if (current.isOutgoing) (caller?.role?.name ?: "LEARNER") else current.otherUser.role.name,
                    callerAvatar = if (current.isOutgoing) (caller?.avatarUrl ?: "") else current.otherUser.avatarUrl,
                    receiverId = if (current.isOutgoing) current.otherUser.id else (caller?.id ?: "current"),
                    receiverName = if (current.isOutgoing) current.otherUser.displayName else (caller?.displayName ?: "أنا"),
                    receiverRole = if (current.isOutgoing) current.otherUser.role.name else (caller?.role?.name ?: "LEARNER"),
                    receiverAvatar = if (current.isOutgoing) current.otherUser.avatarUrl else (caller?.avatarUrl ?: ""),
                    callType = current.callType.name,
                    durationSeconds = current.durationSeconds,
                    hadScreenShare = current.isScreenSharing,
                    status = "COMPLETED"
                )
            )
        }

        _activeCall.value = current.copy(status = CallStatus.ENDED)
        viewModelScope.launch {
            delay(1200L)
            _activeCall.value = null
        }
    }
}
