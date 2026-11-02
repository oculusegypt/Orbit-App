package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Durable storage for consultation bookings (lead funnel). Reads are reactive
 * via [Flow] so instructor/admin screens update live when a parent books.
 */
@Dao
interface ConsultationBookingDao {

    @Query("SELECT * FROM consultation_bookings ORDER BY createdAt DESC")
    fun getAllBookingsFlow(): Flow<List<ConsultationBookingEntity>>

    @Query("SELECT * FROM consultation_bookings WHERE status = :status ORDER BY createdAt DESC")
    fun getBookingsByStatusFlow(status: String): Flow<List<ConsultationBookingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: ConsultationBookingEntity)

    @Query("UPDATE consultation_bookings SET status = :newStatus WHERE id = :bookingId")
    suspend fun updateStatus(bookingId: String, newStatus: String)

    @Query("SELECT COUNT(*) FROM consultation_bookings")
    suspend fun getBookingsCount(): Int

    @Query("SELECT COUNT(*) FROM consultation_bookings WHERE status = 'PENDING'")
    suspend fun getPendingCount(): Int
}
