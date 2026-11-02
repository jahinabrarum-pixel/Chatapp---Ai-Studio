package com.example.ui.screens.notifications

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.NotificationItem
import com.example.data.repository.VyroRepository
import com.example.ui.components.AmbientGlowBackground
import com.example.ui.components.GlassCard
import com.example.ui.theme.VyroAccentPrimary
import com.example.ui.theme.VyroAccentSecondary
import com.example.ui.theme.VyroAccentTertiary
import com.example.ui.theme.VyroGlassBorder
import com.example.ui.theme.VyroGlassDark
import com.example.ui.theme.VyroGlassSurface
import com.example.ui.theme.VyroTextPrimary
import com.example.ui.theme.VyroTextSecondary
import kotlinx.coroutines.launch

@Composable
fun NotificationsScreen(
    repository: VyroRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    val coroutineScope = rememberCoroutineScope()
    val notifications by repository.observeNotifications().collectAsState(initial = emptyList())

    // Mock initial notification examples if empty so user has instant feedback
    val displayNotifications = if (notifications.isNotEmpty()) notifications else listOf(
        NotificationItem(
            id = "notif_1",
            fromUsername = "Nova Cyber",
            type = "like",
            content = "liked your post 'Connecting beyond limits in 3D'",
            isRead = false
        ),
        NotificationItem(
            id = "notif_2",
            fromUsername = "Elena Frost",
            type = "comment",
            content = "commented: 'This glassmorphism interface is incredible!'",
            isRead = false
        ),
        NotificationItem(
            id = "notif_3",
            fromUsername = "Aiden Storm",
            type = "call",
            content = "missed a voice call with you",
            isRead = true
        ),
        NotificationItem(
            id = "notif_4",
            fromUsername = "Vyro AI Team",
            type = "follow",
            content = "started following your journey on VYRO",
            isRead = true
        )
    )

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
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(VyroGlassDark)
                    .border(1.dp, VyroGlassBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = VyroTextPrimary
                            )
                        }
                        Text(
                            text = "Notifications",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = VyroTextPrimary
                        )
                    }

                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                notifications.forEach { n ->
                                    repository.markNotificationRead(n.id)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Mark all read",
                            tint = VyroAccentSecondary
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(displayNotifications, key = { it.id }) { notif ->
                    val iconVector = when (notif.type) {
                        "like" -> Icons.Default.Favorite
                        "comment" -> Icons.Default.ChatBubble
                        "call" -> Icons.Default.Call
                        else -> Icons.Default.PersonAdd
                    }
                    val iconTint = when (notif.type) {
                        "like" -> VyroAccentTertiary
                        "comment" -> VyroAccentPrimary
                        "call" -> VyroAccentSecondary
                        else -> VyroAccentSecondary
                    }

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        backgroundColor = if (!notif.isRead) VyroGlassSurface.copy(alpha = 0.22f) else VyroGlassSurface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(iconTint.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = null,
                                    tint = iconTint,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = notif.fromUsername,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = VyroTextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = notif.content,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = VyroTextSecondary
                                )
                            }

                            if (!notif.isRead) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(VyroAccentPrimary)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
