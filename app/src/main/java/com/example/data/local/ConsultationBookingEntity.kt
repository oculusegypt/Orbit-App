package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.models.ConsultationBooking

/**
 * Room-backed consultation booking. This makes the lead funnel durable:
 * bookings survive app restarts and are visible to the instructor/admin
 * pipelines from any session, not just the in-memory one that created them.
 */
@Entity(tableName = "consultation_bookings")
data class ConsultationBookingEntity(
    @PrimaryKey val id: String,
    val parentName: String,
    val childName: String,
    val childAge: String,
    val phone: String,
    val service: String,
    val status: String, // PENDING | CONTACTED | SEATED
    val createdAt: String
)

fun ConsultationBooking.toEntity() = ConsultationBookingEntity(
    id = id,
    parentName = parentName,
    childName = childName,
    childAge = childAge,
    phone = phone,
    service = service,
    status = status,
    createdAt = createdAt
)

fun ConsultationBookingEntity.toModel() = ConsultationBooking(
    id = id,
    parentName = parentName,
    childName = childName,
    childAge = childAge,
    phone = phone,
    service = service,
    status = status,
    createdAt = createdAt
)
