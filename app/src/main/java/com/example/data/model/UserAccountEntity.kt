package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_accounts",
    indices = [Index(value = ["email"], unique = true), Index(value = ["username"], unique = true)]
)
data class UserAccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val email: String,
    val passwordHash: String,
    val salt: String,
    val role: String = "user", // "user" or "admin"
    val avatarColor: Long = 0xFF8B5CF6,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis()
)
