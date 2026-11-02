package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Durable purchase receipt written at checkout commit time. Revenue dashboards
 * aggregate real paid amounts (in minor currency units) straight from this
 * table instead of relying on ephemeral in-memory state.
 */
@Entity(tableName = "purchase_receipts")
data class PurchaseReceiptEntity(
    @PrimaryKey val orderId: String,
    val userId: String,
    val courseId: String,
    val courseTitle: String,
    val paidAmountMinor: Int,
    val currency: String,
    val paymentMethod: String,
    val campaignId: String?,
    val discountPercent: Int,
    val referenceCode: String,
    val createdAt: String,
    val recordedAt: Long = System.currentTimeMillis()
)
