package com.example

import com.example.core.ScheduleConflictResolver
import com.example.data.models.*
import com.example.data.repository.OrbitRepository
import com.example.ui.navigation.*
import org.junit.Assert.*
import org.junit.Test

/**
 * 26 Comprehensive Operational Test Scenarios for Sara Al-Araby Educational Platform.
 * Validates scheduling conflicts, atomic capacity management, age-tier routing,
 * resilient book synchronization, multi-country billing, and certified curricula.
 */
class ExampleUnitTest {

    // 1. Age Tier Routing: Early Childhood (< 6 years)
    @Test
    fun test01_AgeTierRoutingEarlyChildhood() {
        val tier4 = AgeTier.fromAge(4)
        val tier6 = AgeTier.fromAge(6)
        assertEquals(AgeTier.EARLY_CHILDHOOD, tier4)
        assertEquals(AgeTier.EARLY_CHILDHOOD, tier6)
        assertEquals("🌱", tier4.badgeEmoji)
    }

    // 2. Age Tier Routing: Junior Champions (7-8 years)
    @Test
    fun test02_AgeTierRoutingJuniorChampions() {
        val tier7 = AgeTier.fromAge(7)
        val tier8 = AgeTier.fromAge(8)
        assertEquals(AgeTier.JUNIOR_CHAMPIONS, tier7)
        assertEquals(AgeTier.JUNIOR_CHAMPIONS, tier8)
        assertEquals("🚀", tier7.badgeEmoji)
    }

    // 3. Age Tier Routing: Young Geniuses (9-14 years)
    @Test
    fun test03_AgeTierRoutingYoungGeniuses() {
        val tier9 = AgeTier.fromAge(9)
        val tier12 = AgeTier.fromAge(12)
        val tier14 = AgeTier.fromAge(14)
        assertEquals(AgeTier.YOUNG_GENIUSES, tier9)
        assertEquals(AgeTier.YOUNG_GENIUSES, tier12)
        assertEquals(AgeTier.YOUNG_GENIUSES, tier14)
        assertEquals("🧠", tier12.badgeEmoji)
    }

    // 4. Scheduling Conflict: Exact Window Collision
    @Test
    fun test04_ScheduleConflictExactOverlap() {
        val existingSlot = SessionTimeSlot(
            sessionId = "sess-1",
            title = "حساب ذهني - أبطال 1",
            startUtcTimestamp = 1700000000000L,
            durationMinutes = 45
        )
        val result = ScheduleConflictResolver.checkTimeConflict(
            startUtcTimestamp = 1700000000000L,
            durationMinutes = 45,
            existingSlots = listOf(existingSlot)
        )
        assertTrue(result.hasConflict)
        assertEquals("حساب ذهني - أبطال 1", result.conflictingTitle)
    }

    // 5. Scheduling Conflict: Intersecting Buffer Window
    @Test
    fun test05_ScheduleConflictBufferIntersection() {
        val existingSlot = SessionTimeSlot(
            sessionId = "sess-2",
            title = "تأسيس لغة عربية",
            startUtcTimestamp = 1700000000000L,
            durationMinutes = 45
        )
        // 5 minutes after session ends (within default 10-minute buffer)
        val proposedStart = existingSlot.endUtcTimestamp + (5 * 60 * 1000L)
        val result = ScheduleConflictResolver.checkTimeConflict(
            startUtcTimestamp = proposedStart,
            durationMinutes = 45,
            existingSlots = listOf(existingSlot),
            bufferMinutes = 10
        )
        assertTrue(result.hasConflict)
    }

    // 6. Scheduling Non-Conflicting Window
    @Test
    fun test06_ScheduleNonConflictingWindow() {
        val existingSlot = SessionTimeSlot(
            sessionId = "sess-3",
            title = "رياضيات براعم",
            startUtcTimestamp = 1700000000000L,
            durationMinutes = 45
        )
        // 2 hours later - well outside buffer
        val proposedStart = existingSlot.endUtcTimestamp + (120 * 60 * 1000L)
        val result = ScheduleConflictResolver.checkTimeConflict(
            startUtcTimestamp = proposedStart,
            durationMinutes = 45,
            existingSlots = listOf(existingSlot)
        )
        assertFalse(result.hasConflict)
        assertNull(result.conflictingTitle)
    }

