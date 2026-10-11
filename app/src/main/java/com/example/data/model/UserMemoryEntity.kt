package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_memories")
data class UserMemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val key: String,
    val content: String,
    val category: String = "general", // "preference", "fact", "instruction", "project"
    val createdAt: Long = System.currentTimeMillis(),
    val isEnabled: Boolean = true
)
