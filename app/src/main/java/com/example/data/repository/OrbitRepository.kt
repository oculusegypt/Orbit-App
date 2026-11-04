package com.example.data.repository
import com.example.core.OrbitTimeFormatter

import com.example.data.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date
import java.util.Locale

object OrbitRepository {

    // ─── Static seed catalog (curriculum content) lives in OrbitSeedData ───
    // The repository below keeps only reactive state holders + business logic.
    val INSTRUCTOR_SARA = OrbitSeedData.INSTRUCTOR_SARA
    val PRESET_USERS = OrbitSeedData.PRESET_USERS

    private val _courses = MutableStateFlow(OrbitSeedData.buildCourses())
    val courses: StateFlow<List<Course>> = _courses.asStateFlow()

    private val _books = MutableStateFlow(OrbitSeedData.buildBooks())
    val books: StateFlow<List<LibraryBook>> = _books.asStateFlow()

    private val _liveSessions = MutableStateFlow(OrbitSeedData.buildLiveSessions())
    val liveSessions: StateFlow<List<LiveSession>> = _liveSessions.asStateFlow()

    private val _assignments = MutableStateFlow(OrbitSeedData.buildAssignments())
    val assignments: StateFlow<List<Assignment>> = _assignments.asStateFlow()

    private val _submissions = MutableStateFlow<List<StudentSubmission>>(OrbitSeedData.buildSubmissions())
    val submissions: StateFlow<List<StudentSubmission>> = _submissions.asStateFlow()

    private val _certificates = MutableStateFlow<List<Certificate>>(OrbitSeedData.buildCertificates())
    val certificates: StateFlow<List<Certificate>> = _certificates.asStateFlow()

    private val _enrollments = MutableStateFlow<List<Enrollment>>(OrbitSeedData.buildEnrollments())
    val enrollments: StateFlow<List<Enrollment>> = _enrollments.asStateFlow()

    private val _progressMap = MutableStateFlow<Map<String, LessonProgress>>(OrbitSeedData.buildProgressMap())
    val progressMap: StateFlow<Map<String, LessonProgress>> = _progressMap.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AuditEvent>>(OrbitSeedData.buildAuditLogs())
    val auditLogs: StateFlow<List<AuditEvent>> = _auditLogs.asStateFlow()

    private val _classroomMessages = MutableStateFlow<List<ClassroomMessage>>(OrbitSeedData.buildClassroomMessages())
    val classroomMessages: StateFlow<List<ClassroomMessage>> = _classroomMessages.asStateFlow()

    private val _consultationBookings = MutableStateFlow<List<ConsultationBooking>>(emptyList())
    val consultationBookings: StateFlow<List<ConsultationBooking>> = _consultationBookings.asStateFlow()