    // 7. Group Capacity: Available Seats Validation
    @Test
    fun test07_GroupCapacityAvailableSeats() {
        val group = GroupSchedule(
            id = "grp-eg-1",
            programId = "prog-soroban",
            title = "مجموعة العباقرة - مصر",
            country = PlatformCountry.EGYPT,
            targetAgeTier = AgeTier.YOUNG_GENIUSES,
            maxCapacity = 8,
            currentEnrolledCount = 5,
            fixedDaysAr = "الأحد والأربعاء",
            timeSlotUtc = "14:00 UTC",
            localTimeDisplay = "04:00 م بتوقيت المنصورة",
            monthlyPriceMinor = 45000,
            currency = "EGP"
        )
        assertTrue(ScheduleConflictResolver.validateGroupCapacity(group))
        assertEquals(3, group.availableSeats)
        assertFalse(group.isFull)
    }

    // 8. Group Capacity: Full Group Rejection
    @Test
    fun test08_GroupCapacityFullRejection() {
        val fullGroup = GroupSchedule(
            id = "grp-sa-1",
            programId = "prog-soroban",
            title = "مجموعة النخبة - السعودية",
            country = PlatformCountry.SAUDI_ARABIA,
            targetAgeTier = AgeTier.YOUNG_GENIUSES,
            maxCapacity = 6,
            currentEnrolledCount = 6,
            fixedDaysAr = "السبت والثلاثاء",
            timeSlotUtc = "15:00 UTC",
            localTimeDisplay = "06:00 م بتوقيت الرياض",
            monthlyPriceMinor = 35000,
            currency = "SAR"
        )
        assertFalse(ScheduleConflictResolver.validateGroupCapacity(fullGroup))
        assertEquals(0, fullGroup.availableSeats)
        assertTrue(fullGroup.isFull)
    }

    // 9. Atomic Enrollment: Success Path
    @Test
    fun test09_AtomicEnrollInGroupSuccess() {
        val group = GroupSchedule(
            id = "grp-test-atomic",
            programId = "prog-arabic",
            title = "تأسيس لغة عربية",
            country = PlatformCountry.EGYPT,
            targetAgeTier = AgeTier.EARLY_CHILDHOOD,
            maxCapacity = 8,
            currentEnrolledCount = 4,
            fixedDaysAr = "الإثنين والخميس",
            timeSlotUtc = "13:00 UTC",
            localTimeDisplay = "03:00 م بتوقيت المنصورة",
            monthlyPriceMinor = 35000,
            currency = "EGP"
        )
        val updated = ScheduleConflictResolver.atomicEnrollInGroup(group)
        assertNotNull(updated)
        assertEquals(5, updated?.currentEnrolledCount)
        assertEquals("ACTIVE", updated?.status)
    }

    // 10. Atomic Enrollment: Rejection When Max Capacity Reached
    @Test
    fun test10_AtomicEnrollInGroupRejectionWhenFull() {
        val fullGroup = GroupSchedule(
            id = "grp-test-full",
            programId = "prog-math",
            title = "رياضيات براعم",
            country = PlatformCountry.EGYPT,
            targetAgeTier = AgeTier.EARLY_CHILDHOOD,
            maxCapacity = 5,
            currentEnrolledCount = 5,
            fixedDaysAr = "الأحد",
            timeSlotUtc = "12:00 UTC",
            localTimeDisplay = "02:00 م",
            monthlyPriceMinor = 30000,
            currency = "EGP"
        )
        val result = ScheduleConflictResolver.atomicEnrollInGroup(fullGroup)
        assertNull(result)
    }

