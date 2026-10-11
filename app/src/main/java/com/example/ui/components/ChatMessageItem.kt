package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MessageEntity
import com.example.ui.theme.CodeBackground
import com.example.ui.theme.NexaCyanSecondary
import com.example.ui.theme.NexaVioletPrimary

@Composable
fun ChatMessageItem(
    message: MessageEntity,
    isStreaming: Boolean = false,
    onRegenerate: () -> Unit = {},
    onEdit: (String) -> Unit = {},
    onLike: () -> Unit = {},
    onDislike: () -> Unit = {},
    onSpeak: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val isUser = message.role == "user"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 12.dp)
            .testTag(if (isUser) "user_message_row" else "assistant_message_row"),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            // Nexa Assistant Avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(NexaVioletPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "NEXA AI",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.weight(1f, fill = false),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            // Bubble card
            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.border(
                    width = 0.5.dp,
                    color = if (isUser) NexaVioletPrimary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (message.isError) {
                        Text(
                            text = message.content,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        RenderFormattedMarkdown(content = message.content)
                    }
                }
            }

            // Action Toolbar (below message)
            if (!isUser && !isStreaming && message.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    // Copy
                    IconButton(
                        onClick = {
                            copyToClipboard(context, message.content)
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("copy_response_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy response",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Read Aloud
                    IconButton(
                        onClick = { onSpeak(message.content) },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("read_aloud_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Read aloud",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Regenerate
                    IconButton(
                        onClick = onRegenerate,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("regenerate_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Regenerate",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Like
                    IconButton(
                        onClick = onLike,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("like_button")
                    ) {
                        Icon(
                            imageVector = if (message.isLiked) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                            contentDescription = "Like",
                            tint = if (message.isLiked) NexaCyanSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Dislike
                    IconButton(
                        onClick = onDislike,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("dislike_button")
                    ) {
                        Icon(
                            imageVector = if (message.isDisliked) Icons.Default.ThumbDown else Icons.Outlined.ThumbDown,
                            contentDescription = "Dislike",
                            tint = if (message.isDisliked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Share
                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, message.content)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share response via"))
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else if (isUser) {
                // User message actions
                Row(
                    modifier = Modifier.padding(end = 4.dp, top = 2.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = { onEdit(message.content) },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("edit_message_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit message",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            // User Avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(NexaCyanSecondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "You",
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Parses markdown blocks and renders headings, code blocks, lists, and body paragraphs.
 */
@Composable
fun RenderFormattedMarkdown(content: String) {
    val context = LocalContext.current
    val blocks = remember(content) { parseMarkdownBlocks(content) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.CodeBlock -> {
                    CodeSnippetCard(
                        language = block.language,
                        code = block.code,
                        onCopy = {
                            copyToClipboard(context, block.code)
                            Toast.makeText(context, "Code copied", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                is MarkdownBlock.Heading -> {
                    Text(
                        text = block.text,
                        style = when (block.level) {
                            1 -> MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            2 -> MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            else -> MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium)
                        },
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                is MarkdownBlock.ListItem -> {
                    Row(modifier = Modifier.padding(start = 4.dp)) {
                        Text(
                            text = if (block.isOrdered) "${block.index}. " else "• ",
                            fontWeight = FontWeight.Bold,
                            color = NexaCyanSecondary
                        )
                        Text(
                            text = block.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                is MarkdownBlock.Paragraph -> {
                    Text(
                        text = block.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun CodeSnippetCard(language: String, code: String, onCopy: () -> Unit) {
    var copied by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = CodeBackground),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, Color(0xFF30363D), RoundedCornerShape(8.dp))
    ) {
        Column {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF161B22))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = language.ifBlank { "code" }.uppercase(),
                    color = NexaCyanSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                IconButton(
                    onClick = {
                        onCopy()
                        copied = true
                    },
                    modifier = Modifier
                        .size(26.dp)
                        .testTag("copy_code_button")
                ) {
                    Icon(
                        imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = "Copy code",
                        tint = if (copied) NexaCyanSecondary else Color(0xFF8B949E),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Code content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                Text(
                    text = code,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = Color(0xFFE6EDF3),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

sealed class MarkdownBlock {
    data class Paragraph(val text: String) : MarkdownBlock()
    data class Heading(val level: Int, val text: String) : MarkdownBlock()
    data class ListItem(val text: String, val isOrdered: Boolean = false, val index: Int = 1) : MarkdownBlock()
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock()
}

fun parseMarkdownBlocks(text: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = text.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]

        // Check for code block start
        if (line.trim().startsWith("```")) {
            val language = line.trim().removePrefix("```").trim()
            val codeBuilder = StringBuilder()
            i++
            while (i < lines.size && !lines[i].trim().startsWith("```")) {
                codeBuilder.append(lines[i]).append("\n")
                i++
            }
            blocks.add(MarkdownBlock.CodeBlock(language, codeBuilder.toString().trimEnd()))
            i++
            continue
        }

        // Heading
        if (line.startsWith("#")) {
            val level = line.takeWhile { it == '#' }.length
            val headingText = line.drop(level).trim()
            blocks.add(MarkdownBlock.Heading(level, headingText))
            i++
            continue
        }

        // Unordered list
        if (line.trim().startsWith("- ") || line.trim().startsWith("* ")) {
            val itemText = line.trim().drop(2).trim()
            blocks.add(MarkdownBlock.ListItem(itemText, isOrdered = false))
            i++
            continue
        }

        // Ordered list
        val match = Regex("^(\\d+)\\.\\s+(.*)").find(line.trim())
        if (match != null) {
            val index = match.groupValues[1].toIntOrNull() ?: 1
            val itemText = match.groupValues[2]
            blocks.add(MarkdownBlock.ListItem(itemText, isOrdered = true, index = index))
            i++
            continue
        }

        // Regular line or empty
        if (line.isNotBlank()) {
            blocks.add(MarkdownBlock.Paragraph(line.trim()))
        }
        i++
    }

    return blocks.ifEmpty { listOf(MarkdownBlock.Paragraph(text)) }
}

fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("NEXA-AI", text)
    clipboard.setPrimaryClip(clip)
}
