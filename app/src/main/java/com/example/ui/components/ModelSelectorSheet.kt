package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NexaCyanSecondary
import com.example.ui.theme.NexaVioletPrimary

data class ModelOption(
    val id: String,
    val displayName: String,
    val provider: String,
    val description: String,
    val capabilities: List<String>,
    val badgeColor: Color
)

val AVAILABLE_MODELS = listOf(
    ModelOption(
        id = "gemini-3.5-flash",
        displayName = "Nexa Flash 3.5",
        provider = "Google Gemini",
        description = "Balanced high-speed model with advanced multimodal reasoning and web research capabilities.",
        capabilities = listOf("Default", "Multimodal", "Fast", "Long-Context"),
        badgeColor = NexaVioletPrimary
    ),
    ModelOption(
        id = "gemini-3.1-pro-preview",
        displayName = "Nexa Reasoning Pro 3.1",
        provider = "Google Gemini",
        description = "Deep thinking & reasoning engine for software architecture, mathematics, and complex analysis.",
        capabilities = listOf("Deep Logic", "Complex Code", "STEM"),
        badgeColor = NexaCyanSecondary
    ),
    ModelOption(
        id = "gemini-2.5-flash-image",
        displayName = "Nexa Vision Studio",
        provider = "Google Gemini",
        description = "Specialized visual comprehension for analyzing photos, diagrams, UI wireframes, and charts.",
        capabilities = listOf("Vision", "OCR", "Charts"),
        badgeColor = Color(0xFFF43F5E)
    ),
    ModelOption(
        id = "gemini-3.1-flash-lite-preview",
        displayName = "Nexa Lite 3.1",
        provider = "Google Gemini",
        description = "Ultra lightweight, minimal latency model optimized for rapid responses and quick drafting.",
        capabilities = listOf("Ultra Fast", "Low Latency"),
        badgeColor = Color(0xFF10B981)
    )
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ModelSelectorSheet(
    selectedModelId: String,
    onModelSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("model_selector_sheet")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = NexaVioletPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Select Intelligence Model",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AVAILABLE_MODELS.forEach { model ->
                    val isSelected = model.id == selectedModelId

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) NexaVioletPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 1.5.dp else 0.5.dp,
                            color = if (isSelected) NexaVioletPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onModelSelect(model.id)
                                onDismiss()
                            }
                            .testTag("model_option_${model.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = model.displayName,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = model.provider,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = model.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    model.capabilities.forEach { cap ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(model.badgeColor.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = cap,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = model.badgeColor
                                            )
                                        }
                                    }
                                }
                            }

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(NexaVioletPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