    // 11. Atomic Enrollment: Transition Status to FULL
    @Test
    fun test11_AtomicEnrollTransitionsStatusToFull() {
        val nearFullGroup = GroupSchedule(
            id = "grp-test-near-full",
            programId = "prog-soroban",
            title = "حساب ذهني المستوى الثاني",
            country = PlatformCountry.SAUDI_ARABIA,
            targetAgeTier = AgeTier.JUNIOR_CHAMPIONS,
            maxCapacity = 6,
            currentEnrolledCount = 5,
            fixedDaysAr = "الجمعة",
            timeSlotUtc = "16:00 UTC",
            localTimeDisplay = "07:00 م",
            monthlyPriceMinor = 30000,
            currency = "SAR"
        )
        val updated = ScheduleConflictResolver.atomicEnrollInGroup(nearFullGroup)
        assertNotNull(updated)
        assertEquals(6, updated?.currentEnrolledCount)
        assertEquals("FULL", updated?.status)
    }

    // 12. Compensation Slots: Max 3 Valid Slots Returned
    @Test
    fun test12_CompensationSlotsMaxThreeCount() {
        val slots = ScheduleConflictResolver.findAvailableCompensationSlots(
            existingSessions = emptyList(),
            baseTimestampUtc = 1700000000000L,
            country = PlatformCountry.EGYPT
        )
        assertTrue(slots.isNotEmpty())
        assertTrue(slots.size <= 3)
    }

    // 13. Compensation Slots: Filter Conflicting Times
    @Test
    fun test13_CompensationSlotsFilterConflictingTimes() {
        val baseTime = 1700000000000L
        val conflictingCandidateTime = (baseTime / (24 * 3600 * 1000L)) * (24 * 3600 * 1000L) + (24 * 3600 * 1000L + 14 * 3600 * 1000L)

        val blockerSession = SessionTimeSlot(
            sessionId = "blocker-1",
            title = "حصة متعارضة",
            startUtcTimestamp = conflictingCandidateTime,
            durationMinutes = 45
        )

        val slots = ScheduleConflictResolver.findAvailableCompensationSlots(
            existingSessions = listOf(blockerSession),
            baseTimestampUtc = baseTime,
            country = PlatformCountry.EGYPT
        )

        // Ensure none of the returned slots match the blocked timestamp
        assertTrue(slots.none { it.startUtcTimestamp == conflictingCandidateTime })
    }

    // 14. Compensation Slots: Egyptian Timezone Localization
    @Test
    fun test14_CompensationSlotsEgyptianTimezoneLabel() {
        val slots = ScheduleConflictResolver.findAvailableCompensationSlots(
            existingSessions = emptyList(),
            baseTimestampUtc = 1700000000000L,
            country = PlatformCountry.EGYPT
        )
        assertTrue(slots.all { it.timeSlotDisplay.contains("بتوقيت المنصورة / مصر") })
    }

    // 15. Compensation Slots: Saudi Timezone Localization
    @Test
    fun test15_CompensationSlotsSaudiTimezoneLabel() {
        val slots = ScheduleConflictResolver.findAvailableCompensationSlots(
            existingSessions = emptyList(),
            baseTimestampUtc = 1700000000000L,
            country = PlatformCountry.SAUDI_ARABIA
        )
        assertTrue(slots.all { it.timeSlotDisplay.contains("بتوقيت الرياض / السعودية") })
    }

    // 16. Multi-Country Pricing & Currency Separation
    @Test
    fun test16_MultiCountryPricingCurrencySeparation() {
        val egGroup = GroupSchedule(
            id = "grp-eg-price",
            programId = "prog-arabic",
            title = "تأسيس مصري",
            country = PlatformCountry.EGYPT,
            targetAgeTier = AgeTier.EARLY_CHILDHOOD,
            maxCapacity = 8,
            currentEnrolledCount = 2,
            fixedDaysAr = "الأحد",
            timeSlotUtc = "14:00 UTC",
            localTimeDisplay = "04:00 م",
            monthlyPriceMinor = 45000,
            currency = "EGP"
        )
        val saGroup = GroupSchedule(
            id = "grp-sa-price",
            programId = "prog-arabic",
            title = "تأسيس سعودي",
            country = PlatformCountry.SAUDI_ARABIA,
            targetAgeTier = AgeTier.EARLY_CHILDHOOD,
            maxCapacity = 8,
            currentEnrolledCount = 2,
            fixedDaysAr = "الأحد",
            timeSlotUtc = "15:00 UTC",
            localTimeDisplay = "06:00 م",
            monthlyPriceMinor = 35000,
            currency = "SAR"
        )
        assertEquals("450 EGP", egGroup.priceFormatted)
        assertEquals("350 SAR", saGroup.priceFormatted)
        assertNotEquals(egGroup.currency, saGroup.currency)
    }

