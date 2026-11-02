package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CourseEntity::class,
        CourseSectionEntity::class,
        LessonEntity::class,
        LessonProgressEntity::class,
        LiveSessionEntity::class,
        UserAccountEntity::class,
        CallSessionEntity::class,
        ConsultationBookingEntity::class,
        PurchaseReceiptEntity::class,
        LearnerAchievementEntity::class,
        TutorMessageEntity::class,
        CertificateEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class OrbitDatabase : RoomDatabase() {

    abstract fun courseDao(): CourseDao
    abstract fun lessonProgressDao(): LessonProgressDao
    abstract fun userAccountDao(): UserAccountDao
    abstract fun callSessionDao(): CallSessionDao
    abstract fun consultationBookingDao(): ConsultationBookingDao
    abstract fun purchaseReceiptDao(): PurchaseReceiptDao
    abstract fun learnerAchievementDao(): LearnerAchievementDao
    abstract fun tutorMessageDao(): TutorMessageDao
    abstract fun certificateDao(): CertificateDao

    companion object {
        @Volatile
        private var INSTANCE: OrbitDatabase? = null

        fun getDatabase(context: Context): OrbitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OrbitDatabase::class.java,
                    "orbit_learn_offline.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
