package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ConversationEntity
import com.example.data.model.MessageEntity
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

    val activeConversations: StateFlow<List<ConversationEntity>> =
        chatRepository.activeConversations.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val archivedConversations: StateFlow<List<ConversationEntity>> =
        chatRepository.archivedConversations.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentConversation = MutableStateFlow<ConversationEntity?>(null)
    val currentConversation: StateFlow<ConversationEntity?> = _currentConversation.asStateFlow()

    private val _messages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val messages: StateFlow<List<MessageEntity>> = _messages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _streamingBuffer = MutableStateFlow("")
    val streamingBuffer: StateFlow<String> = _streamingBuffer.asStateFlow()

    private val _selectedModel = MutableStateFlow("gemini-3.5-flash")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<ConversationEntity>>(emptyList())
    val searchResults: StateFlow<List<ConversationEntity>> = _searchResults.asStateFlow()

    // Mode: "chat", "web", "deep_research", "code", "write", "math"
    private val _chatMode = MutableStateFlow("chat")
    val chatMode: StateFlow<String> = _chatMode.asStateFlow()

    private var activeJob: Job? = null
    private var messageCollectionJob: Job? = null

    init {
        // Observe first conversation when available
        viewModelScope.launch {
            activeConversations.collect { list ->
                if (_currentConversation.value == null && list.isNotEmpty()) {
                    selectConversation(list.first())
                }
            }
        }
    }

    fun setChatMode(mode: String) {
        _chatMode.value = mode
    }

    fun selectModel(model: String) {
        _selectedModel.value = model
    }

    fun selectConversation(conversation: ConversationEntity) {
        _currentConversation.value = conversation
        _selectedModel.value = conversation.model
        observeMessages(conversation.id)
    }

    private fun observeMessages(conversationId: Long) {
        messageCollectionJob?.cancel()
        messageCollectionJob = viewModelScope.launch {
            chatRepository.getMessages(conversationId).collect { msgs ->
                _messages.value = msgs
            }
        }
    }

    fun newChat(title: String = "New Chat", workspaceId: Long? = null, instructions: String = "") {
        viewModelScope.launch {
            val newId = chatRepository.createConversation(
                title = title,
                model = _selectedModel.value,
                workspaceId = workspaceId,
                systemPrompt = instructions
            )
            val newConv = ConversationEntity(
                id = newId,
                title = title,
                model = _selectedModel.value,
                workspaceId = workspaceId,
                systemPrompt = instructions
            )
            selectConversation(newConv)
        }
    }

    fun sendMessage(
        userPrompt: String,
        customApiKey: String? = null,
        temperature: Float = 0.7f,
        useStreaming: Boolean = true
    ) {
        if (userPrompt.isBlank() || _isGenerating.value) return

        val conv = _currentConversation.value ?: return
        _isGenerating.value = true
        _streamingBuffer.value = ""
        _errorMessage.value = null

        activeJob = viewModelScope.launch {
            chatRepository.sendMessage(
                conversationId = conv.id,
                userPrompt = userPrompt,
                model = _selectedModel.value,
                customApiKey = customApiKey,
                temperature = temperature,
                useStreaming = useStreaming,
                workspaceInstructions = conv.systemPrompt,
                onChunk = { chunk ->
                    _streamingBuffer.value += chunk
                },
                onComplete = {
                    _isGenerating.value = false
                    _streamingBuffer.value = ""
                },
                onError = { err ->
                    _isGenerating.value = false
                    _errorMessage.value = err
                    _streamingBuffer.value = ""
                }
            )
        }
    }

    fun stopGeneration() {
        activeJob?.cancel()
        _isGenerating.value = false
        _streamingBuffer.value = ""
    }

    fun regenerate(
        customApiKey: String? = null,
        temperature: Float = 0.7f,
        useStreaming: Boolean = true
    ) {
        val conv = _currentConversation.value ?: return
        if (_isGenerating.value) return

        _isGenerating.value = true
        _streamingBuffer.value = ""
        _errorMessage.value = null

        activeJob = viewModelScope.launch {
            chatRepository.regenerateLastMessage(
                conversationId = conv.id,
                model = _selectedModel.value,
                customApiKey = customApiKey,
                temperature = temperature,
                useStreaming = useStreaming,
                workspaceInstructions = conv.systemPrompt,
                onChunk = { chunk -> _streamingBuffer.value += chunk },
                onComplete = {
                    _isGenerating.value = false
                    _streamingBuffer.value = ""
                },
                onError = { err ->
                    _isGenerating.value = false
                    _errorMessage.value = err
                    _streamingBuffer.value = ""
                }
            )
        }
    }

    fun editAndResend(
        messageId: Long,
        newPrompt: String,
        customApiKey: String? = null,
        temperature: Float = 0.7f,
        useStreaming: Boolean = true
    ) {
        val conv = _currentConversation.value ?: return
        if (newPrompt.isBlank() || _isGenerating.value) return

        _isGenerating.value = true
        _streamingBuffer.value = ""
        _errorMessage.value = null

        activeJob = viewModelScope.launch {
            chatRepository.editAndResend(
                conversationId = conv.id,
                userMessageId = messageId,
                newPrompt = newPrompt,
                model = _selectedModel.value,
                customApiKey = customApiKey,
                temperature = temperature,
                useStreaming = useStreaming,
                workspaceInstructions = conv.systemPrompt,
                onChunk = { chunk -> _streamingBuffer.value += chunk },
                onComplete = {
                    _isGenerating.value = false
                    _streamingBuffer.value = ""
                },
                onError = { err ->
                    _isGenerating.value = false
                    _errorMessage.value = err
                    _streamingBuffer.value = ""
                }
            )
        }
    }

    fun renameConversation(id: Long, newTitle: String) {
        viewModelScope.launch {
            chatRepository.renameConversation(id, newTitle)
            if (_currentConversation.value?.id == id) {
                _currentConversation.value = _currentConversation.value?.copy(title = newTitle)
            }
        }
    }

    fun togglePin(conversation: ConversationEntity) {
        viewModelScope.launch {
            chatRepository.setPinned(conversation.id, !conversation.isPinned)
        }
    }

    fun toggleArchive(conversation: ConversationEntity) {
        viewModelScope.launch {
            chatRepository.setArchived(conversation.id, !conversation.isArchived)
            if (_currentConversation.value?.id == conversation.id) {
                _currentConversation.value = null
            }
        }
    }

    fun deleteConversation(id: Long) {
        viewModelScope.launch {
            chatRepository.deleteConversation(id)
            if (_currentConversation.value?.id == id) {
                _currentConversation.value = null
            }
        }
    }

    fun likeMessage(messageId: Long, current: Boolean) {
        viewModelScope.launch {
            chatRepository.setLiked(messageId, !current)
        }
    }

    fun dislikeMessage(messageId: Long, current: Boolean) {
        viewModelScope.launch {
            chatRepository.setDisliked(messageId, !current)
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            chatRepository.searchConversations(query).collect {
                _searchResults.value = it
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
