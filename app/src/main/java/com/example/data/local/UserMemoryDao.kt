package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.UserMemoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserMemoryDao {
    @Query("SELECT * FROM user_memories ORDER BY createdAt DESC")
    fun getAllMemories(): Flow<List<UserMemoryEntity>>

    @Query("SELECT * FROM user_memories WHERE isEnabled = 1 ORDER BY createdAt DESC")
    fun getEnabledMemories(): Flow<List<UserMemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: UserMemoryEntity): Long

    @Update
    suspend fun updateMemory(memory: UserMemoryEntity)

    @Query("UPDATE user_memories SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setEnabled(id: Long, isEnabled: Boolean)

    @Query("DELETE FROM user_memories WHERE id = :id")
    suspend fun deleteMemoryById(id: Long)

    @Query("DELETE FROM user_memories")
    suspend fun clearAllMemories()

    @Query("SELECT COUNT(*) FROM user_memories")
    fun countMemories(): Flow<Int>
}
