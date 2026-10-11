package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workspaces")
data class WorkspaceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val colorHex: String = "#8B5CF6",
    val promptInstructions: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
