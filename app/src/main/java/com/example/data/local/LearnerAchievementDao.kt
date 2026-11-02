package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LearnerAchievementDao {

    @Query("SELECT * FROM learner_achievements WHERE userId = :userId LIMIT 1")
    fun getAchievementFlow(userId: String): Flow<LearnerAchievementEntity?>

    @Query("SELECT * FROM learner_achievements ORDER BY xpTotal DESC")
    fun getLeaderboardFlow(): Flow<List<LearnerAchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(achievement: LearnerAchievementEntity)

    /** Atomic accumulate used after every finished mental-math round. */
    @Query(
        """
        INSERT INTO learner_achievements (userId, xpTotal, bestStreak, gamesPlayed, correctAnswers, lastPlayedAt)
        VALUES (:userId, :xpDelta, :streak, 1, :correct, :now)
        ON CONFLICT(userId) DO UPDATE SET
            xpTotal = xpTotal + :xpDelta,
            bestStreak = MAX(bestStreak, :streak),
            gamesPlayed = gamesPlayed + 1,
            correctAnswers = correctAnswers + :correct,
            lastPlayedAt = :now
        """
    )
    suspend fun accumulate(
        userId: String,
        xpDelta: Int,
        streak: Int,
        correct: Int,
        now: Long = System.currentTimeMillis()
    )
}
