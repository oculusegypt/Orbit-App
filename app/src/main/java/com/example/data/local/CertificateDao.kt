package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Durable certificates store. Verification queries hit this table first so
 * codes issued in previous sessions remain valid after a restart; seed/demo
 * certificates stay in the in-memory catalog as a fallback only.
 */
@Dao
interface CertificateDao {

    @Query("SELECT * FROM certificates ORDER BY recordedAt DESC")
    fun getAllCertificatesFlow(): Flow<List<CertificateEntity>>

    @Query("SELECT * FROM certificates WHERE userId = :userId ORDER BY recordedAt DESC")
    fun getCertificatesForUserFlow(userId: String): Flow<List<CertificateEntity>>

    @Query(
        "SELECT * FROM certificates " +
            "WHERE TRIM(verificationCode) COLLATE NOCASE = TRIM(:code) COLLATE NOCASE LIMIT 1"
    )
    suspend fun findByVerificationCode(code: String): CertificateEntity?

    @Query(
        "SELECT COUNT(*) FROM certificates WHERE courseId = :courseId AND userId = :userId"
    )
    suspend fun countFor(courseId: String, userId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCertificate(certificate: CertificateEntity)
}
