package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonProgressDao {

    @Query("SELECT * FROM lesson_progress WHERE userId = :userId")
    fun getAllProgressForUser(userId: String): Flow<List<LessonProgressEntity>>

    @Query("SELECT * FROM lesson_progress WHERE userId = :userId AND courseId = :courseId")
    fun getProgressForCourse(userId: String, courseId: String): Flow<List<LessonProgressEntity>>

    @Query("SELECT * FROM lesson_progress WHERE userId = :userId AND lessonId = :lessonId LIMIT 1")
    fun getProgressForLesson(userId: String, lessonId: String): Flow<LessonProgressEntity?>

    @Query("SELECT * FROM lesson_progress WHERE userId = :userId AND lessonId = :lessonId LIMIT 1")
    suspend fun getProgressForLessonDirect(userId: String, lessonId: String): LessonProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: LessonProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(progressList: List<LessonProgressEntity>)

    @Query("UPDATE lesson_progress SET progressSeconds = :seconds, lastSeenAt = :lastSeen, lastUpdatedTimestamp = :now WHERE userId = :userId AND lessonId = :lessonId")
    suspend fun updateWatchSeconds(userId: String, lessonId: String, seconds: Int, lastSeen: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE lesson_progress SET completed = 1, completedAt = :completedAt, lastSeenAt = :completedAt, lastUpdatedTimestamp = :now WHERE userId = :userId AND lessonId = :lessonId")
    suspend fun markCompleted(userId: String, lessonId: String, completedAt: String, now: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM lesson_progress WHERE userId = :userId AND courseId = :courseId AND completed = 1")
    fun getCompletedLessonsCount(userId: String, courseId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM lesson_progress WHERE userId = :userId AND completed = 1")
    fun getTotalCompletedLessonsCount(userId: String): Flow<Int>

    @Query("DELETE FROM lesson_progress WHERE userId = :userId AND lessonId = :lessonId")
    suspend fun deleteProgress(userId: String, lessonId: String)
}