    // 17. Synchronized PDF Book: Live Teacher Page Sync
    @Test
    fun test17_PdfBookSyncEffectiveDisplayPageLiveSync() {
        val syncState = PdfBookSyncState(
            bookId = "book-soroban-1",
            title = "كتاب الحساب الذهني",
            totalPages = 48,
            currentTeacherPage = 7,
            lastCachedStudentPage = 4,
            isStudentBookVisible = true
        )
        assertEquals(7, syncState.effectiveDisplayPage)
    }

    // 18. Synchronized PDF Book: Resilient Offline Page Fallback
    @Test
    fun test18_PdfBookSyncResilientCacheFallbackOnDisconnection() {
        // Teacher connection lost (page drops to 0)
        val offlineSyncState = PdfBookSyncState(
            bookId = "book-soroban-1",
            title = "كتاب الحساب الذهني",
            totalPages = 48,
            currentTeacherPage = 0,
            lastCachedStudentPage = 6,
            isStudentBookVisible = true
        )
        assertEquals(6, offlineSyncState.effectiveDisplayPage)
    }

    // 19. Synchronized PDF Book: Independent Student Visibility Toggle
    @Test
    fun test19_PdfBookSyncIndependentStudentVisibilityToggle() {
        val hiddenSyncState = PdfBookSyncState(
            bookId = "book-nour-el-bayan",
            title = "كتاب نور البيان",
            totalPages = 60,
            currentTeacherPage = 12,
            lastCachedStudentPage = 12,
            isStudentBookVisible = false
        )
        assertFalse(hiddenSyncState.isStudentBookVisible)
        assertEquals(12, hiddenSyncState.currentTeacherPage)
    }

    // 20. Late Arrival Check-In: PENDING Status
    @Test
    fun test20_LateArrivalCheckInPendingStatus() {
        val checkIn = LateArrivalCheckIn(
            id = "late-101",
            studentId = "stu-omar",
            studentName = "عمر خالد",
            sessionId = "sess-soroban-1",
            sessionTitle = "حساب ذهني مباشر",
            arrivalTime = "14:15 UTC",
            status = "PENDING"
        )
        assertEquals("PENDING", checkIn.status)
        assertEquals("عمر خالد", checkIn.studentName)
    }

    // 21. Absence Apology: Record Creation and Compensation Attachment
    @Test
    fun test21_AbsenceApologyRecordCreationAndSlotAttachment() {
        val compSlot = CompensationSlot(
            id = "slot-1",
            dateDisplay = "الخميس القادم",
            timeSlotDisplay = "05:00 م بتوقيت القاهرة",
            startUtcTimestamp = 1700050000000L
        )
        val apology = AbsenceApologyRecord(
            id = "apol-001",
            studentId = "stu-leila",
            studentName = "ليلى مصطفى",
            parentId = "par-leila",
            sessionId = "sess-arabic-4",
            sessionTitle = "قراءة نور البيان",
            apologyReason = "ظرف صحي طارئ",
            submittedAt = "2026-11-02 10:00",
            status = "OFFERED",
            offeredSlots = listOf(compSlot)
        )
        assertEquals("OFFERED", apology.status)
        assertEquals(1, apology.offeredSlots.size)
        assertEquals("ظرف صحي طارئ", apology.apologyReason)
    }

    // 22. Certified Curricula: All 4 Programs Present
    @Test
    fun test22_SaraAlArabyCertifiedCurriculaPresence() {
        val courses = OrbitRepository.courses.value
        assertEquals(4, courses.size)
        assertTrue(courses.any { it.id == "course-soroban-kids" })
        assertTrue(courses.any { it.id == "course-arabic-foundations" })
        assertTrue(courses.any { it.id == "course-smart-math" })
        assertTrue(courses.any { it.id == "course-tot-trainers" })
    }

