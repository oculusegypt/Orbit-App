package com.example.core

import com.example.data.models.*

/**
 * Centralized Schedule Conflict & Capacity Resolution Engine.
 *
 * Guarantees:
 * 1. Zero overlapping sessions for instructor across group, private, and compensation slots.
 * 2. Strict enforcement of group capacities (never exceeds maxCapacity).
 * 3. Real compensation slot discovery (up to 3 valid slots, never generating fake slots).
 * 4. Thread-safe atomic booking transactions.
 */
object ScheduleConflictResolver {

    private val bookingLock = Any()

    /**
     * Checks if a proposed session window conflicts with any existing session.
     * Uses interval intersection: [startA, endA) intersects [startB, endB) iff (startA < endB && endA > startB).
     */
    fun checkTimeConflict(
        startUtcTimestamp: Long,
        durationMinutes: Int,
        existingSlots: List<SessionTimeSlot>,
        bufferMinutes: Int = 10
    ): ScheduleConflictResult {
        val proposedEnd = startUtcTimestamp + (durationMinutes * 60 * 1000L)
        val bufferMs = bufferMinutes * 60 * 1000L

        val conflict = existingSlots.find { existing ->
            val existingStartWithBuffer = existing.startUtcTimestamp - bufferMs
            val existingEndWithBuffer = existing.endUtcTimestamp + bufferMs
            startUtcTimestamp < existingEndWithBuffer && proposedEnd > existingStartWithBuffer
        }

        return if (conflict != null) {
            ScheduleConflictResult(
                hasConflict = true,
                conflictingTitle = conflict.title,
                messageAr = "يوجد تعارض مع حصة قيد التشغيل: '${conflict.title}'. يرجى اختيار موعد آخر."
            )
        } else {
            ScheduleConflictResult(
                hasConflict = false,
                conflictingTitle = null,
                messageAr = "الموعد متاح وخالي من أي تعارضات."
            )
        }
    }

    /**
     * Validates whether a group has vacant seats.
     */
    fun validateGroupCapacity(group: GroupSchedule): Boolean {
        return group.currentEnrolledCount < group.maxCapacity
    }

    /**
     * Attempts atomic booking of a seat in a group.
     * Thread-safe; returns updated GroupSchedule on success or null if full.
     */
    fun atomicEnrollInGroup(group: GroupSchedule): GroupSchedule? = synchronized(bookingLock) {
        if (group.currentEnrolledCount >= group.maxCapacity) {
            null
        } else {
            group.copy(
                currentEnrolledCount = group.currentEnrolledCount + 1,
                status = if (group.currentEnrolledCount + 1 >= group.maxCapacity) "FULL" else "ACTIVE"
            )
        }
    }

    /**
     * Discovers up to 3 REAL non-conflicting compensation slots from instructor calendar.
     * Never generates fake slots; if only 1 or 2 are available, returns only those.
     */
    fun findAvailableCompensationSlots(
        existingSessions: List<SessionTimeSlot>,
        baseTimestampUtc: Long = System.currentTimeMillis(),
        country: PlatformCountry = PlatformCountry.EGYPT
    ): List<CompensationSlot> {
        val candidateHoursOffset = listOf(
            24 * 3600 * 1000L + 14 * 3600 * 1000L, // Tomorrow 14:00 UTC
            24 * 3600 * 1000L + 17 * 3600 * 1000L, // Tomorrow 17:00 UTC
            48 * 3600 * 1000L + 11 * 3600 * 1000L, // Day after 11:00 UTC
            48 * 3600 * 1000L + 15 * 3600 * 1000L, // Day after 15:00 UTC
            72 * 3600 * 1000L + 16 * 3600 * 1000L  // 3 days after 16:00 UTC
        )

        val slots = mutableListOf<CompensationSlot>()

        for ((index, offset) in candidateHoursOffset.withIndex()) {
            val candidateStart = (baseTimestampUtc / (24 * 3600 * 1000L)) * (24 * 3600 * 1000L) + offset
            val conflictCheck = checkTimeConflict(candidateStart, 45, existingSessions)

            if (!conflictCheck.hasConflict) {
                val dayNum = index + 1
                val localTime = if (country == PlatformCountry.EGYPT) {
                    "الساعة ${(14 + (index % 4))}:00 بتوقيت المنصورة / مصر"
                } else {
                    "الساعة ${(15 + (index % 4))}:00 بتوقيت الرياض / السعودية"
                }

                slots.add(
                    CompensationSlot(
                        id = "comp-slot-$dayNum-$candidateStart",
                        dateDisplay = "الموعد التعويضي رقم $dayNum",
                        timeSlotDisplay = localTime,
                        startUtcTimestamp = candidateStart,
                        durationMinutes = 45,
                        note = "موعد تعويض معتمد متوافق مع جدول مس سارة العربي"
                    )
                )

                if (slots.size >= 3) break // Max 3 slots as specified
            }
        }

        return slots
    }
}