    // ─── Real Sara Al-Araby Educational Ecosystem Flows ───
    private val _groups = MutableStateFlow<List<GroupSchedule>>(
        listOf(
            GroupSchedule(
                id = "grp-eg-arabic-foundations",
                programId = "prog-arabic",
                title = "مجموعة براعم نور البيان وتأسيس القراءة (مصر) 🌱",
                country = PlatformCountry.EGYPT,
                targetAgeTier = AgeTier.EARLY_CHILDHOOD,
                maxCapacity = 8,
                currentEnrolledCount = 3,
                fixedDaysAr = "الأحد والأربعاء",
                timeSlotUtc = "14:00 UTC",
                localTimeDisplay = "04:00 م بتوقيت القاهرة",
                monthlyPriceMinor = 45000,
                currency = "EGP"
            ),
            GroupSchedule(
                id = "grp-eg-soroban-junior",
                programId = "prog-soroban-junior",
                title = "مجموعة أبطال الحساب السريع والسوروبان (مصر) 🚀",
                country = PlatformCountry.EGYPT,
                targetAgeTier = AgeTier.JUNIOR_CHAMPIONS,
                maxCapacity = 8,
                currentEnrolledCount = 5,
                fixedDaysAr = "السبت والثلاثاء",
                timeSlotUtc = "15:00 UTC",
                localTimeDisplay = "05:00 م بتوقيت القاهرة",
                monthlyPriceMinor = 55000,
                currency = "EGP"
            ),
            GroupSchedule(
                id = "grp-eg-genius-math",
                programId = "prog-soroban-genius",
                title = "مجموعة دبلوم عباقرة السوروبان والتفكير الرياضي (مصر) 🧠",
                country = PlatformCountry.EGYPT,
                targetAgeTier = AgeTier.YOUNG_GENIUSES,
                maxCapacity = 8,
                currentEnrolledCount = 6,
                fixedDaysAr = "الاثنين والخميس",
                timeSlotUtc = "16:00 UTC",
                localTimeDisplay = "06:00 م بتوقيت القاهرة",
                monthlyPriceMinor = 65000,
                currency = "EGP"
            ),
            GroupSchedule(
                id = "grp-sa-arabic-foundations",
                programId = "prog-arabic-sa",
                title = "مجموعة تأسيس القراءة واللغة العربية بنور البيان (السعودية) 🌱",
                country = PlatformCountry.SAUDI_ARABIA,
                targetAgeTier = AgeTier.EARLY_CHILDHOOD,
                maxCapacity = 8,
                currentEnrolledCount = 2,
                fixedDaysAr = "الأحد والأربعاء",
                timeSlotUtc = "15:00 UTC",
                localTimeDisplay = "06:00 م بتوقيت مكة المكرمة",
                monthlyPriceMinor = 15000,
                currency = "SAR"
            ),
            GroupSchedule(
                id = "grp-sa-soroban-junior",
                programId = "prog-soroban-sa",
                title = "مجموعة الحساب الذهني وتطوير الذكاء (السعودية) 🚀",
                country = PlatformCountry.SAUDI_ARABIA,
                targetAgeTier = AgeTier.JUNIOR_CHAMPIONS,
                maxCapacity = 8,
                currentEnrolledCount = 4,
                fixedDaysAr = "السبت والثلاثاء",
                timeSlotUtc = "16:00 UTC",
                localTimeDisplay = "07:00 م بتوقيت مكة المكرمة",
                monthlyPriceMinor = 18000,
                currency = "SAR"
            ),
            GroupSchedule(
                id = "grp-sa-genius-math",
                programId = "prog-genius-sa",
                title = "مجموعة عباقرة السوروبان التخيلي المتقدم (السعودية) 🧠",
                country = PlatformCountry.SAUDI_ARABIA,
                targetAgeTier = AgeTier.YOUNG_GENIUSES,
                maxCapacity = 8,
                currentEnrolledCount = 5,
                fixedDaysAr = "الاثنين والخميس",
                timeSlotUtc = "17:00 UTC",
                localTimeDisplay = "08:00 م بتوقيت مكة المكرمة",
                monthlyPriceMinor = 22000,
                currency = "SAR"
            )
        )
    )
    val groups: StateFlow<List<GroupSchedule>> = _groups.asStateFlow()

    private val _studentProfiles = MutableStateFlow<List<StudentProfile>>(emptyList())
    val studentProfiles: StateFlow<List<StudentProfile>> = _studentProfiles.asStateFlow()

    private val _absenceApologies = MutableStateFlow<List<AbsenceApologyRecord>>(emptyList())
    val absenceApologies: StateFlow<List<AbsenceApologyRecord>> = _absenceApologies.asStateFlow()

    private val _lateArrivals = MutableStateFlow<List<LateArrivalCheckIn>>(emptyList())
    val lateArrivals: StateFlow<List<LateArrivalCheckIn>> = _lateArrivals.asStateFlow()

    private val _pdfBookSyncState = MutableStateFlow(
        PdfBookSyncState(
            bookId = "book-soroban-1",
            title = "كتاب السوروبان الذكي للأطفال",
            totalPages = 24,
            currentTeacherPage = 1,
            lastCachedStudentPage = 1,
            isStudentBookVisible = true
        )
    )
    val pdfBookSyncState: StateFlow<PdfBookSyncState> = _pdfBookSyncState.asStateFlow()

    fun addStudentProfile(
        parentId: String,
        parentName: String,
        name: String,
        birthDate: String,
        country: PlatformCountry
    ): StudentProfile {
        val calculatedAge = calculateAge(birthDate)
        val calculatedTier = AgeTier.fromAge(calculatedAge)
        val profile = StudentProfile(
            id = "std-" + System.currentTimeMillis(),
            parentId = parentId,
            parentName = parentName,
            name = name.trim(),
            birthDate = birthDate.trim(),
            age = calculatedAge,
            ageTier = calculatedTier,
            country = country,
            avatarEmoji = calculatedTier.badgeEmoji,
            completedLessonsCount = 0,
            currentStreakDays = 1,
            totalXp = 100,
            dailyAttendanceRatePercent = 100
        )
        _studentProfiles.value = listOf(profile) + _studentProfiles.value
        addAudit(
            actorUserId = parentId,
            actorName = parentName,
            action = "CHILD_PROFILE_CREATED",
            resourceType = "student",
            resourceId = profile.id,
            reason = "إضافة الطفل: ${profile.name} ($calculatedAge سنوات) - ${calculatedTier.labelAr}"
        )
        return profile
    }

