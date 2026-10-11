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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.RenderFormattedMarkdown
import com.example.ui.components.copyToClipboard
import com.example.ui.theme.NexaCyanSecondary
import com.example.ui.theme.NexaVioletPrimary
import com.example.ui.viewmodel.ToolsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    viewModel: ToolsViewModel,
    customApiKey: String?,
    onBack: () -> Unit,
    onOpenDrawer: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    val isLoading by viewModel.isLoading.collectAsState()
    val output by viewModel.toolOutput.collectAsState()
    val error by viewModel.error.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Code, 1: Writing, 2: Math & Data
    var inputCode by remember { mutableStateOf("") }
    var inputWriting by remember { mutableStateOf("") }
    var inputMath by remember { mutableStateOf("") }

    val tabTitles = listOf("Code Studio", "Writing Studio", "Math & Data")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = NexaVioletPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI Studios", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            viewModel.clearOutput()
                        },
                        text = {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) NexaVioletPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // Code Studio
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Software Engineering & Architecture", fontWeight = FontWeight.Bold, color = NexaCyanSecondary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Analyze bottlenecks, generate unit tests, or review security practices.", style = MaterialTheme.typography.bodySmall)
                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = inputCode,
                                        onValueChange = { inputCode = it },
                                        placeholder = { Text("Paste code snippet or describe programming problem...") },
                                        minLines = 4,
                                        modifier = Modifier.fillMaxWidth().testTag("code_studio_input"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { viewModel.runTool("code_review", inputCode, customApiKey = customApiKey) },
                                            enabled = inputCode.isNotBlank() && !isLoading,
                                            colors = ButtonDefaults.buttonColors(containerColor = NexaVioletPrimary),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f).testTag("run_code_review_button")
                                        ) {
                                            Text("Review & Audit", fontSize = 12.sp)
                                        }

                                        Button(
                                            onClick = { viewModel.runTool("code_test", inputCode, customApiKey = customApiKey) },
                                            enabled = inputCode.isNotBlank() && !isLoading,
                                            colors = ButtonDefaults.buttonColors(containerColor = NexaCyanSecondary),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f).testTag("run_unit_test_button")
                                        ) {
                                            Text("Generate Tests", fontSize = 12.sp, color = Color.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // Writing Studio
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Creative & Professional Writing", fontWeight = FontWeight.Bold, color = NexaVioletPrimary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Polish prose, draft executive memos, or structure essays.", style = MaterialTheme.typography.bodySmall)
                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = inputWriting,
                                        onValueChange = { inputWriting = it },
                                        placeholder = { Text("Enter prompt, bullet points, or draft to rewrite...") },
                                        minLines = 4,
                                        modifier = Modifier.fillMaxWidth().testTag("writing_studio_input"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { viewModel.runTool("writing_polish", inputWriting, customApiKey = customApiKey) },
                                            enabled = inputWriting.isNotBlank() && !isLoading,
                                            colors = ButtonDefaults.buttonColors(containerColor = NexaVioletPrimary),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Polish & Elevate", fontSize = 12.sp)
                                        }

                                        Button(
                                            onClick = { viewModel.runTool("writing_email", inputWriting, customApiKey = customApiKey) },
                                            enabled = inputWriting.isNotBlank() && !isLoading,
                                            colors = ButtonDefaults.buttonColors(containerColor = NexaCyanSecondary),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Draft Email", fontSize = 12.sp, color = Color.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // Math & Data
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Mathematics & Data Analysis", fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Step-by-step calculus, algebraic breakdown, and CSV generation.", style = MaterialTheme.typography.bodySmall)
                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = inputMath,
                                        onValueChange = { inputMath = it },
                                        placeholder = { Text("Describe equation or data table to convert to CSV...") },
                                        minLines = 4,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { viewModel.runTool("math_solve", inputMath, customApiKey = customApiKey) },
                                            enabled = inputMath.isNotBlank() && !isLoading,
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Step Solver", fontSize = 12.sp)
                                        }

                                        Button(
                                            onClick = { viewModel.runTool("data_csv", inputMath, customApiKey = customApiKey) },
                                            enabled = inputMath.isNotBlank() && !isLoading,
                                            colors = ButtonDefaults.buttonColors(containerColor = NexaCyanSecondary),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Generate CSV", fontSize = 12.sp, color = Color.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (isLoading) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = NexaCyanSecondary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Studio model processing...", fontSize = 13.sp)
                        }
                    }
                }

                if (error != null) {
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                            Text(error ?: "", color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(14.dp))
                        }
                    }
                }

                if (output != null) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Studio Result", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    IconButton(
                                        onClick = {
                                            copyToClipboard(context, output ?: "")
                                            Toast.makeText(context, "Result copied", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = NexaCyanSecondary)
                                    }
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                RenderFormattedMarkdown(content = output ?: "")
                            }
                        }
                    }
                }
            }
        }
    }
}
