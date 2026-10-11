package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ConfirmationDialog
import com.example.ui.theme.NexaCyanSecondary
import com.example.ui.theme.NexaVioletPrimary
import com.example.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onOpenDrawer: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    val customKey by viewModel.customApiKey.collectAsState()
    val defaultModel by viewModel.defaultModel.collectAsState()
    val streamingEnabled by viewModel.streamingEnabled.collectAsState()
    val temperature by viewModel.temperature.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    var keyInput by remember { mutableStateOf(customKey) }
    var tempSlider by remember { mutableFloatStateOf(temperature) }
    var showClearConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(statusMessage) {
        if (statusMessage != null) {
            Toast.makeText(context, statusMessage, Toast.LENGTH_SHORT).show()
            viewModel.clearStatus()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = NexaCyanSecondary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Settings & Intelligence", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) { Icon(Icons.Default.Menu, contentDescription = "Menu") }
                },
                actions = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // API Key Configuration
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = NexaCyanSecondary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gemini API Credentials", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "AI Studio automatically provides a Gemini API key at runtime. You can also provide an override API key below.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = keyInput,
                            onValueChange = { keyInput = it },
                            placeholder = { Text("Paste custom Gemini API key (optional)") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("custom_api_key_input")
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { viewModel.setCustomApiKey(keyInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = NexaVioletPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.align(Alignment.End).testTag("save_api_key_button")
                        ) {
                            Text("Save Key")
                        }
                    }
                }
            }

            // Generation Parameters
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = NexaVioletPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generation Controls", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Streaming Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Server-Sent Streaming", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                Text("Display response progressively as tokens arrive", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = streamingEnabled,
                                onCheckedChange = { viewModel.setStreamingEnabled(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = NexaCyanSecondary, checkedTrackColor = NexaCyanSecondary.copy(alpha = 0.4f))
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Temperature Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Creativity (Temperature)", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                Text(String.format("%.2f", tempSlider), fontWeight = FontWeight.Bold, color = NexaCyanSecondary, fontSize = 13.sp)
                            }
                            Slider(
                                value = tempSlider,
                                onValueChange = {
                                    tempSlider = it
                                    viewModel.setTemperature(it)
                                },
                                valueRange = 0.0f..1.0f,
                                colors = SliderDefaults.colors(thumbColor = NexaCyanSecondary, activeTrackColor = NexaCyanSecondary)
                            )
                        }
                    }
                }
            }

            // Theme Preferences
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = NexaCyanSecondary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Theme & Appearance", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("dark" to "Dark", "light" to "Light", "system" to "System").forEach { (mode, label) ->
                                FilterChip(
                                    selected = themeMode == mode,
                                    onClick = { viewModel.setThemeMode(mode) },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                }
            }

            // Data Reset
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Data & History Management", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Permanently wipe conversation history and stored messages from local database storage.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { showClearConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("clear_history_button")
                        ) {
                            Text("Clear All Conversations", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    if (showClearConfirm) {
        ConfirmationDialog(
            title = "Clear All Conversations?",
            message = "All conversation threads and messages will be permanently removed. This action cannot be undone.",
            confirmLabel = "Delete All",
            isDestructive = true,
            onConfirm = {
                viewModel.clearAllConversations()
                showClearConfirm = false
            },
            onDismiss = { showClearConfirm = false }
        )
    }
}