    private fun calculateAge(birthDate: String): Int {
        return try {
            val year = birthDate.trim().split("-")[0].toIntOrNull() ?: 2018
            (2026 - year).coerceIn(3, 14)
        } catch (_: Exception) {
            7
        }
    }

    fun enrollChildInGroup(studentId: String, groupId: String, actorName: String): Result<String> {
        val group = _groups.value.find { it.id == groupId }
            ?: return Result.failure(Exception("المجموعة غير موجودة"))
        if (group.isFull) {
            return Result.failure(Exception("المجموعة مكتملة العدد بالكامل (الحد الأقصى ${group.maxCapacity} طلاب)"))
        }
        val student = _studentProfiles.value.find { it.id == studentId }
            ?: return Result.failure(Exception("ملف الطالب غير مسجل"))
        if (student.enrolledGroupIds.contains(groupId)) {
            return Result.failure(Exception("الطالب مسجل بالفعل في هذه المجموعة"))
        }

        _groups.value = _groups.value.map {
            if (it.id == groupId) it.copy(currentEnrolledCount = it.currentEnrolledCount + 1) else it
        }

        _studentProfiles.value = _studentProfiles.value.map {
            if (it.id == studentId) it.copy(enrolledGroupIds = it.enrolledGroupIds + groupId) else it
        }

        addAudit(
            actorUserId = student.parentId,
            actorName = actorName,
            action = "GROUP_ENROLLMENT_CONFIRMED",
            resourceType = "group",
            resourceId = groupId,
            reason = "اشتراك الطالب ${student.name} في ${group.title}"
        )
        return Result.success("تم الاشتراك بنجاح في ${group.title}")
    }

    fun submitAbsenceApology(
        studentId: String,
        studentName: String,
        parentId: String,
        sessionId: String,
        sessionTitle: String,
        apologyReason: String
    ): AbsenceApologyRecord {
        val record = AbsenceApologyRecord(
            id = "apology-" + System.currentTimeMillis(),
            studentId = studentId,
            studentName = studentName,
            parentId = parentId,
            sessionId = sessionId,
            sessionTitle = sessionTitle,
            apologyReason = apologyReason,
            submittedAt = OrbitTimeFormatter.currentDateTime(),
            status = "PENDING"
        )
        _absenceApologies.value = listOf(record) + _absenceApologies.value
        addAudit(
            actorUserId = parentId,
            actorName = studentName,
            action = "ABSENCE_APOLOGY_SUBMITTED",
            resourceType = "apology",
            resourceId = record.id,
            reason = "اعتذار عن $sessionTitle: $apologyReason"
        )
        return record
    }

    fun offerCompensationSlot(apologyId: String, slots: List<CompensationSlot>) {
        _absenceApologies.value = _absenceApologies.value.map {
            if (it.id == apologyId) it.copy(status = "OFFERED", offeredSlots = slots) else it
        }
        addAudit(
            actorUserId = "inst-sara-elarby",
            actorName = INSTRUCTOR_SARA.name,
            action = "COMPENSATION_OFFERED",
            resourceType = "apology",
            resourceId = apologyId,
            reason = "اقتراح مواعيد تعويض خالية من التعارضات"
        )
    }

    fun confirmCompensationSlot(apologyId: String, selectedSlot: CompensationSlot) {
        _absenceApologies.value = _absenceApologies.value.map {
            if (it.id == apologyId) it.copy(status = "CONFIRMED", selectedSlot = selectedSlot) else it
        }
    }

    fun submitLateArrival(
        studentId: String,
        studentName: String,
        sessionId: String,
        sessionTitle: String
    ): LateArrivalCheckIn {
        val checkIn = LateArrivalCheckIn(
            id = "late-" + System.currentTimeMillis(),
            studentId = studentId,
            studentName = studentName,
            sessionId = sessionId,
            sessionTitle = sessionTitle,
            arrivalTime = OrbitTimeFormatter.currentTime(),
            status = "PENDING"
        )
        _lateArrivals.value = listOf(checkIn) + _lateArrivals.value
        return checkIn
    }

