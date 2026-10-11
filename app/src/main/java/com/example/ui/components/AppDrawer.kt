package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConversationEntity
import com.example.data.model.UserAccountEntity
import com.example.ui.navigation.NavDestination
import com.example.ui.theme.NexaCyanSecondary
import com.example.ui.theme.NexaVioletPrimary

@Composable
fun AppDrawer(
    conversations: List<ConversationEntity>,
    archivedConversations: List<ConversationEntity>,
    currentConversationId: Long?,
    currentDestination: NavDestination,
    currentUser: UserAccountEntity?,
    onSelectConversation: (ConversationEntity) -> Unit,
    onNewChat: () -> Unit,
    onNavigate: (NavDestination) -> Unit,
    onRename: (ConversationEntity) -> Unit,
    onTogglePin: (ConversationEntity) -> Unit,
    onToggleArchive: (ConversationEntity) -> Unit,
    onDelete: (ConversationEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var isArchivedExpanded by remember { mutableStateOf(false) }

    val filteredList = remember(conversations, searchQuery) {
        if (searchQuery.isBlank()) conversations
        else conversations.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

    val pinnedList = filteredList.filter { it.isPinned }
    val regularList = filteredList.filter { !it.isPinned }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .width(320.dp)
            .fillMaxHeight()
            .testTag("app_drawer")
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 12.dp)
        ) {
            // Header: Brand & Logo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(NexaVioletPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "NEXA-AI",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Intelligence Platform",
                        fontSize = 11.sp,
                        color = NexaCyanSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Primary New Chat Button
            Button(
                onClick = onNewChat,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(44.dp)
                    .testTag("new_chat_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NexaVioletPrimary)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "New Chat", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search conversations...", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(16.dp))
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(48.dp)
                    .testTag("search_conversations_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Conversations List (Scrollable)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                // Pinned section
                if (pinnedList.isNotEmpty()) {
                    item {
                        SectionHeader(title = "PINNED")
                    }
                    items(pinnedList, key = { it.id }) { conv ->
                        ConversationDrawerItem(
                            conversation = conv,
                            isSelected = currentDestination == NavDestination.CHAT && conv.id == currentConversationId,
                            onClick = {
                                onSelectConversation(conv)
                                onNavigate(NavDestination.CHAT)
                            },
                            onRename = { onRename(conv) },
                            onTogglePin = { onTogglePin(conv) },
                            onToggleArchive = { onToggleArchive(conv) },
                            onDelete = { onDelete(conv) }
                        )
                    }
                }

                // Recent Conversations
                if (regularList.isNotEmpty()) {
                    item {
                        SectionHeader(title = "CONVERSATIONS")
                    }
                    items(regularList, key = { it.id }) { conv ->
                        ConversationDrawerItem(
                            conversation = conv,
                            isSelected = currentDestination == NavDestination.CHAT && conv.id == currentConversationId,
                            onClick = {
                                onSelectConversation(conv)
                                onNavigate(NavDestination.CHAT)
                            },
                            onRename = { onRename(conv) },
                            onTogglePin = { onTogglePin(conv) },
                            onToggleArchive = { onToggleArchive(conv) },
                            onDelete = { onDelete(conv) }
                        )
                    }
                }

                // Archived Expander
                if (archivedConversations.isNotEmpty()) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isArchivedExpanded = !isArchivedExpanded }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = if (isArchivedExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Archived (${archivedConversations.size})",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (isArchivedExpanded) {
                        items(archivedConversations, key = { "archived_${it.id}" }) { conv ->
                            ConversationDrawerItem(
                                conversation = conv,
                                isSelected = currentDestination == NavDestination.CHAT && conv.id == currentConversationId,
                                onClick = {
                                    onSelectConversation(conv)
                                    onNavigate(NavDestination.CHAT)
                                },
                                onRename = { onRename(conv) },
                                onTogglePin = { onTogglePin(conv) },
                                onToggleArchive = { onToggleArchive(conv) },
                                onDelete = { onDelete(conv) }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                color = DividerDefaults.color.copy(alpha = 0.3f)
            )

            // Bottom Navigation Destinations
            DrawerNavRow(
                icon = Icons.Default.Science,
                label = "Deep Research",
                isSelected = currentDestination == NavDestination.DEEP_RESEARCH,
                onClick = { onNavigate(NavDestination.DEEP_RESEARCH) }
            )
            DrawerNavRow(
                icon = Icons.Default.Build,
                label = "AI Studios (Code & Write)",
                isSelected = currentDestination == NavDestination.TOOLS,
                onClick = { onNavigate(NavDestination.TOOLS) }
            )
            DrawerNavRow(
                icon = Icons.Default.Psychology,
                label = "User Memory",
                isSelected = currentDestination == NavDestination.MEMORY,
                onClick = { onNavigate(NavDestination.MEMORY) }
            )
            DrawerNavRow(
                icon = Icons.Default.AdminPanelSettings,
                label = "Admin & Usage",
                isSelected = currentDestination == NavDestination.ADMIN,
                onClick = { onNavigate(NavDestination.ADMIN) }
            )
            DrawerNavRow(
                icon = Icons.Default.Settings,
                label = "Settings",
                isSelected = currentDestination == NavDestination.SETTINGS,
                onClick = { onNavigate(NavDestination.SETTINGS) }
            )
            DrawerNavRow(
                icon = Icons.Default.Person,
                label = currentUser?.username ?: "Account",
                isSelected = currentDestination == NavDestination.AUTH,
                onClick = { onNavigate(NavDestination.AUTH) }
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

@Composable
fun ConversationDrawerItem(
    conversation: ConversationEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleArchive: () -> Unit,
    onDelete: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }

    NavigationDrawerItem(
        label = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (conversation.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = NexaCyanSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = conversation.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Box {
                    IconButton(
                        onClick = { menuOpen = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Rename") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                menuOpen = false
                                onRename()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (conversation.isPinned) "Unpin" else "Pin to top") },
                            leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                menuOpen = false
                                onTogglePin()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (conversation.isArchived) "Unarchive" else "Archive") },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (conversation.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            onClick = {
                                menuOpen = false
                                onToggleArchive()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            onClick = {
                                menuOpen = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        },
        selected = isSelected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Chat,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = NexaVioletPrimary.copy(alpha = 0.15f),
            unselectedContainerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(vertical = 2.dp)
            .testTag("conversation_item_${conversation.id}")
    )
}

@Composable
fun DrawerNavRow(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        },
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) NexaCyanSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        },
        selected = isSelected,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = NexaVioletPrimary.copy(alpha = 0.15f),
            unselectedContainerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .testTag("nav_item_${label.lowercase().replace(" ", "_")}")
    )
}
