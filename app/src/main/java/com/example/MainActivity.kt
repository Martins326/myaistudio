package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.local.AppDatabase
import com.example.data.remote.GeminiApiService
import com.example.data.repository.AuthRepository
import com.example.data.repository.ChatRepository
import com.example.data.repository.MemoryRepository
import com.example.data.repository.ResearchRepository
import com.example.ui.components.AppDrawer
import com.example.ui.navigation.NavDestination
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.MemoryScreen
import com.example.ui.screens.ResearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.ResearchViewModel
import com.example.ui.viewmodel.SettingsViewModel
import com.example.ui.viewmodel.ToolsViewModel
import com.example.util.SpeechManager
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var speechManager: SpeechManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Dependency initialization
        val database = AppDatabase.getInstance(this)
        val geminiService = GeminiApiService()
        speechManager = SpeechManager(this)

        val chatRepository = ChatRepository(
            conversationDao = database.conversationDao(),
            messageDao = database.messageDao(),
            userMemoryDao = database.userMemoryDao(),
            geminiService = geminiService
        )
        val authRepository = AuthRepository(database.userAccountDao())
        val memoryRepository = MemoryRepository(database.userMemoryDao())
        val researchRepository = ResearchRepository(geminiService)

        // 2. ViewModels
        val chatViewModel = ChatViewModel(chatRepository)
        val authViewModel = AuthViewModel(authRepository)
        val settingsViewModel = SettingsViewModel(application, chatRepository)
        val researchViewModel = ResearchViewModel(researchRepository)
        val toolsViewModel = ToolsViewModel(geminiService)

        setContent {
            val themeMode by settingsViewModel.themeMode.collectAsState()
            val isDark = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NexaApp(
                        database = database,
                        chatViewModel = chatViewModel,
                        authViewModel = authViewModel,
                        settingsViewModel = settingsViewModel,
                        researchViewModel = researchViewModel,
                        toolsViewModel = toolsViewModel,
                        memoryRepository = memoryRepository,
                        speechManager = speechManager
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechManager.release()
    }
}

@Composable
fun NexaApp(
    database: AppDatabase,
    chatViewModel: ChatViewModel,
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    researchViewModel: ResearchViewModel,
    toolsViewModel: ToolsViewModel,
    memoryRepository: MemoryRepository,
    speechManager: SpeechManager
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var currentDestination by remember { mutableStateOf(NavDestination.CHAT) }

    val activeConversations by chatViewModel.activeConversations.collectAsState()
    val archivedConversations by chatViewModel.archivedConversations.collectAsState()
    val currentConversation by chatViewModel.currentConversation.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    val customApiKey by settingsViewModel.customApiKey.collectAsState()
    val temperature by settingsViewModel.temperature.collectAsState()
    val useStreaming by settingsViewModel.streamingEnabled.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                AppDrawer(
                    conversations = activeConversations,
                    archivedConversations = archivedConversations,
                    currentConversationId = currentConversation?.id,
                    currentDestination = currentDestination,
                    currentUser = currentUser,
                    onSelectConversation = { conv ->
                        chatViewModel.selectConversation(conv)
                        scope.launch { drawerState.close() }
                    },
                    onNewChat = {
                        chatViewModel.newChat()
                        currentDestination = NavDestination.CHAT
                        scope.launch { drawerState.close() }
                    },
                    onNavigate = { dest ->
                        currentDestination = dest
                        scope.launch { drawerState.close() }
                    },
                    onRename = { conv ->
                        chatViewModel.renameConversation(conv.id, "${conv.title} (edited)")
                    },
                    onTogglePin = { conv ->
                        chatViewModel.togglePin(conv)
                    },
                    onToggleArchive = { conv ->
                        chatViewModel.toggleArchive(conv)
                    },
                    onDelete = { conv ->
                        chatViewModel.deleteConversation(conv.id)
                    }
                )
            }
        }
    ) {
        when (currentDestination) {
            NavDestination.CHAT -> {
                ChatScreen(
                    viewModel = chatViewModel,
                    speechManager = speechManager,
                    customApiKey = customApiKey.ifBlank { null },
                    temperature = temperature,
                    useStreaming = useStreaming,
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
            }
            NavDestination.DEEP_RESEARCH -> {
                ResearchScreen(
                    viewModel = researchViewModel,
                    customApiKey = customApiKey.ifBlank { null },
                    onBack = { currentDestination = NavDestination.CHAT },
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
            }
            NavDestination.TOOLS -> {
                ToolsScreen(
                    viewModel = toolsViewModel,
                    customApiKey = customApiKey.ifBlank { null },
                    onBack = { currentDestination = NavDestination.CHAT },
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
            }
            NavDestination.MEMORY -> {
                MemoryScreen(
                    memoryRepository = memoryRepository,
                    onBack = { currentDestination = NavDestination.CHAT },
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
            }
            NavDestination.ADMIN -> {
                AdminScreen(
                    conversationDao = database.conversationDao(),
                    messageDao = database.messageDao(),
                    userAccountDao = database.userAccountDao(),
                    onBack = { currentDestination = NavDestination.CHAT },
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
            }
            NavDestination.SETTINGS -> {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onBack = { currentDestination = NavDestination.CHAT },
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
            }
            NavDestination.AUTH -> {
                AuthScreen(
                    viewModel = authViewModel,
                    onBack = { currentDestination = NavDestination.CHAT },
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
            }
        }
    }
}
