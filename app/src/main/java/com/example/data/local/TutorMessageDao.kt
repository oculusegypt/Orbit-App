package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TutorMessageDao {

    @Query("SELECT * FROM tutor_messages WHERE userId = :userId ORDER BY createdAt ASC")
    fun getConversationFlow(userId: String): Flow<List<TutorMessageEntity>>

    @Query(
        "SELECT * FROM tutor_messages WHERE userId = :userId " +
            "ORDER BY createdAt DESC LIMIT :limit"
    )
    suspend fun getRecentTurns(userId: String, limit: Int): List<TutorMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: TutorMessageEntity)

    @Query("SELECT COUNT(*) FROM tutor_messages WHERE userId = :userId AND role = 'user'")
    suspend fun getUserQuestionCount(userId: String): Int
}
