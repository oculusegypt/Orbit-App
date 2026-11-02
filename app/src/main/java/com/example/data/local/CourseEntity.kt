package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.models.*

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: String,
    val slug: String,
    val title: String,
    val summary: String,
    val languageCode: String = "ar",
    val level: String,
    val category: String,
    val status: String,
    val priceMinor: Int,
    val currency: String,
    val rating: Double,
    val reviewsCount: Int,
    val durationHours: Double,
    val thumbnailUrl: String,
    val instructorId: String,
    val instructorName: String,
    val instructorTitle: String,
    val instructorAvatarUrl: String,
    val outcomesJoined: String, // newline-separated outcomes
    val prerequisitesJoined: String, // newline-separated prerequisites
    val createdAt: String,
    val publishedAt: String,
    val cachedAtTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "course_sections")
data class CourseSectionEntity(
    @PrimaryKey val id: String,
    val courseId: String,
    val title: String,
    val position: Int
)

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: String,
    val sectionId: String,
    val courseId: String,
    val title: String,
    val lessonKind: String, // "video", "text", "quiz", "resources"
    val durationSeconds: Int,
    val position: Int,
    val isPreview: Boolean,
    val videoUrl: String,
    val bodyMarkdown: String,
    val description: String,
    val quizTitle: String? = null,
    val quizPassingScore: Int = 80
)

@Entity(tableName = "lesson_progress", primaryKeys = ["userId", "lessonId"])
data class LessonProgressEntity(
    val userId: String,
    val lessonId: String,
    val courseId: String,
    val progressSeconds: Int = 0,
    val completed: Boolean = false,
    val completedAt: String? = null,
    val lastSeenAt: String = "",
    val isSyncedWithCloud: Boolean = true,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_live_sessions")
data class LiveSessionEntity(
    @PrimaryKey val id: String,
    val courseId: String,
    val courseTitle: String,
    val instructorName: String,
    val title: String,
    val description: String,
    val scheduledAt: String,
    val durationMinutes: Int,
    val status: String,
    val meetingCode: String
)
