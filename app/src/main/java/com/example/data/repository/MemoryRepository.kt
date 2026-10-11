package com.example.data.repository

import com.example.data.local.UserMemoryDao
import com.example.data.model.UserMemoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MemoryRepository(private val memoryDao: UserMemoryDao) {

    val allMemories: Flow<List<UserMemoryEntity>> = memoryDao.getAllMemories()

    suspend fun addMemory(key: String, content: String, category: String = "general"): Long =
        withContext(Dispatchers.IO) {
            memoryDao.insertMemory(
                UserMemoryEntity(
                    key = key.trim(),
                    content = content.trim(),
                    category = category.trim(),
                    isEnabled = true
                )
            )
        }

    suspend fun updateMemory(memory: UserMemoryEntity) = withContext(Dispatchers.IO) {
        memoryDao.updateMemory(memory)
    }

    suspend fun deleteMemory(id: Long) = withContext(Dispatchers.IO) {
        memoryDao.deleteMemoryById(id)
    }

    suspend fun setEnabled(id: Long, isEnabled: Boolean) = withContext(Dispatchers.IO) {
        memoryDao.setEnabled(id, isEnabled)
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        memoryDao.clearAllMemories()
    }
}
