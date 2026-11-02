package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {

    @Query("SELECT * FROM courses ORDER BY title ASC")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE id = :id")
    fun getCourseById(id: String): Flow<CourseEntity?>

    @Query("SELECT * FROM courses WHERE id = :id")
    suspend fun getCourseByIdDirect(id: String): CourseEntity?

    @Query("SELECT * FROM course_sections WHERE courseId = :courseId ORDER BY position ASC")
    fun getSectionsForCourse(courseId: String): Flow<List<CourseSectionEntity>>

    @Query("SELECT * FROM course_sections WHERE courseId = :courseId ORDER BY position ASC")
    suspend fun getSectionsForCourseDirect(courseId: String): List<CourseSectionEntity>

    @Query("SELECT * FROM lessons WHERE sectionId = :sectionId ORDER BY position ASC")
    fun getLessonsForSection(sectionId: String): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE sectionId = :sectionId ORDER BY position ASC")
    suspend fun getLessonsForSectionDirect(sectionId: String): List<LessonEntity>

    @Query("SELECT * FROM lessons WHERE courseId = :courseId ORDER BY position ASC")
    fun getLessonsForCourse(courseId: String): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    suspend fun getLessonById(lessonId: String): LessonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<CourseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSections(sections: List<CourseSectionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<LessonEntity>)

    @Query("SELECT COUNT(*) FROM courses")
    suspend fun getCoursesCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLiveSessions(sessions: List<LiveSessionEntity>)

    @Query("SELECT * FROM cached_live_sessions ORDER BY scheduledAt ASC")
    fun getLiveSessions(): Flow<List<LiveSessionEntity>>
}
