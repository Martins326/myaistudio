package com.example.data.repository

import com.example.data.local.ConversationDao
import com.example.data.local.MessageDao
import com.example.data.local.UserMemoryDao
import com.example.data.model.ConversationEntity
import com.example.data.model.MessageEntity
import com.example.data.remote.GeminiApiService
import com.example.data.remote.GeminiContent
import com.example.data.remote.GeminiGenerationConfig
import com.example.data.remote.GeminiPart
import com.example.data.remote.GeminiRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ChatRepository(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val userMemoryDao: UserMemoryDao,
    private val geminiService: GeminiApiService
) {
    val activeConversations: Flow<List<ConversationEntity>> = conversationDao.getActiveConversations()
    val archivedConversations: Flow<List<ConversationEntity>> = conversationDao.getArchivedConversations()

    fun getMessages(conversationId: Long): Flow<List<MessageEntity>> =
        messageDao.getMessagesByConversationId(conversationId)

    fun searchConversations(query: String): Flow<List<ConversationEntity>> =
        conversationDao.searchConversations(query)

    suspend fun createConversation(
        title: String = "New Chat",
        model: String = "gemini-3.5-flash",
        workspaceId: Long? = null,
        systemPrompt: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val entity = ConversationEntity(
            title = title,
            model = model,
            workspaceId = workspaceId,
            systemPrompt = systemPrompt
        )
        conversationDao.insertConversation(entity)
    }

    suspend fun deleteConversation(id: Long) = withContext(Dispatchers.IO) {
        conversationDao.deleteConversationById(id)
    }

    suspend fun setPinned(id: Long, isPinned: Boolean) = withContext(Dispatchers.IO) {
        conversationDao.setPinned(id, isPinned)
    }

    suspend fun setArchived(id: Long, isArchived: Boolean) = withContext(Dispatchers.IO) {
        conversationDao.setArchived(id, isArchived)
    }

    suspend fun renameConversation(id: Long, title: String) = withContext(Dispatchers.IO) {
        conversationDao.updateTitle(id, title)
    }

    suspend fun setLiked(messageId: Long, isLiked: Boolean) = withContext(Dispatchers.IO) {
        messageDao.setLiked(messageId, isLiked)
    }

    suspend fun setDisliked(messageId: Long, isDisliked: Boolean) = withContext(Dispatchers.IO) {
        messageDao.setDisliked(messageId, isDisliked)
    }

    suspend fun deleteAllConversations() = withContext(Dispatchers.IO) {
        conversationDao.deleteAllConversations()
    }

    /**
     * Sends a user message, stores it, calls Gemini with conversational history + user memories,
     * and streams or directly generates the response.
     */
    suspend fun sendMessage(
        conversationId: Long,
        userPrompt: String,
        model: String,
        customApiKey: String? = null,
        temperature: Float = 0.7f,
        useStreaming: Boolean = true,
        workspaceInstructions: String = "",
        onChunk: (String) -> Unit,
        onComplete: (MessageEntity) -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        // 1. Insert user message
        val userMsgId = messageDao.insertMessage(
            MessageEntity(
                conversationId = conversationId,
                role = "user",
                content = userPrompt,
                modelUsed = model
            )
        )

        // Auto-update conversation title if it's the first message
        val existingMessages = messageDao.getMessagesByConversationId(conversationId).first()
        if (existingMessages.size <= 2) {
            val titleCandidate = if (userPrompt.length > 32) userPrompt.take(32) + "..." else userPrompt
            conversationDao.updateTitle(conversationId, titleCandidate.replace("\n", " ").trim())
        }

        // 2. Insert placeholder assistant message
        val assistantMsgId = messageDao.insertMessage(
            MessageEntity(
                conversationId = conversationId,
                role = "assistant",
                content = "",
                modelUsed = model
            )
        )

        // 3. Compile system prompt (System instructions + Opt-in user memories)
        val enabledMemories = userMemoryDao.getEnabledMemories().first()
        val memoryContext = if (enabledMemories.isNotEmpty()) {
            "\n[User-Controlled Preferences & Memory]:\n" + enabledMemories.joinToString("\n") { "- ${it.key}: ${it.content}" }
        } else ""

        val fullSystemPrompt = buildString {
            append("You are NEXA-AI, a world-class, intelligent assistant platform. ")
            append("Format code snippets with markdown backticks specifying the programming language. ")
            if (workspaceInstructions.isNotBlank()) {
                append("\n[Workspace Context]: $workspaceInstructions\n")
            }
            append(memoryContext)
        }

        // 4. Build Gemini conversational history (send last 15 messages for context window efficiency)
        val historyTurns = existingMessages.takeLast(15).mapNotNull { msg ->
            if (msg.role == "user" || msg.role == "assistant") {
                GeminiContent(
                    role = if (msg.role == "assistant") "model" else "user",
                    parts = listOf(GeminiPart(text = msg.content))
                )
            } else null
        }

        val request = GeminiRequest(
            contents = historyTurns + listOf(
                GeminiContent(role = "user", parts = listOf(GeminiPart(text = userPrompt)))
            ),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = fullSystemPrompt))),
            generationConfig = GeminiGenerationConfig(
                temperature = temperature,
                topP = 0.95f
            )
        )

        // 5. Call API
        val accumulatedText = StringBuilder()
        if (useStreaming) {
            val result = geminiService.streamGenerateContent(
                model = model,
                request = request,
                customKey = customApiKey,
                onChunk = { chunk ->
                    accumulatedText.append(chunk)
                    onChunk(chunk)
                }
            )

            result.fold(
                onSuccess = { (finalText, tokens) ->
                    val textToSave = if (finalText.isNotBlank()) finalText else accumulatedText.toString()
                    messageDao.updateContent(assistantMsgId, textToSave, isError = false)
                    val saved = MessageEntity(
                        id = assistantMsgId,
                        conversationId = conversationId,
                        role = "assistant",
                        content = textToSave,
                        tokenCount = tokens,
                        modelUsed = model
                    )
                    onComplete(saved)
                },
                onFailure = { ex ->
                    val errorMsg = ex.message ?: "Failed to generate AI response"
                    val fallbackContent = accumulatedText.ifBlank { "⚠️ Error: $errorMsg" }.toString()
                    messageDao.updateContent(assistantMsgId, fallbackContent, isError = true)
                    onError(errorMsg)
                }
            )
        } else {
            val result = geminiService.generateContent(
                model = model,
                request = request,
                customKey = customApiKey
            )

            result.fold(
                onSuccess = { (text, tokens) ->
                    messageDao.updateContent(assistantMsgId, text, isError = false)
                    val saved = MessageEntity(
                        id = assistantMsgId,
                        conversationId = conversationId,
                        role = "assistant",
                        content = text,
                        tokenCount = tokens,
                        modelUsed = model
                    )
                    onComplete(saved)
                },
                onFailure = { ex ->
                    val errorMsg = ex.message ?: "Failed to generate AI response"
                    messageDao.updateContent(assistantMsgId, "⚠️ Error: $errorMsg", isError = true)
                    onError(errorMsg)
                }
            )
        }
    }

    suspend fun regenerateLastMessage(
        conversationId: Long,
        model: String,
        customApiKey: String? = null,
        temperature: Float = 0.7f,
        useStreaming: Boolean = true,
        workspaceInstructions: String = "",
        onChunk: (String) -> Unit,
        onComplete: (MessageEntity) -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        val messages = messageDao.getMessagesByConversationId(conversationId).first()
        val lastAssistant = messages.lastOrNull { it.role == "assistant" }
        val lastUser = messages.lastOrNull { it.role == "user" }

        if (lastUser == null) {
            onError("No previous user message to regenerate.")
            return@withContext
        }

        // Delete last assistant message if present
        if (lastAssistant != null) {
            messageDao.deleteMessageById(lastAssistant.id)
        }

        // Resend using the last user prompt
        sendMessage(
            conversationId = conversationId,
            userPrompt = lastUser.content,
            model = model,
            customApiKey = customApiKey,
            temperature = temperature,
            useStreaming = useStreaming,
            workspaceInstructions = workspaceInstructions,
            onChunk = onChunk,
            onComplete = onComplete,
            onError = onError
        )
    }

    suspend fun editAndResend(
        conversationId: Long,
        userMessageId: Long,
        newPrompt: String,
        model: String,
        customApiKey: String? = null,
        temperature: Float = 0.7f,
        useStreaming: Boolean = true,
        workspaceInstructions: String = "",
        onChunk: (String) -> Unit,
        onComplete: (MessageEntity) -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        // Delete this user message and everything after it
        messageDao.deleteMessagesFrom(conversationId, userMessageId)

        // Now send new message
        sendMessage(
            conversationId = conversationId,
            userPrompt = newPrompt,
            model = model,
            customApiKey = customApiKey,
            temperature = temperature,
            useStreaming = useStreaming,
            workspaceInstructions = workspaceInstructions,
            onChunk = onChunk,
            onComplete = onComplete,
            onError = onError
        )
    }
}
