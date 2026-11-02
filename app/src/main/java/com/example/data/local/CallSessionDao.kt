package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CallSessionDao {

    @Query("SELECT * FROM call_sessions ORDER BY timestamp DESC")
    fun getAllCallsFlow(): Flow<List<CallSessionEntity>>

    @Query("SELECT * FROM call_sessions WHERE callerId = :userId OR receiverId = :userId ORDER BY timestamp DESC")
    fun getCallsForUserFlow(userId: String): Flow<List<CallSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCall(call: CallSessionEntity)

    @Query("SELECT COUNT(*) FROM call_sessions")
    suspend fun getCallsCount(): Int
}
