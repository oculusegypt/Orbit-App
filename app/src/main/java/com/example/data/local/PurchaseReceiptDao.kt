package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Durable purchase receipts. The admin revenue KPI aggregates SUM of real
 * paid amounts from this table — never from in-memory seed data.
 */
@Dao
interface PurchaseReceiptDao {

    @Query("SELECT * FROM purchase_receipts ORDER BY recordedAt DESC")
    fun getAllReceiptsFlow(): Flow<List<PurchaseReceiptEntity>>

    @Query("SELECT * FROM purchase_receipts WHERE userId = :userId ORDER BY recordedAt DESC")
    fun getReceiptsForUserFlow(userId: String): Flow<List<PurchaseReceiptEntity>>

    @Query("SELECT COALESCE(SUM(paidAmountMinor), 0) FROM purchase_receipts")
    fun getTotalRevenueMinorFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM purchase_receipts")
    suspend fun getReceiptsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: PurchaseReceiptEntity)
}