    // 23. Soroban Curriculum: Interactive Quiz Loaded
    @Test
    fun test23_SorobanCurriculumHasGradedQuiz() {
        val courses = OrbitRepository.courses.value
        val sorobanCourse = courses.first { it.id == "course-soroban-kids" }
        val firstLesson = sorobanCourse.sections.first().lessons.first()
        assertNotNull(firstLesson.quiz)
        assertEquals(4, firstLesson.quiz?.questions?.size)
    }

    // 24. Digital Library: Books and Structured Chapters
    @Test
    fun test24_DigitalLibraryChaptersAndBooks() {
        val books = OrbitRepository.books.value
        assertEquals(4, books.size)
        assertTrue(books.all { it.chapters.isNotEmpty() })
    }

    // 25. Certificate Issuance: Verification Code Generation
    @Test
    fun test25_CertificateIssuanceAndAudit() {
        val cert = OrbitRepository.issueCertificate(
            courseId = "course-arabic-foundations",
            courseTitle = "تأسيس اللغة العربية والقرائية",
            userId = "usr-student-test",
            userName = "نور الهدى",
            grade = 100,
            skills = listOf("نور البيان", "التحليل الصوتي")
        )
        assertNotNull(cert)
        assertTrue(cert.verificationCode.startsWith("CERT-2026-"))

        val verified = OrbitRepository.verifyCertificate(cert.verificationCode)
        assertNotNull(verified)
        assertEquals("نور الهدى", verified?.userName)
    }

    // 26. Lesson Progress & Completion Mapping
    @Test
    fun test26_EnrollmentAndProgressLogic() {
        val enrollment = OrbitRepository.enrollCourse("usr-student-test", "course-smart-math", "free")
        assertNotNull(enrollment)
        assertEquals("course-smart-math", enrollment.courseId)

        OrbitRepository.completeLesson("usr-student-test", "les-math-1", "course-smart-math")
        val progress = OrbitRepository.progressMap.value["les-math-1"]
        assertNotNull(progress)
        assertTrue(progress?.completed == true)
    }

    // 27. Student Age Category Routing: Precise Age Boundary Verification
    @Test
    fun test27_StudentAgeCategoryFromAgeBoundaries() {
        assertEquals(StudentAgeCategory.CHILD, StudentAgeCategory.fromAge(3))
        assertEquals(StudentAgeCategory.CHILD, StudentAgeCategory.fromAge(5))
        assertEquals(StudentAgeCategory.CHILD, StudentAgeCategory.fromAge(6))
        assertEquals(StudentAgeCategory.YOUTH, StudentAgeCategory.fromAge(7))
        assertEquals(StudentAgeCategory.YOUTH, StudentAgeCategory.fromAge(8))
        assertEquals(StudentAgeCategory.PRE_TEEN, StudentAgeCategory.fromAge(9))
        assertEquals(StudentAgeCategory.PRE_TEEN, StudentAgeCategory.fromAge(11))
        assertEquals(StudentAgeCategory.PRE_TEEN, StudentAgeCategory.fromAge(14))
    }

    // 28. Student Age Category Routing: BirthDate String Resolution
    @Test
    fun test28_StudentAgeCategoryFromBirthDate() {
        // In anchor year 2026:
        // Born in 2021 -> age 5 -> CHILD (<6)
        assertEquals(StudentAgeCategory.CHILD, StudentAgeCategory.fromBirthDate("2021-04-15", 2026))
        // Born in 2018 -> age 8 -> YOUTH (7-8)
        assertEquals(StudentAgeCategory.YOUTH, StudentAgeCategory.fromBirthDate("2018-05-12", 2026))
        // Born in 2014 -> age 12 -> PRE_TEEN (9-14)
        assertEquals(StudentAgeCategory.PRE_TEEN, StudentAgeCategory.fromBirthDate("2014-09-01", 2026))
    }

