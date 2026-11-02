package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserAccountDao {

    @Query("SELECT * FROM user_accounts ORDER BY createdAt DESC")
    fun getAllUsersFlow(): Flow<List<UserAccountEntity>>

    @Query("SELECT * FROM user_accounts ORDER BY createdAt DESC")
    suspend fun getAllUsers(): List<UserAccountEntity>

    @Query("SELECT * FROM user_accounts WHERE role = :role ORDER BY displayName ASC")
    fun getUsersByRoleFlow(role: String): Flow<List<UserAccountEntity>>

    @Query("SELECT * FROM user_accounts WHERE id = :id LIMIT 1")
    fun getUserByIdFlow(id: String): Flow<UserAccountEntity?>

    @Query("SELECT * FROM user_accounts WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserAccountEntity?

    @Query("SELECT * FROM user_accounts WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserAccountEntity?

    @Query("SELECT * FROM user_accounts WHERE email = :email AND passwordHash = :passwordHash LIMIT 1")
    suspend fun authenticate(email: String, passwordHash: String): UserAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserAccountEntity>)

    @Update
    suspend fun updateUser(user: UserAccountEntity)

    @Delete
    suspend fun deleteUser(user: UserAccountEntity)

    @Query("UPDATE user_accounts SET isOnline = :isOnline WHERE id = :userId")
    suspend fun setOnlineStatus(userId: String, isOnline: Boolean)

    @Query("SELECT COUNT(*) FROM user_accounts")
    suspend fun getUsersCount(): Int
}
