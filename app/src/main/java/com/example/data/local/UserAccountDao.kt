package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.UserAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserAccountDao {
    @Query("SELECT * FROM user_accounts WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserAccountEntity?

    @Query("SELECT * FROM user_accounts WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserAccountEntity?

    @Query("SELECT * FROM user_accounts WHERE id = :id LIMIT 1")
    fun getUserById(id: Long): Flow<UserAccountEntity?>

    @Query("SELECT * FROM user_accounts ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserAccountEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserAccountEntity): Long

    @Update
    suspend fun updateUser(user: UserAccountEntity)

    @Query("UPDATE user_accounts SET lastLoginAt = :timestamp WHERE id = :id")
    suspend fun updateLastLogin(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM user_accounts WHERE id = :id")
    suspend fun deleteUser(id: Long)

    @Query("SELECT COUNT(*) FROM user_accounts")
    fun countUsers(): Flow<Int>
}
