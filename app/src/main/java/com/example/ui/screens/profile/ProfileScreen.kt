package com.example.ui.screens.profile

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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.models.UserProfile
import com.example.data.repository.VyroRepository
import com.example.ui.components.AmbientGlowBackground
import com.example.ui.components.GlassCard
import com.example.ui.screens.feed.PostCard
import com.example.ui.theme.VyroAccentPrimary
import com.example.ui.theme.VyroAccentSecondary
import com.example.ui.theme.VyroGlassBorder
import com.example.ui.theme.VyroGlassDark
import com.example.ui.theme.VyroGlassSurface
import com.example.ui.theme.VyroTextPrimary
import com.example.ui.theme.VyroTextSecondary
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    repository: VyroRepository,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val currentUserId = repository.currentUserIdOrNull() ?: ""
    val currentUser = Firebase.auth.currentUser
    val userProfile by repository.observeUserProfile(currentUserId).collectAsState(initial = null)
    val allPosts by repository.observeFeedPosts().collectAsState(initial = emptyList())

    val userPosts = allPosts.filter { it.userId == currentUserId }
    val bookmarkedPosts = allPosts.filter { it.isBookmarkedByMe }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    val displayName = userProfile?.displayName?.ifBlank { currentUser?.displayName } ?: "Vyro Pioneer"
    val username = userProfile?.username?.ifBlank { "vyro_${currentUserId.take(6)}" } ?: "vyro_user"
    val bio = userProfile?.bio?.ifBlank { "Exploring social media beyond limits with 3D Glassmorphism ✨" } ?: ""
    val avatarUrl = userProfile?.avatarUrl?.ifBlank { currentUser?.photoUrl?.toString() } ?: ""

    AmbientGlowBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(VyroGlassDark)
                    .border(1.dp, VyroGlassBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "@$username",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = VyroTextPrimary
                    )

                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = VyroAccentSecondary
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                // Profile Card
                item {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.sweepGradient(
                                            listOf(
                                                VyroAccentPrimary,
                                                VyroAccentSecondary,
                                                VyroAccentPrimary
                                            )
                                        )
                                    )
                                    .padding(3.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0F172A)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (avatarUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = avatarUrl,
                                        contentDescription = displayName,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(46.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = VyroTextPrimary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = bio,
                                style = MaterialTheme.typography.bodyMedium,
                                color = VyroTextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Stats: Posts, Followers, Following
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatItem(label = "Posts", count = userPosts.size.toLong())
                                StatItem(label = "Followers", count = userProfile?.followersCount ?: 342L)
                                StatItem(label = "Following", count = userProfile?.followingCount ?: 189L)
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Edit Profile Button
                            Button(
                                onClick = { showEditProfileDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VyroGlassSurface,
                                    contentColor = Color.White
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, VyroGlassBorder)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = VyroAccentSecondary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Edit Profile",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }
                    }
                }

                // Profile Tabs: My Posts, Bookmarked
                item {
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.Transparent,
                        contentColor = VyroAccentPrimary,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = VyroAccentPrimary,
                                height = 3.dp
                            )
                        },
                        divider = {},
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                            text = {
                                Text(
                                    text = "My Posts (${userPosts.size})",
                                    fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTabIndex == 0) VyroTextPrimary else VyroTextSecondary
                                )
                            }
                        )
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            text = {
                                Text(
                                    text = "Saved (${bookmarkedPosts.size})",
                                    fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTabIndex == 1) VyroTextPrimary else VyroTextSecondary
                                )
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Display Selected Tab Content
                val displayList = if (selectedTabIndex == 0) userPosts else bookmarkedPosts
                if (displayList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (selectedTabIndex == 0) "No posts published yet" else "No saved posts yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = VyroTextSecondary
                            )
                        }
                    }
                } else {
                    items(displayList, key = { it.id }) { post ->
                        PostCard(
                            post = post,
                            onLikeClick = {
                                coroutineScope.launch {
                                    repository.toggleLike(post.id, post.isLikedByMe)
                                }
                            },
                            onCommentClick = { },
                            onBookmarkClick = {
                                coroutineScope.launch {
                                    repository.toggleBookmark(post.id, post.isBookmarkedByMe)
                                }
                            },
                            onShareClick = { },
                            onUserClick = { }
                        )
                    }
                }
            }
        }

        if (showEditProfileDialog) {
            EditProfileDialog(
                currentProfile = userProfile ?: UserProfile(
                    userId = currentUserId,
                    displayName = displayName,
                    username = username,
                    bio = bio,
                    avatarUrl = avatarUrl
                ),
                onDismiss = { showEditProfileDialog = false },
                onSave = { updated ->
                    coroutineScope.launch {
                        repository.saveUserProfile(updated)
                        showEditProfileDialog = false
                    }
                }
            )
        }
    }
}

@Composable
private fun StatItem(label: String, count: Long) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = VyroTextPrimary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = VyroTextSecondary
        )
    }
}
