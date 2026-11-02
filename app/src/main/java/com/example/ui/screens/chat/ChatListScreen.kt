package com.example.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.models.Conversation
import com.example.data.repository.VyroRepository
import com.example.ui.components.AmbientGlowBackground
import com.example.ui.components.GlassCard
import com.example.ui.components.VyroGradientButton
import com.example.ui.components.VyroTextField
import com.example.ui.theme.VyroAccentPrimary
import com.example.ui.theme.VyroAccentSecondary
import com.example.ui.theme.VyroGlassBorder
import com.example.ui.theme.VyroGlassDark
import com.example.ui.theme.VyroGlassSurface
import com.example.ui.theme.VyroSuccess
import com.example.ui.theme.VyroTextPrimary
import com.example.ui.theme.VyroTextSecondary
import kotlinx.coroutines.launch

@Composable
fun ChatListScreen(
    repository: VyroRepository,
    onConversationClick: (conversationId: String, title: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val conversations by repository.observeConversations().collectAsState(initial = emptyList())
    val currentUserId = repository.currentUserIdOrNull() ?: ""

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var showNewChatDialog by remember { mutableStateOf(false) }
    var showCreateGroupDialog by remember { mutableStateOf(false) }

    val filteredConversations = conversations.filter { conv ->
        val matchesSearch = conv.groupName.contains(searchQuery, ignoreCase = true) ||
                conv.lastMessage.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "Direct" -> !conv.isGroup
            "Groups" -> conv.isGroup
            else -> true
        }
        matchesSearch && matchesFilter
    }

    AmbientGlowBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(VyroGlassDark)
                    .border(1.dp, VyroGlassBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Messages",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        ),
                        color = VyroTextPrimary
                    )

                    Row {
                        IconButton(onClick = { showCreateGroupDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.GroupAdd,
                                contentDescription = "New Group",
                                tint = VyroAccentSecondary
                            )
                        }
                    }
                }
            }

            // Search Bar
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                VyroTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = "Search messages & users",
                    placeholder = "Search conversations...",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = VyroTextSecondary
                        )
                    }
                )
            }

            // Filter Chips: All, Direct, Groups
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Direct", "Groups").forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) VyroAccentPrimary else VyroGlassSurface
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) VyroAccentPrimary else VyroGlassBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = filter,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isSelected) Color.White else VyroTextSecondary
                        )
                    }
                }
            }

            // Conversations List
            if (filteredConversations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = VyroTextSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No conversations yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = VyroTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Start a 1-on-1 encrypted chat or create a group room with your friends.",
                            style = MaterialTheme.typography.bodySmall,
                            color = VyroTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        VyroGradientButton(
                            text = "Start Direct Chat",
                            onClick = { showNewChatDialog = true }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredConversations, key = { it.id }) { conv ->
                        val title = if (conv.isGroup) conv.groupName.ifBlank { "Group Chat" } else conv.groupName.ifBlank { "Direct Chat" }
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onConversationClick(conv.id, title) },
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar with online indicator
                                Box(modifier = Modifier.size(50.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(CircleShape)
                                            .background(VyroGlassSurface)
                                            .border(
                                                1.dp,
                                                Brush.linearGradient(listOf(VyroAccentPrimary, VyroAccentSecondary)),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (conv.groupPhotoUrl.isNotBlank()) {
                                            AsyncImage(
                                                model = conv.groupPhotoUrl,
                                                contentDescription = title,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Icon(
                                                imageVector = if (conv.isGroup) Icons.Default.Group else Icons.Default.Person,
                                                contentDescription = null,
                                                tint = VyroAccentPrimary,
                                                modifier = Modifier.size(26.dp)
                                            )
                                        }
                                    }

                                    // Online green indicator dot
                                    Box(
                                        modifier = Modifier
                                            .size(13.dp)
                                            .clip(CircleShape)
                                            .background(VyroSuccess)
                                            .border(2.dp, Color(0xFF080B14), CircleShape)
                                            .align(Alignment.BottomEnd)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = VyroTextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Online",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = VyroAccentSecondary,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = conv.lastMessage.ifBlank { "Tap to open chat" },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = VyroTextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // FAB to start new chat
        FloatingActionButton(
            onClick = { showNewChatDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 20.dp),
            containerColor = VyroAccentPrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "New Chat")
        }

        // New Chat Dialog
        if (showNewChatDialog) {
            var targetUsername by remember { mutableStateOf("") }
            Dialog(onDismissRequest = { showNewChatDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF111827),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VyroGlassBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Start Direct Chat",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = VyroTextPrimary
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        VyroTextField(
                            value = targetUsername,
                            onValueChange = { targetUsername = it },
                            label = "Recipient Username / ID",
                            placeholder = "e.g. alex, maya, neo"
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        VyroGradientButton(
                            text = "Open Chat Room",
                            onClick = {
                                if (targetUsername.isNotBlank()) {
                                    coroutineScope.launch {
                                        val otherId = "user_${targetUsername.trim().lowercase()}"
                                        val res = repository.getOrCreateDirectConversation(otherId, targetUsername.trim())
                                        if (res.isSuccess) {
                                            showNewChatDialog = false
                                            onConversationClick(res.getOrThrow(), targetUsername.trim())
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Create Group Dialog
        if (showCreateGroupDialog) {
            var groupName by remember { mutableStateOf("") }
            var membersText by remember { mutableStateOf("") }
            Dialog(onDismissRequest = { showCreateGroupDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF111827),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VyroGlassBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Create Group Room",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = VyroTextPrimary
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        VyroTextField(
                            value = groupName,
                            onValueChange = { groupName = it },
                            label = "Group Name",
                            placeholder = "e.g. Cyber Squad, Dev Hub"
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        VyroTextField(
                            value = membersText,
                            onValueChange = { membersText = it },
                            label = "Member Usernames",
                            placeholder = "Comma-separated usernames"
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        VyroGradientButton(
                            text = "Create Group",
                            onClick = {
                                if (groupName.isNotBlank()) {
                                    coroutineScope.launch {
                                        val members = membersText.split(",").map { "user_${it.trim().lowercase()}" }.filter { it.isNotBlank() }
                                        val res = repository.createGroupConversation(groupName.trim(), members)
                                        if (res.isSuccess) {
                                            showCreateGroupDialog = false
                                            onConversationClick(res.getOrThrow(), groupName.trim())
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
