package com.example.data.models

/**
 * Domain models for Sara Al-Araby Educational Ecosystem (منصة سارة العربي للتعليم الإلكتروني).
 */

enum class PlatformCountry(
    val code: String,
    val nameAr: String,
    val currency: String,
    val flag: String,
    val timezone: String,
    val defaultCity: String
) {
    EGYPT("EG", "مصر", "EGP", "🇪🇬", "Africa/Cairo", "المنصورة / القاهرة"),
    SAUDI_ARABIA("SA", "المملكة العربية السعودية", "SAR", "🇸🇦", "Asia/Riyadh", "الرياض / جدة")
}

enum class AgeTier(
    val code: String,
    val labelAr: String,
    val ageRangeAr: String,
    val minAge: Int,
    val maxAge: Int,
    val badgeEmoji: String,
    val descriptionAr: String
) {
    EARLY_CHILDHOOD(
        code = "tier_early",
        labelAr = "براعم سارة (التأسيس المبكر)",
        ageRangeAr = "حتى 6 سنوات",
        minAge = 3,
        maxAge = 6,
        badgeEmoji = "🌱",
        descriptionAr = "تأسيس اللغة العربية والرياضيات بنور البيان، أصوات الحروف، والأرقام المصورة مع ألعاب تفاعلية مبهجة."
    ),
    JUNIOR_CHAMPIONS(
        code = "tier_junior",
        labelAr = "أبطال سارة (القراءة والحساب السريع)",
        ageRangeAr = "7 إلى 8 سنوات",
        minAge = 7,
        maxAge = 8,
        badgeEmoji = "🚀",
        descriptionAr = "انطلاق القراءة الطليقة، مبادئ العداد الصيني (السوروبان)، تحديات يومية بنقاط XP وعداد الأيام المتتالية."
    ),
    YOUNG_GENIUSES(
        code = "tier_genius",
        labelAr = "عباقرة سارة (السوروبان المتقدم والتفكير الرياضي)",
        ageRangeAr = "9 إلى 14 سنة",
        minAge = 9,
        maxAge = 14,
        badgeEmoji = "🧠",
        descriptionAr = "الحساب الذهني الخارق التخيلي، العمليات الحسابية المتزامنة، الجداول الأكاديمية والشهادات المعتمدة."
    );

    companion object {
        fun fromAge(age: Int): AgeTier {
            return when {
                age <= 6 -> EARLY_CHILDHOOD
                age in 7..8 -> JUNIOR_CHAMPIONS
                else -> YOUNG_GENIUSES
            }
        }
    }
}

enum class PlatformRole(val labelAr: String) {
    STUDENT("طالب"),
    PARENT("ولي أمر"),
    INSTRUCTOR("المعلمة سارة العربي"),
    ADMIN("الإدارة المدرسية")
}

data class StudentProfile(
    val id: String,
    val parentId: String,
    val parentName: String,
    val name: String,
    val birthDate: String, // "YYYY-MM-DD"
    val age: Int,
    val ageTier: AgeTier,
    val country: PlatformCountry,
    val enrolledGroupIds: List<String> = emptyList(),
    val avatarEmoji: String = "🧒",
    val completedLessonsCount: Int = 0,
    val currentStreakDays: Int = 3,
    val totalXp: Int = 450,
    val dailyAttendanceRatePercent: Int = 100
)

data class GroupSchedule(
    val id: String,
    val programId: String,
    val title: String,
    val country: PlatformCountry,
    val targetAgeTier: AgeTier,
    val maxCapacity: Int = 8,
    val currentEnrolledCount: Int = 0,
    val fixedDaysAr: String, // e.g. "الأحد والثلاثاء"
    val timeSlotUtc: String, // e.g. "14:00 UTC"
    val localTimeDisplay: String, // e.g. "04:00 م بتوقيت القاهرة"
    val monthlyPriceMinor: Int, // EGP cents or SAR cents
    val currency: String,
    val instructorName: String = "أ / سارة العربي",
    val status: String = "ACTIVE" // "ACTIVE", "FULL", "ARCHIVED"
) {
    val isFull: Boolean get() = currentEnrolledCount >= maxCapacity
    val availableSeats: Int get() = (maxCapacity - currentEnrolledCount).coerceAtLeast(0)
    val priceFormatted: String get() = "${monthlyPriceMinor / 100} $currency"
}

data class SessionTimeSlot(
    val sessionId: String,
    val title: String,
    val startUtcTimestamp: Long,
    val durationMinutes: Int = 45,
    val type: String = "GROUP", // GROUP | PRIVATE | COMPENSATION
    val instructorId: String = "inst-sara-elarby"
) {
    val endUtcTimestamp: Long get() = startUtcTimestamp + (durationMinutes * 60 * 1000L)
}

data class CompensationSlot(
    val id: String,
    val dateDisplay: String,
    val timeSlotDisplay: String,
    val startUtcTimestamp: Long,
    val durationMinutes: Int = 45,
    val note: String = "موعد تعويض معتمد وخالي من التعارضات"
)

data class AbsenceApologyRecord(
    val id: String,
    val studentId: String,
    val studentName: String,
    val parentId: String,
    val sessionId: String,
    val sessionTitle: String,
    val apologyReason: String,
    val submittedAt: String,
    val status: String = "PENDING", // PENDING | OFFERED | CONFIRMED | REJECTED
    val offeredSlots: List<CompensationSlot> = emptyList(),
    val selectedSlot: CompensationSlot? = null,
    val confirmationNote: String = ""
)

data class LateArrivalCheckIn(
    val id: String,
    val studentId: String,
    val studentName: String,
    val sessionId: String,
    val sessionTitle: String,
    val arrivalTime: String,
    val status: String = "PENDING" // PENDING | ACCEPTED | REJECTED
)

data class PdfBookSyncState(
    val bookId: String,
    val title: String,
    val totalPages: Int,
    val currentTeacherPage: Int = 1,
    val lastCachedStudentPage: Int = 1,
    val isStudentBookVisible: Boolean = true
) {
    // If student loses connection, resilient page falls back to last cached page
    val effectiveDisplayPage: Int
        get() = if (currentTeacherPage > 0) currentTeacherPage else lastCachedStudentPage
}

data class ScheduleConflictResult(
    val hasConflict: Boolean,
    val conflictingTitle: String? = null,
    val messageAr: String
)
