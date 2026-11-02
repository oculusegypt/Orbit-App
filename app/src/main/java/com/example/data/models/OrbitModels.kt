package com.example.data.models

enum class UserRole(val labelAr: String) {
    LEARNER("طالب / ولي أمر"),
    INSTRUCTOR("معلمة / مدربة"),
    ADMIN("مدير النظام")
}

data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val role: UserRole,
    val locale: String = "ar",
    val avatarUrl: String = "",
    val organization: String = "أكاديمية مس سارة العربي"
)

data class Instructor(
    val id: String,
    val name: String,
    val title: String,
    val bio: String,
    val avatarUrl: String,
    val phone: String,
    val whatsapp: String,
    val email: String,
    val location: String
)

data class ResourceAttachment(
    val id: String,
    val title: String,
    val fileType: String, // "pdf", "zip", "doc"
    val fileSize: String,
    val url: String = "#"
)

data class TranscriptLine(
    val id: String,
    val timeSeconds: Int,
    val speaker: String,
    val text: String
)

data class QuizQuestion(
    val id: String,
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String = ""
)

data class QuizData(
    val id: String,
    val title: String,
    val passingScore: Int = 80,
    val questions: List<QuizQuestion> = emptyList()
)

data class Lesson(
    val id: String,
    val sectionId: String,
    val title: String,
    val lessonKind: String, // "video", "text", "quiz", "resources"
    val durationSeconds: Int,
    val position: Int,
    val isPreview: Boolean = false,
    val videoUrl: String = "",
    val bodyMarkdown: String = "",
    val description: String = "",
    val transcript: List<TranscriptLine> = emptyList(),
    val resources: List<ResourceAttachment> = emptyList(),
    val quiz: QuizData? = null
)

data class CourseSection(
    val id: String,
    val courseId: String,
    val title: String,
    val position: Int,
    val lessons: List<Lesson> = emptyList()
)

data class Course(
    val id: String,
    val slug: String,
    val title: String,
    val summary: String,
    val languageCode: String = "ar",
    val level: String = "مبتدئ إلى متقدم",
    val category: String,
    val status: String = "published",
    val priceMinor: Int, // in EGP cents e.g. 25000 = 250 EGP, 0 = free
    val currency: String = "EGP",
    val version: Int = 2,
    val rating: Double = 4.98,
    val reviewsCount: Int = 184,
    val durationHours: Double = 12.0,
    val thumbnailUrl: String,
    val instructor: Instructor,
    val outcomes: List<String> = emptyList(),
    val prerequisites: List<String> = emptyList(),
    val sections: List<CourseSection> = emptyList(),
    val createdAt: String = "2026-01-10",
    val publishedAt: String = "2026-01-15"
) {
    val priceFormatted: String
        get() = if (priceMinor == 0) "مجاناً" else "${priceMinor / 100} $currency"
}

data class Enrollment(
    val id: String,
    val userId: String,
    val courseId: String,
    val status: String = "active", // "active", "completed"
    val source: String = "purchase",
    val enrolledAt: String,
    val completedAt: String? = null,
    val currentLessonId: String? = null
)

data class LessonProgress(
    val userId: String,
    val lessonId: String,
    val progressSeconds: Int = 0,
    val completed: Boolean = false,
    val completedAt: String? = null,
    val lastSeenAt: String = ""
)

data class Certificate(
    val id: String,
    val enrollmentId: String,
    val courseId: String,
    val courseTitle: String,
    val userId: String,
    val userName: String,
    val instructorName: String,
    val verificationCode: String,
    val issuedAt: String,
    val gradePercentage: Int,
    val skillsVerified: List<String> = emptyList()
)

data class BookChapter(
    val number: Int,
    val title: String,
    val pages: String
)

data class LibraryBook(
    val id: String,
    val title: String,
    val author: String,
    val category: String,
    val pagesCount: Int,
    val coverUrl: String,
    val description: String,
    val downloadUrl: String = "#",
    val publishedYear: Int = 2026,
    val chapters: List<BookChapter> = emptyList()
)

data class LiveSession(
    val id: String,
    val courseId: String,
    val courseTitle: String,
    val instructorId: String,
    val instructorName: String,
    val title: String,
    val description: String,
    val scheduledAt: String,
    val durationMinutes: Int,
    val status: String, // "live", "upcoming", "ended"
    val attendeesCount: Int,
    val meetingCode: String
)

data class Assignment(
    val id: String,
    val courseId: String,
    val courseTitle: String,
    val title: String,
    val description: String,
    val rubric: List<String> = emptyList(),
    val dueDate: String,
    val maxScore: Int = 100
)

data class StudentSubmission(
    val id: String,
    val assignmentId: String,
    val assignmentTitle: String,
    val studentId: String,
    val studentName: String,
    val submittedAt: String,
    val textSubmission: String,
    val repoUrl: String? = null,
    val grade: Int? = null,
    val maxScore: Int = 100,
    val feedback: String? = null,
    val status: String = "submitted" // "submitted", "graded"
)

data class TutorCitation(
    val lessonTitle: String,
    val quote: String
)

data class TutorMessage(
    val id: String,
    val role: String, // "user", "assistant"
    val content: String,
    val timestamp: String = "",
    val citations: List<TutorCitation> = emptyList()
)

data class AuditEvent(
    val id: String,
    val actorUserId: String,
    val actorName: String,
    val action: String,
    val resourceType: String,
    val resourceId: String,
    val reason: String = "",
    val correlationId: String = "",
    val createdAt: String
)

data class ClassroomMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val senderRole: String, // "instructor", "learner"
    val text: String,
    val timestamp: String,
    val isQuestion: Boolean = false,
    val upvotes: Int = 0
)

/**
 * A real consultation/seating booking request captured by the booking funnel.
 * Persisted through [com.example.data.repository.OrbitRepository] and surfaced
 * to the instructor & admin portals (not a dead-end UI state).
 */
data class ConsultationBooking(
    val id: String,
    val parentName: String,
    val childName: String,
    val childAge: String,
    val phone: String,
    val service: String,
    val status: String = "PENDING", // PENDING | CONTACTED | SEATED
    val createdAt: String
)
