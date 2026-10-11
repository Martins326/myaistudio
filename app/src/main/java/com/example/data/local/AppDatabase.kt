package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ConversationEntity
import com.example.data.model.MessageEntity
import com.example.data.model.UserAccountEntity
import com.example.data.model.UserMemoryEntity
import com.example.data.model.WorkspaceEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        UserMemoryEntity::class,
        UserAccountEntity::class,
        WorkspaceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun userMemoryDao(): UserMemoryDao
    abstract fun userAccountDao(): UserAccountDao
    abstract fun workspaceDao(): WorkspaceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nexa_ai_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate default sample data in coroutine
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getInstance(context)
                                populateInitialData(database)
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            // Default Workspaces
            val wsGeneral = db.workspaceDao().insertWorkspace(
                WorkspaceEntity(
                    name = "General AI",
                    description = "Everyday reasoning, creative ideas, and versatile assistance",
                    colorHex = "#8B5CF6",
                    promptInstructions = "You are NEXA-AI, an advanced, insightful, and adaptable assistant."
                )
            )
            db.workspaceDao().insertWorkspace(
                WorkspaceEntity(
                    name = "Engineering & Code",
                    description = "Software architecture, refactoring, debugging, and review",
                    colorHex = "#06B6D4",
                    promptInstructions = "You are NEXA-AI Software Architect. Provide clean, secure, production-grade code with optimal time/space complexity."
                )
            )
            db.workspaceDao().insertWorkspace(
                WorkspaceEntity(
                    name = "Deep Research",
                    description = "Analytical investigation, structured synthesis, and citations",
                    colorHex = "#10B981",
                    promptInstructions = "You are NEXA-AI Research Director. Formulate methodical analyses, rigorous breakdowns, and verify factual details."
                )
            )

            // Initial Conversation
            val convId = db.conversationDao().insertConversation(
                ConversationEntity(
                    title = "Welcome to NEXA-AI",
                    isPinned = true,
                    workspaceId = wsGeneral,
                    model = "gemini-3.5-flash"
                )
            )

            db.messageDao().insertMessage(
                MessageEntity(
                    conversationId = convId,
                    role = "assistant",
                    content = """
                        # Welcome to **NEXA-AI** 🚀
                        
                        I am your full-stack, multimodal AI assistant platform powered by Google Gemini.
                        
                        ### What I can do for you:
                        * **Multimodal Intelligence**: Analyze uploaded images, documents, and charts.
                        * **Deep Web Research**: Methodical question decomposition, evidence gathering, and structured reporting.
                        * **Software Engineering**: Code generation, syntax highlighting, refactoring, test generation, and debugging.
                        * **Writing & Creativity**: Drafting emails, essays, summaries, translations, and tone transformations.
                        * **Voice & Speech**: Real-time voice dictation and high-fidelity text-to-speech read-aloud.
                        * **User Memory**: Opt-in memory persistence so I can remember your key preferences across sessions.
                        
                        Type a question below or switch modes using the toolbar to begin!
                    """.trimIndent(),
                    modelUsed = "gemini-3.5-flash"
                )
            )

            // Seed initial memory example
            db.userMemoryDao().insertMemory(
                UserMemoryEntity(
                    key = "Primary Goal",
                    content = "Prefers clear, production-ready, well-formatted technical responses with code syntax highlighting.",
                    category = "preference"
                )
            )
        }
    }
}