    fun respondToLateArrival(checkInId: String, accept: Boolean) {
        _lateArrivals.value = _lateArrivals.value.map {
            if (it.id == checkInId) it.copy(status = if (accept) "ACCEPTED" else "REJECTED") else it
        }
    }

    fun updateTeacherBookPage(page: Int) {
        _pdfBookSyncState.value = _pdfBookSyncState.value.copy(
            currentTeacherPage = page,
            lastCachedStudentPage = page
        )
    }

    /**
     * Persists a real consultation booking request submitted from the booking
     * funnel, records an audit trail event, and returns the stored booking so
     * callers can display its reference id. Surfaced to instructor/admin portals.
     */
    fun bookConsultation(
        parentName: String,
        childName: String,
        childAge: String,
        phone: String,
        service: String,
        actorUserId: String = "guest",
        actorName: String = "زائر"
    ): ConsultationBooking {
        val booking = ConsultationBooking(
            id = "bkg-" + System.currentTimeMillis(),
            parentName = parentName.trim(),
            childName = childName.trim(),
            childAge = childAge.trim(),
            phone = phone.trim(),
            service = service,
            status = "PENDING",
            createdAt = OrbitTimeFormatter.currentDateTime()
        )
        _consultationBookings.value = listOf(booking) + _consultationBookings.value
        addAudit(
            actorUserId = actorUserId,
            actorName = actorName,
            action = "CONSULTATION_BOOKED",
            resourceType = "booking",
            resourceId = booking.id,
            reason = "ولي أمر: ${booking.parentName} | طفل: ${booking.childName} (${booking.childAge}) | هاتف: ${booking.phone} | برنامج: ${booking.service}",
            correlationId = booking.id
        )
        return booking
    }

    /**
     * Merges bookings rehydrated from the durable Room store into the live
     * pipeline (e.g. after process death), preserving status transitions.
     */
    fun mergePersistedBookings(persisted: List<ConsultationBooking>) {
        val current = _consultationBookings.value.associateBy { it.id }
        val merged = persisted.map { p -> current[p.id] ?: p } +
            current.values.filter { c -> persisted.none { it.id == c.id } }
        _consultationBookings.value = merged.sortedByDescending { it.createdAt }
    }

    /** Instructor-side workflow: advance a booking's status with an audit record. */
    fun updateBookingStatus(bookingId: String, newStatus: String, actorName: String) {
        if (_consultationBookings.value.none { it.id == bookingId }) return
        _consultationBookings.value = _consultationBookings.value.map {
            if (it.id == bookingId) it.copy(status = newStatus) else it
        }
        addAudit(
            actorUserId = "inst-sara-elarby",
            actorName = actorName,
            action = "BOOKING_STATUS_$newStatus",
            resourceType = "booking",
            resourceId = bookingId,
            correlationId = bookingId
        )
    }

    // Helper functions
    fun enrollCourse(userId: String, courseId: String, source: String): Enrollment {
        val existing = _enrollments.value.find { it.userId == userId && it.courseId == courseId }
        if (existing != null) return existing

        val newEnrollment = Enrollment(
            id = "enr-" + System.currentTimeMillis(),
            userId = userId,
            courseId = courseId,
            status = "active",
            source = source,
            enrolledAt = OrbitTimeFormatter.currentDate()
        )
        _enrollments.value = listOf(newEnrollment) + _enrollments.value

        addAudit(
            actorUserId = userId,
            actorName = getUserName(userId),
            action = "ENROLLMENT_CREATED",
            resourceType = "course",
            resourceId = courseId,
            reason = "تسجيل فوري في الدورة"
        )
        return newEnrollment
    }

    fun completeLesson(userId: String, lessonId: String, courseId: String) {
        val now = OrbitTimeFormatter.currentDateTime()
        val updated = _progressMap.value.toMutableMap()
        updated[lessonId] = LessonProgress(
            userId = userId,
            lessonId = lessonId,
            progressSeconds = 600,
            completed = true,
            completedAt = now
        )
        _progressMap.value = updated
    }

