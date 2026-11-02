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
        val course = allCourses.find { it.id == courseId } ?: return 0
        val lessonIds = course.sections.flatMap { it.lessons }.map { it.id }
        val grades = lessonIds.mapNotNull { lid ->
            _progressMap.value["$userId:$lid"]?.quizGradePercent
        }
        return if (grades.isEmpty()) 0 else grades.sum() / grades.size
    }

    private fun getUserName(userId: String): String {
        return PRESET_USERS.find { it.id == userId }?.displayName ?: "مستخدم"
    }
}