    // 29. Student Navigation Controller: Initial Route Selection by Category
    @Test
    fun test29_StudentNavControllerInitialRouteByCategory() {
        val childStudent = StudentProfile(
            id = "s-child", parentId = "p-1", parentName = "أمير", name = "مالك",
            birthDate = "2021-01-01", age = 5, ageTier = AgeTier.EARLY_CHILDHOOD,
            country = PlatformCountry.EGYPT
        )
        val youthStudent = StudentProfile(
            id = "s-youth", parentId = "p-1", parentName = "أمير", name = "سالم",
            birthDate = "2018-01-01", age = 8, ageTier = AgeTier.JUNIOR_CHAMPIONS,
            country = PlatformCountry.EGYPT
        )
        val preTeenStudent = StudentProfile(
            id = "s-preteen", parentId = "p-1", parentName = "أمير", name = "هدى",
            birthDate = "2014-01-01", age = 12, ageTier = AgeTier.YOUNG_GENIUSES,
            country = PlatformCountry.EGYPT
        )

        val childNav = StudentNavController(childStudent)
        assertEquals(StudentAgeCategory.CHILD, childNav.currentCategory)
        assertEquals(StudentRoute.ChildPlayground, childNav.currentRoute)
        assertFalse(childNav.canNavigateBack)

        val youthNav = StudentNavController(youthStudent)
        assertEquals(StudentAgeCategory.YOUTH, youthNav.currentCategory)
        assertEquals(StudentRoute.YouthHub, youthNav.currentRoute)

        val preTeenNav = StudentNavController(preTeenStudent)
        assertEquals(StudentAgeCategory.PRE_TEEN, preTeenNav.currentCategory)
        assertEquals(StudentRoute.PreTeenDashboard, preTeenNav.currentRoute)
    }

    // 30. Student Navigation Controller: BackStack Push and Pop
    @Test
    fun test30_StudentNavControllerNavigationStackAndPop() {
        val student = StudentProfile(
            id = "s-test", parentId = "p-1", parentName = "محمود", name = "كريم",
            birthDate = "2018-05-01", age = 8, ageTier = AgeTier.JUNIOR_CHAMPIONS,
            country = PlatformCountry.EGYPT
        )
        val nav = StudentNavController(student)
        assertEquals(StudentRoute.YouthHub, nav.currentRoute)
        assertFalse(nav.canNavigateBack)

        // Navigate to Soroban trainer
        nav.navigate(StudentRoute.YouthSorobanTrainer)
        assertEquals(StudentRoute.YouthSorobanTrainer, nav.currentRoute)
        assertTrue(nav.canNavigateBack)

        // Navigate deeper to Missions
        nav.navigate(StudentRoute.YouthMissions)
        assertEquals(StudentRoute.YouthMissions, nav.currentRoute)

        // Pop back to Soroban trainer
        val popped1 = nav.popBackStack()
        assertTrue(popped1)
        assertEquals(StudentRoute.YouthSorobanTrainer, nav.currentRoute)

        // Pop back to root Hub
        val popped2 = nav.popBackStack()
        assertTrue(popped2)
        assertEquals(StudentRoute.YouthHub, nav.currentRoute)
        assertFalse(nav.canNavigateBack)

        // Attempting to pop root returns false
        assertFalse(nav.popBackStack())
    }

    // 31. Student Navigation Controller: Category Switching Resets Root
    @Test
    fun test31_StudentNavControllerCategorySwitchResetsRoot() {
        val student = StudentProfile(
            id = "s-init", parentId = "p-1", parentName = "فاطمة", name = "يمنى",
            birthDate = "2018-02-10", age = 8, ageTier = AgeTier.JUNIOR_CHAMPIONS,
            country = PlatformCountry.EGYPT
        )
        val nav = StudentNavController(student)
        assertEquals(StudentAgeCategory.YOUTH, nav.currentCategory)

        // Switch to CHILD (<6)
        nav.switchAgeCategory(StudentAgeCategory.CHILD)
        assertEquals(StudentAgeCategory.CHILD, nav.currentCategory)
        assertEquals(StudentRoute.ChildPlayground, nav.currentRoute)
        assertEquals(AgeTier.EARLY_CHILDHOOD, nav.studentProfile.ageTier)

        // Switch to PRE_TEEN (9-14)
        nav.switchAgeCategory(StudentAgeCategory.PRE_TEEN)
        assertEquals(StudentAgeCategory.PRE_TEEN, nav.currentCategory)
        assertEquals(StudentRoute.PreTeenDashboard, nav.currentRoute)
        assertEquals(AgeTier.YOUNG_GENIUSES, nav.studentProfile.ageTier)
    }
}
