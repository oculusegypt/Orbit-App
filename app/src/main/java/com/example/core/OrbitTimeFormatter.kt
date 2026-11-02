package com.example.core

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Centralized date/time formatting helpers used across the repository,
 * services and UI layers. Replaces scattered ad-hoc `SimpleDateFormat(...)`
 * constructions so format patterns live in one place.
 */
object OrbitTimeFormatter {

    private const val PATTERN_DATE = "yyyy-MM-dd"
    private const val PATTERN_DATE_TIME = "yyyy-MM-dd HH:mm"
    private const val PATTERN_TIME = "HH:mm"

    private fun format(pattern: String): String =
        SimpleDateFormat(pattern, Locale.getDefault()).format(Date())

    /** e.g. "2026-11-02" */
    fun currentDate(): String = format(PATTERN_DATE)

    /** e.g. "2026-11-02 14:35" */
    fun currentDateTime(): String = format(PATTERN_DATE_TIME)

    /** e.g. "14:35" */
    fun currentTime(): String = format(PATTERN_TIME)
}