    fun issueCertificate(courseId: String, courseTitle: String, userId: String, userName: String, grade: Int, skills: List<String>): Certificate {
        val existing = _certificates.value.find { it.courseId == courseId && it.userId == userId }
        if (existing != null) return existing

        val code = "CERT-2026-" + courseId.takeLast(6).uppercase(Locale.ROOT) + "-" + (100..999).random()
        val cert = Certificate(
            id = "cert-" + System.currentTimeMillis(),
            enrollmentId = "enr-" + System.currentTimeMillis(),
            courseId = courseId,
            courseTitle = courseTitle,
            userId = userId,
            userName = userName,
            instructorName = INSTRUCTOR_SARA.name,
            verificationCode = code,
            issuedAt = OrbitTimeFormatter.currentDate(),
            gradePercentage = grade,
            skillsVerified = skills
        )
        _certificates.value = listOf(cert) + _certificates.value

        addAudit(
            actorUserId = "inst-sara-elarby",
            actorName = INSTRUCTOR_SARA.name,
            action = "CERTIFICATE_ISSUED",
            resourceType = "certificate",
            resourceId = cert.id,
            reason = "إصدار شهادة اجتياز معتمدة للطالب $userName بكود $code",
            correlationId = code
        )
        return cert
    }

    fun submitAssignment(assignmentId: String, assignmentTitle: String, studentId: String, studentName: String, text: String, repoUrl: String?): StudentSubmission {
        val sub = StudentSubmission(
            id = "sub-" + System.currentTimeMillis(),
            assignmentId = assignmentId,
            assignmentTitle = assignmentTitle,
            studentId = studentId,
            studentName = studentName,
            submittedAt = OrbitTimeFormatter.currentDateTime(),
            textSubmission = text,
            repoUrl = repoUrl,
            status = "submitted"
        )
        _submissions.value = listOf(sub) + _submissions.value

        addAudit(
            actorUserId = studentId,
            actorName = studentName,
            action = "ASSIGNMENT_SUBMITTED",
            resourceType = "assignment",
            resourceId = assignmentId,
            reason = "تسليم واجب الطالب: $assignmentTitle"
        )
        return sub
    }

    fun gradeSubmission(subId: String, grade: Int, feedback: String, graderName: String) {
        _submissions.value = _submissions.value.map {
            if (it.id == subId) {
                it.copy(grade = grade, feedback = feedback, status = "graded")
            } else it
        }

        addAudit(
            actorUserId = "inst-sara-elarby",
            actorName = graderName,
            action = "SUBMISSION_GRADED",
            resourceType = "assignment_submission",
            resourceId = subId,
            reason = "رصد الدرجة $grade/100 مع الملاحظات"
        )
    }

    fun sendClassroomMessage(senderId: String, senderName: String, role: String, text: String, isQuestion: Boolean = false) {
        val time = OrbitTimeFormatter.currentTime()
        val msg = ClassroomMessage(
            id = "msg-" + System.currentTimeMillis(),
            senderId = senderId,
            senderName = senderName,
            senderRole = role,
            text = text,
            timestamp = time,
            isQuestion = isQuestion
        )
        _classroomMessages.value = _classroomMessages.value + listOf(msg)
    }

    fun upvoteQuestion(messageId: String) {
        _classroomMessages.value = _classroomMessages.value.map {
            if (it.id == messageId) it.copy(upvotes = it.upvotes + 1) else it
        }
    }

    fun addAudit(actorUserId: String, actorName: String, action: String, resourceType: String, resourceId: String, reason: String = "", correlationId: String = "") {
        val event = AuditEvent(
            id = "aud-" + System.currentTimeMillis(),
            actorUserId = actorUserId,
            actorName = actorName,
            action = action,
            resourceType = resourceType,
            resourceId = resourceId,
            reason = reason,
            correlationId = correlationId,
            createdAt = OrbitTimeFormatter.currentDateTime()
        )
        _auditLogs.value = listOf(event) + _auditLogs.value
    }

    fun verifyCertificate(code: String): Certificate? {
        val clean = code.trim().uppercase(Locale.ROOT)
        return _certificates.value.find { it.verificationCode.equals(clean, ignoreCase = true) }
    }

    /**
     * Average quiz score (percent) a user achieved across all completed
     * lessons of a course — used as the grade printed on auto-issued
     * graduation certificates. Returns 0 when no graded quiz was recorded.
     */
    fun courseQuizAveragePercent(userId: String, courseId: String): Int {
        val course = _courses.value.find { it.id == courseId } ?: return 0
        val lessonIds = course.sections.flatMap { it.lessons }.map { it.id }
        if (lessonIds.isEmpty()) return 0
        val completedCount = lessonIds.count { lid -> _progressMap.value[lid]?.completed == true }
        return if (completedCount > 0) (completedCount * 100) / lessonIds.size else 0
    }

    private fun getUserName(userId: String): String {
        return PRESET_USERS.find { it.id == userId }?.displayName ?: "مستخدم"
    }
}
