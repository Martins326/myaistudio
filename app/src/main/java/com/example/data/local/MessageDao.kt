package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC, id ASC")
    fun getMessagesByConversationId(conversationId: Long): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("UPDATE messages SET content = :content, isError = :isError WHERE id = :id")
    suspend fun updateContent(id: Long, content: String, isError: Boolean = false)

    @Query("UPDATE messages SET isLiked = :isLiked, isDisliked = 0 WHERE id = :id")
    suspend fun setLiked(id: Long, isLiked: Boolean)

    @Query("UPDATE messages SET isDisliked = :isDisliked, isLiked = 0 WHERE id = :id")
    suspend fun setDisliked(id: Long, isDisliked: Boolean)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessageById(id: Long)

    @Query("DELETE FROM messages WHERE conversationId = :conversationId AND id >= :fromMessageId")
    suspend fun deleteMessagesFrom(conversationId: Long, fromMessageId: Long)

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY id DESC LIMIT 1")
    suspend fun getLastMessage(conversationId: Long): MessageEntity?

    @Query("SELECT COUNT(*) FROM messages")
    fun countTotalMessages(): Flow<Int>

    @Query("SELECT SUM(tokenCount) FROM messages")
    fun sumTotalTokens(): Flow<Int?>
}
