package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Durable AI-tutor conversation turn. Every Gemini (or fallback) exchange is
 * persisted so the tutor resumes context after process death, and so the
 * instructor/admin portals can audit what was actually asked & answered.
 */
@Entity(tableName = "tutor_messages")
data class TutorMessageEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val role: String, // "user" | "assistant"
    val content: String,
    val lessonTitle: String = "",
    val engineSource: String = "GEMINI", // GEMINI | OFFLINE_FALLBACK
    val createdAt: Long = System.currentTimeMillis()
)
