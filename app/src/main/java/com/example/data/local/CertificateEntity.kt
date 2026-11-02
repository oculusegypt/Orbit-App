package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Durable certificate record. Issued certificates are written here at issue
 * time so verification works across app restarts — a graduate can close the
 * app, reopen it weeks later, and still verify their certificate code.
 */
@Entity(tableName = "certificates")
data class CertificateEntity(
    @PrimaryKey val id: String,
    val enrollmentId: String,
    val courseId: String,
    val courseTitle: String,
    val userId: String,
    val userName: String,
    val instructorName: String,
    val verificationCode: String,
    val issuedAt: String,
    val gradePercentage: Int,
    /** Comma-separated skill list (kept denormalized for simple storage). */
    val skillsVerifiedCsv: String = "",
    val recordedAt: Long = System.currentTimeMillis()
)
