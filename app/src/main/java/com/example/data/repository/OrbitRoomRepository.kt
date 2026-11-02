package com.example.data.repository
import com.example.core.OrbitTimeFormatter

import android.content.Context
import com.example.data.local.*
import com.example.data.models.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date

class OrbitRoomRepository(
    private val courseDao: CourseDao,
    private val lessonProgressDao: LessonProgressDao,
    private val userAccountDao: UserAccountDao? = null,
    private val callSessionDao: CallSessionDao? = null
) {
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    private val _isOfflineReady = MutableStateFlow(false)
    val isOfflineReady: StateFlow<Boolean> = _isOfflineReady.asStateFlow()

    init {
        repositoryScope.launch {
            seedFromRemoteOrCache()
            seedRealUserAccounts()
            _isOfflineReady.value = true
        }
    }

    /**
     * Seeds initial course metadata and initial progress to Room SQLite database
     * so students can resume learning without requiring an active internet connection.
     */
    suspend fun seedFromRemoteOrCache() {
        val count = courseDao.getCoursesCount()
        if (count == 0) {
            val courses = OrbitRepository.courses.value
            val courseEntities = mutableListOf<CourseEntity>()
            val sectionEntities = mutableListOf<CourseSectionEntity>()
            val lessonEntities = mutableListOf<LessonEntity>()

            courses.forEach { course ->
                courseEntities.add(
                    CourseEntity(
                        id = course.id,
                        slug = course.slug,
                        title = course.title,
                        summary = course.summary,
                        languageCode = course.languageCode,
                        level = course.level,
                        category = course.category,
                        status = course.status,
                        priceMinor = course.priceMinor,
                        currency = course.currency,
                        rating = course.rating,
                        reviewsCount = course.reviewsCount,
                        durationHours = course.durationHours,
                        thumbnailUrl = course.thumbnailUrl,
                        instructorId = course.instructor.id,
                        instructorName = course.instructor.name,
                        instructorTitle = course.instructor.title,
                        instructorAvatarUrl = course.instructor.avatarUrl,
                        outcomesJoined = course.outcomes.joinToString("\n"),
                        prerequisitesJoined = course.prerequisites.joinToString("\n"),
                        createdAt = course.createdAt,
                        publishedAt = course.publishedAt
                    )
                )

                course.sections.forEach { section ->
                    sectionEntities.add(
                        CourseSectionEntity(
                            id = section.id,
                            courseId = course.id,
                            title = section.title,
                            position = section.position
                        )
                    )

                    section.lessons.forEach { lesson ->
                        lessonEntities.add(
                            LessonEntity(
                                id = lesson.id,
                                sectionId = section.id,
                                courseId = course.id,
                                title = lesson.title,
                                lessonKind = lesson.lessonKind,
                                durationSeconds = lesson.durationSeconds,
                                position = lesson.position,
                                isPreview = lesson.isPreview,
                                videoUrl = lesson.videoUrl,
                                bodyMarkdown = lesson.bodyMarkdown,
                                description = lesson.description,
                                quizTitle = lesson.quiz?.title,
                                quizPassingScore = lesson.quiz?.passingScore ?: 80
                            )
                        )
                    }
                }
            }

            courseDao.insertCourses(courseEntities)
            courseDao.insertSections(sectionEntities)
            courseDao.insertLessons(lessonEntities)

            // Seed initial progress for preset learner
            val initialProgressEntities = OrbitRepository.progressMap.value.values.map { prog ->
                LessonProgressEntity(
                    userId = prog.userId,
                    lessonId = prog.lessonId,
                    courseId = "course-soroban-kids",
                    progressSeconds = prog.progressSeconds,
                    completed = prog.completed,
                    completedAt = prog.completedAt,
                    lastSeenAt = prog.lastSeenAt.ifEmpty { "2026-01-20" }
                )
            }
            lessonProgressDao.insertAll(initialProgressEntities)

            // Cache live sessions
            val liveSessions = OrbitRepository.liveSessions.value.map { session ->
                LiveSessionEntity(
                    id = session.id,
                    courseId = session.courseId,
                    courseTitle = session.courseTitle,
                    instructorName = session.instructorName,
                    title = session.title,
                    description = session.description,
                    scheduledAt = session.scheduledAt,
                    durationMinutes = session.durationMinutes,
                    status = session.status,
                    meetingCode = session.meetingCode
                )
            }
            courseDao.insertLiveSessions(liveSessions)
        }
    }

    /**
     * Reactive Stream of cached courses from Room database
     */
    fun getCachedCourses(): Flow<List<Course>> {
        return courseDao.getAllCourses().map { entities ->
            entities.map { entity ->
                // Map to domain Course
                val sections = courseDao.getSectionsForCourseDirect(entity.id).map { secEntity ->
                    val lessons = courseDao.getLessonsForSectionDirect(secEntity.id).map { lesEntity ->
                        Lesson(
                            id = lesEntity.id,
                            sectionId = secEntity.id,
                            title = lesEntity.title,
                            lessonKind = lesEntity.lessonKind,
                            durationSeconds = lesEntity.durationSeconds,
                            position = lesEntity.position,
                            isPreview = lesEntity.isPreview,
                            videoUrl = lesEntity.videoUrl,
                            bodyMarkdown = lesEntity.bodyMarkdown,
                            description = lesEntity.description
                        )
                    }
                    CourseSection(
                        id = secEntity.id,
                        courseId = entity.id,
                        title = secEntity.title,
                        position = secEntity.position,
                        lessons = lessons
                    )
                }

                Course(
                    id = entity.id,
                    slug = entity.slug,
                    title = entity.title,
                    summary = entity.summary,
                    languageCode = entity.languageCode,
                    level = entity.level,
                    category = entity.category,
                    status = entity.status,
                    priceMinor = entity.priceMinor,
                    currency = entity.currency,
                    rating = entity.rating,
                    reviewsCount = entity.reviewsCount,
                    durationHours = entity.durationHours,
                    thumbnailUrl = entity.thumbnailUrl,
                    instructor = Instructor(
                        id = entity.instructorId,
                        name = entity.instructorName,
                        title = entity.instructorTitle,
                        bio = "",
                        avatarUrl = entity.instructorAvatarUrl,
                        phone = "010 28340042",
                        whatsapp = "+201028340042",
                        email = "Saraelarby96@gmail.com",
                        location = "المنصورة"
                    ),
                    outcomes = if (entity.outcomesJoined.isNotEmpty()) entity.outcomesJoined.split("\n") else emptyList(),
                    prerequisites = if (entity.prerequisitesJoined.isNotEmpty()) entity.prerequisitesJoined.split("\n") else emptyList(),
                    sections = sections,
                    createdAt = entity.createdAt,
                    publishedAt = entity.publishedAt
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    /**
     * Stream user lesson progress as a Map<lessonId, LessonProgress>
     */
    fun getUserProgressMap(userId: String): Flow<Map<String, LessonProgress>> {
        return lessonProgressDao.getAllProgressForUser(userId).map { list ->
            list.associate { entity ->
                entity.lessonId to LessonProgress(
                    userId = entity.userId,
                    lessonId = entity.lessonId,
                    progressSeconds = entity.progressSeconds,
                    completed = entity.completed,
                    completedAt = entity.completedAt,
                    lastSeenAt = entity.lastSeenAt
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    /**
     * Get specific lesson progress from Room
     */
    fun getLessonProgress(userId: String, lessonId: String): Flow<LessonProgress?> {
        return lessonProgressDao.getProgressForLesson(userId, lessonId).map { entity ->
            entity?.let {
                LessonProgress(
                    userId = it.userId,
                    lessonId = it.lessonId,
                    progressSeconds = it.progressSeconds,
                    completed = it.completed,
                    completedAt = it.completedAt,
                    lastSeenAt = it.lastSeenAt
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    /**
     * Saves playback position locally in Room so the student can resume offline
     */
    suspend fun saveWatchProgress(userId: String, lessonId: String, courseId: String, seconds: Int) {
        val now = OrbitTimeFormatter.currentDateTime()
        val existing = lessonProgressDao.getProgressForLessonDirect(userId, lessonId)
        if (existing == null) {
            lessonProgressDao.upsertProgress(
                LessonProgressEntity(
                    userId = userId,
                    lessonId = lessonId,
                    courseId = courseId,
                    progressSeconds = seconds,
                    completed = false,
                    lastSeenAt = now
                )
            )
        } else {
            lessonProgressDao.updateWatchSeconds(userId, lessonId, seconds, now)
        }
    }

    /**
     * Marks a lesson completed in Room database
     */
    suspend fun markLessonCompleted(userId: String, lessonId: String, courseId: String) {
        val now = OrbitTimeFormatter.currentDateTime()
        val existing = lessonProgressDao.getProgressForLessonDirect(userId, lessonId)
        if (existing == null) {
            lessonProgressDao.upsertProgress(
                LessonProgressEntity(
                    userId = userId,
                    lessonId = lessonId,
                    courseId = courseId,
                    progressSeconds = 600,
                    completed = true,
                    completedAt = now,
                    lastSeenAt = now
                )
            )
        } else {
            lessonProgressDao.markCompleted(userId, lessonId, now)
        }
    }

    /**
     * Determines which lesson and timestamp the student should resume from
     */
    suspend fun getResumePoint(userId: String, courseId: String): Pair<String?, Int> {
        val sections = courseDao.getSectionsForCourseDirect(courseId)
        val allLessons = sections.flatMap { courseDao.getLessonsForSectionDirect(it.id) }
        for (lesson in allLessons) {
            val prog = lessonProgressDao.getProgressForLessonDirect(userId, lesson.id)
            if (prog == null || !prog.completed) {
                return Pair(lesson.id, prog?.progressSeconds ?: 0)
            }
        }
        return Pair(allLessons.firstOrNull()?.id, 0)
    }

    /**
     * Seeds initial certified real personnel accounts if the database has 0 accounts.
     */
    suspend fun seedRealUserAccounts() {
        val dao = userAccountDao ?: return
        if (dao.getUsersCount() == 0) {
            val initialAccounts = listOf(
                UserAccountEntity(
                    id = "usr_sara_alarabi",
                    email = "sara.alarabi@orbitlearn.edu",
                    passwordHash = "sara123",
                    displayName = "مس سارة العربي",
                    role = "INSTRUCTOR",
                    phone = "+20 10 9876 5432",
                    avatarUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=400",
                    bioOrGrade = "كبير مدربي السوربان والحساب الذهني - مؤسس الأكاديمية",
                    isOnline = true
                ),
                UserAccountEntity(
                    id = "usr_hady_admin",
                    email = "hadybash781@gmail.com",
                    passwordHash = "admin123",
                    displayName = "م. هادي باشا",
                    role = "ADMIN",
                    phone = "+20 12 3456 7890",
                    avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400",
                    bioOrGrade = "المشرف العام وإدارة منصة أوربت ليرن التعليمية",
                    isOnline = true
                ),
                UserAccountEntity(
                    id = "usr_ziad_ahmed",
                    email = "ziad.ahmed@gmail.com",
                    passwordHash = "ziad123",
                    displayName = "زياد أحمد محمود",
                    role = "LEARNER",
                    phone = "+20 11 2233 4455",
                    avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400",
                    bioOrGrade = "الصف الرابع الابتدائي - بطل مسابقة السوربان السريعة",
                    isOnline = true
                ),
                UserAccountEntity(
                    id = "usr_mariam_kareem",
                    email = "mariam.kareem@gmail.com",
                    passwordHash = "mariam123",
                    displayName = "مريم كريم الشريف",
                    role = "LEARNER",
                    phone = "+20 15 5566 7788",
                    avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400",
                    bioOrGrade = "الصف السادس الابتدائي - المستوى المتقدم في الرياضيات الذهنية",
                    isOnline = false
                ),
                UserAccountEntity(
                    id = "usr_asmaa_tarek",
                    email = "asmaa.tarek@orbitlearn.edu",
                    passwordHash = "asmaa123",
                    displayName = "أ. أسماء طارق",
                    role = "INSTRUCTOR",
                    phone = "+20 10 1122 3344",
                    avatarUrl = "https://images.unsplash.com/photo-1580489944761-15a19d654956?w=400",
                    bioOrGrade = "مدربة معتمدة لتنمية مهارات التفكير المنطقي للأطفال",
                    isOnline = true
                )
            )
            dao.insertUsers(initialAccounts)
        }
    }

    fun getAllUserAccountsFlow(): Flow<List<UserAccount>> {
        return userAccountDao?.getAllUsersFlow()?.map { list ->
            list.map { it.toUserAccount() }
        } ?: flowOf(emptyList())
    }

    suspend fun registerNewAccount(
        name: String,
        email: String,
        password: String,
        role: UserRole,
        phone: String,
        bioOrGrade: String
    ): Result<UserAccount> {
        val dao = userAccountDao ?: return Result.failure(Exception("قاعدة البيانات غير مهيأة"))
        val existing = dao.getUserByEmail(email.trim().lowercase())
        if (existing != null) {
            return Result.failure(Exception("هذا البريد الإلكتروني مسجل بالفعل لمستخدم آخر"))
        }

        val newId = "usr_" + java.util.UUID.randomUUID().toString().take(8)
        val entity = UserAccountEntity(
            id = newId,
            email = email.trim().lowercase(),
            passwordHash = password,
            displayName = name.trim(),
            role = role.name,
            phone = phone.trim(),
            avatarUrl = "",
            bioOrGrade = bioOrGrade.trim(),
            isOnline = true,
            createdAt = System.currentTimeMillis()
        )
        dao.insertUser(entity)
        return Result.success(entity.toUserAccount())
    }

    suspend fun authenticateUser(email: String, password: String): UserAccount? {
        val dao = userAccountDao ?: return null
        val user = dao.authenticate(email.trim().lowercase(), password)
        if (user != null) {
            dao.setOnlineStatus(user.id, true)
            return user.toUserAccount()
        }
        return null
    }

    suspend fun setUserOnlineStatus(userId: String, isOnline: Boolean) {
        userAccountDao?.setOnlineStatus(userId, isOnline)
    }

    suspend fun recordCallSession(call: CallSessionEntity) {
        callSessionDao?.insertCall(call)
    }

    fun getCallHistoryFlow(): Flow<List<CallSessionEntity>> {
        return callSessionDao?.getAllCallsFlow() ?: flowOf(emptyList())
    }

    companion object {
        @Volatile
        private var INSTANCE: OrbitRoomRepository? = null

        fun getInstance(context: Context): OrbitRoomRepository {
            return INSTANCE ?: synchronized(this) {
                val db = OrbitDatabase.getDatabase(context)
                val repo = OrbitRoomRepository(
                    courseDao = db.courseDao(),
                    lessonProgressDao = db.lessonProgressDao(),
                    userAccountDao = db.userAccountDao(),
                    callSessionDao = db.callSessionDao()
                )
                INSTANCE = repo
                repo
            }
        }
    }
}
