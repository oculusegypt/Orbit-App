package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.models.User
import com.example.data.models.UserRole

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey val id: String,
    val email: String,
    val passwordHash: String,
    val displayName: String,
    val role: String, // "LEARNER", "INSTRUCTOR", "ADMIN"
    val phone: String = "",
    val avatarUrl: String = "",
    val bioOrGrade: String = "",
    val isOnline: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class UserAccount(
    val id: String,
    val email: String,
    val displayName: String,
    val role: UserRole,
    val phone: String = "",
    val avatarUrl: String = "",
    val bioOrGrade: String = "",
    val isOnline: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

fun UserAccountEntity.toUserAccount(): UserAccount {
    val userRole = when (role.uppercase()) {
        "INSTRUCTOR", "TEACHER" -> UserRole.INSTRUCTOR
        "ADMIN", "SUPERVISOR" -> UserRole.ADMIN
        else -> UserRole.LEARNER
    }
    return UserAccount(
        id = id,
        email = email,
        displayName = displayName,
        role = userRole,
        phone = phone,
        avatarUrl = avatarUrl,
        bioOrGrade = bioOrGrade,
        isOnline = isOnline,
        createdAt = createdAt
    )
}

fun UserAccountEntity.toUser(): User {
    val userRole = when (role.uppercase()) {
        "INSTRUCTOR", "TEACHER" -> UserRole.INSTRUCTOR
        "ADMIN", "SUPERVISOR" -> UserRole.ADMIN
        else -> UserRole.LEARNER
    }
    return User(
        id = id,
        email = email,
        displayName = displayName,
        role = userRole,
        avatarUrl = avatarUrl,
        organization = if (bioOrGrade.isNotBlank()) bioOrGrade else "أكاديمية مس سارة العربي"
    )
}

fun UserAccount.toUser(): User {
    return User(
        id = id,
        email = email,
        displayName = displayName,
        role = role,
        avatarUrl = avatarUrl,
        organization = if (bioOrGrade.isNotBlank()) bioOrGrade else "أكاديمية مس سارة العربي"
    )
}

fun UserAccount.toEntity(passwordHash: String = "pass123"): UserAccountEntity {
    return UserAccountEntity(
        id = id,
        email = email,
        passwordHash = passwordHash,
        displayName = displayName,
        role = role.name,
        phone = phone,
        avatarUrl = avatarUrl,
        bioOrGrade = bioOrGrade,
        isOnline = isOnline,
        createdAt = createdAt
    )
}
