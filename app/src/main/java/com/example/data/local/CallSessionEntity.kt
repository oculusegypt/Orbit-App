package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_sessions")
data class CallSessionEntity(
    @PrimaryKey val callId: String,
    val callerId: String,
    val callerName: String,
    val callerRole: String,
    val callerAvatar: String = "",
    val receiverId: String,
    val receiverName: String,
    val receiverRole: String,
    val receiverAvatar: String = "",
    val callType: String, // "VIDEO_SCREEN", "AUDIO"
    val durationSeconds: Int = 0,
    val hadScreenShare: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "COMPLETED" // "COMPLETED", "MISSED", "REJECTED"
)

enum class CallType(val labelAr: String) {
    VIDEO_SCREEN("فيديو ومشاركة شاشة"),
    AUDIO("اتصال صوتي")
}

enum class CallStatus {
    IDLE,
    OUTGOING_RINGING,
    INCOMING_RINGING,
    CONNECTED,
    ENDED
}

enum class ScreenShareContent(val labelAr: String) {
    SOROBAN_BOARD("عداد السوربان التفاعلي المباشر"),
    MATH_WORKSHEET("شيت التمارين والمسائل الحسابية"),
    WHITEBOARD("السبورة الذكية والشرح بالقلم"),
    DEVICE_SCREEN("مشاركة الشاشة الحية")
}
