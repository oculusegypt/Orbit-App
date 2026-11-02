package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Durable learner achievement record (mental-math XP & best streak).
 * Kept per-user so progress survives restarts and feeds the learner portal,
 * the leaderboard surface and the admin analytics without any in-memory state.
 */
@Entity(tableName = "learner_achievements")
data class LearnerAchievementEntity(
    @PrimaryKey val userId: String,
    val xpTotal: Int = 0,
    val bestStreak: Int = 0,
    val gamesPlayed: Int = 0,
    val correctAnswers: Int = 0,
    val lastPlayedAt: Long = 0L
)
