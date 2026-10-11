package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConversationEntity
import com.example.data.model.MessageEntity
import com.example.ui.components.ChatInputBar
import com.example.ui.components.ChatMessageItem
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.ModelSelectorSheet
import com.example.ui.theme.NexaCyanSecondary
import com.example.ui.theme.NexaVioletPrimary
import com.example.ui.viewmodel.ChatViewModel
import com.example.util.SpeechManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    speechManager: SpeechManager,
    customApiKey: String?,
    temperature: Float,
    useStreaming: Boolean,
    onOpenDrawer: () -> Unit
) {
    val currentConv by viewModel.currentConversation.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val streamingBuffer by viewModel.streamingBuffer.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val chatMode by viewModel.chatMode.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var attachedFile by remember { mutableStateOf<String?>(null) }
    var showModelSelector by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }

    // Dialog state for rename / delete
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Auto-scroll to bottom on new message or stream chunk
    LaunchedEffect(messages.size, streamingBuffer.length) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { showModelSelector = true }
                    ) {
                        Text(
                            text = currentConv?.title ?: "NEXA-AI",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NexaVioletPrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(NexaCyanSecondary)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = selectedModel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NexaCyanSecondary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("drawer_menu_button")
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.newChat() },
                        modifier = Modifier.testTag("topbar_new_chat_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "New chat")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Generating progress indicator
            if (isGenerating) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = NexaCyanSecondary,
                    trackColor = NexaVioletPrimary.copy(alpha = 0.2f)
                )
            }

            // Error banner
            AnimatedVisibility(visible = errorMessage != null) {
                if (errorMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = errorMessage ?: "",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 12.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(
                                onClick = { viewModel.clearError() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss error",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Message list
            Box(modifier = Modifier.weight(1f)) {
                if (messages.isEmpty() && !isGenerating) {
                    // Empty state welcome view
                    EmptyChatWelcome(
                        onPromptSuggestion = { suggestion ->
                            inputText = suggestion
                        }
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 4.dp)
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            val isThisMsgStreaming = isGenerating && msg.id == messages.lastOrNull()?.id && msg.role == "assistant"
                            ChatMessageItem(
                                message = if (isThisMsgStreaming && streamingBuffer.isNotBlank()) {
                                    msg.copy(content = streamingBuffer)
                                } else msg,
                                isStreaming = isThisMsgStreaming,
                                onRegenerate = {
                                    viewModel.regenerate(
                                        customApiKey = customApiKey,
                                        temperature = temperature,
                                        useStreaming = useStreaming
                                    )
                                },
                                onEdit = { prevText ->
                                    inputText = prevText
                                },
                                onLike = { viewModel.likeMessage(msg.id, msg.isLiked) },
                                onDislike = { viewModel.dislikeMessage(msg.id, msg.isDisliked) },
                                onSpeak = { text -> speechManager.speak(text) }
                            )
                        }

                        // Bottom breathing room
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }

            // Input Bar
            ChatInputBar(
                input = inputText,
                onInputChange = { inputText = it },
                onSend = { prompt ->
                    val finalPrompt = if (attachedFile != null) {
                        "[Attached Document: $attachedFile]\n\n$prompt"
                    } else prompt

                    viewModel.sendMessage(
                        userPrompt = finalPrompt,
                        customApiKey = customApiKey,
                        temperature = temperature,
                        useStreaming = useStreaming
                    )
                    inputText = ""
                    attachedFile = null
                },
                onStop = { viewModel.stopGeneration() },
                isGenerating = isGenerating,
                isListening = isListening,
                onVoiceClick = {
                    if (isListening) {
                        speechManager.stopListening()
                        isListening = false
                    } else {
                        isListening = true
                        speechManager.startListening(
                            onResult = { text ->
                                isListening = false
                                inputText = if (inputText.isBlank()) text else "$inputText $text"
                            },
                            onError = {
                                isListening = false
                            }
                        )
                    }
                },
                onAttachClick = {
                    // Sample document upload selector
                    attachedFile = "system_architecture_spec.md"
                },
                attachmentName = attachedFile,
                onRemoveAttachment = { attachedFile = null },
                selectedMode = chatMode,
                onModeSelect = { viewModel.setChatMode(it) }
            )
        }
    }

    if (showModelSelector) {
        ModelSelectorSheet(
            selectedModelId = selectedModel,
            onModelSelect = { viewModel.selectModel(it) },
            onDismiss = { showModelSelector = false }
        )
    }

    // Rename Dialog
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Conversation") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = currentConv?.id
                        if (id != null && renameText.isNotBlank()) {
                            viewModel.renameConversation(id, renameText.trim())
                        }
                        showRenameDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Dialog
    if (showDeleteDialog) {
        ConfirmationDialog(
            title = "Delete Conversation?",
            message = "This conversation and all of its messages will be permanently deleted.",
            confirmLabel = "Delete",
            isDestructive = true,
            onConfirm = {
                val id = currentConv?.id
                if (id != null) {
                    viewModel.deleteConversation(id)
                }
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
fun EmptyChatWelcome(onPromptSuggestion: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(NexaVioletPrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Where shall we begin?",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "NEXA-AI is primed for reasoning, code architecture, writing, and deep research.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        val suggestions = listOf(
            "📐 Design a scalable microservices architecture for real-time messaging",
            "🧬 Conduct deep research into quantum computing fault-tolerance",
            "💻 Review this Kotlin coroutine flow code for race conditions",
            "✍️ Draft an executive project update on AI infrastructure"
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            suggestions.forEach { suggestion ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPromptSuggestion(suggestion) }
                ) {
                    Text(
                        text = suggestion,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }
    }
}
