package com.example.ui.screens.feed

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.models.Comment
import com.example.data.models.Post
import com.example.data.models.Story
import com.example.data.repository.VyroRepository
import com.example.ui.components.AmbientGlowBackground
import com.example.ui.components.GlassCard
import com.example.ui.components.VyroGradientButton
import com.example.ui.screens.stories.CreateStoryDialog
import com.example.ui.screens.stories.StoryViewerDialog
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
fun FeedScreen(
    repository: VyroRepository,
    onNavigateToNotifications: () -> Unit,
    onNavigateToExplore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val posts by repository.observeFeedPosts().collectAsState(initial = emptyList())
    val stories by repository.observeStories().collectAsState(initial = emptyList())
    val currentUserId = repository.currentUserIdOrNull() ?: ""

    var showCreatePostDialog by remember { mutableStateOf(false) }
    var showCreateStoryDialog by remember { mutableStateOf(false) }
    var activeStoryToView by remember { mutableStateOf<Story?>(null) }
    var activePostForComments by remember { mutableStateOf<Post?>(null) }
    var activeComments by remember { mutableStateOf<List<Comment>>(emptyList()) }

    AmbientGlowBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Glass Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(VyroGlassDark)
                    .border(1.dp, VyroGlassBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_vyro_logo),
                            contentDescription = "VYRO",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "VYRO",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 3.sp,
                                brush = Brush.horizontalGradient(listOf(Color.White, VyroAccentSecondary))
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onNavigateToExplore) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Explore",
                                tint = VyroTextSecondary
                            )
                        }

                        IconButton(onClick = onNavigateToNotifications) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = VyroTextSecondary
                            )
                        }
                    }
                }
            }

            // Main Feed List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                // Stories Row
                item {
                    StoriesRow(
                        stories = stories,
                        currentUserName = "You",
                        currentUserAvatar = "",
                        onAddStoryClick = { showCreateStoryDialog = true },
                        onStoryClick = { activeStoryToView = it }
                    )
                }

                // If feed empty, show inspirational empty state
                if (posts.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(40.dp))
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = VyroAccentPrimary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Welcome to VYRO",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = VyroTextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "The universe is quiet right now. Share the first visual post or story with your community!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = VyroTextSecondary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(18.dp))
                                    VyroGradientButton(
                                        text = "Create First Post",
                                        onClick = { showCreatePostDialog = true }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(posts, key = { it.id.ifBlank { it.userId + it.caption } }) { post ->
                        PostCard(
                            post = post,
                            onLikeClick = {
                                coroutineScope.launch {
                                    repository.toggleLike(post.id, post.isLikedByMe)
                                }
                            },
                            onCommentClick = {
                                activePostForComments = post
                                coroutineScope.launch {
                                    repository.observeComments(post.id).collect {
                                        activeComments = it
                                    }
                                }
                            },
                            onBookmarkClick = {
                                coroutineScope.launch {
                                    repository.toggleBookmark(post.id, post.isBookmarkedByMe)
                                }
                            },
                            onShareClick = {
                                // In-app share preview
                            },
                            onUserClick = {
                                // Profile preview
                            }
                        )
                    }
                }
            }
        }

        // FAB to create post
        FloatingActionButton(
            onClick = { showCreatePostDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 20.dp),
            containerColor = VyroAccentPrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Create Post")
        }

        // Modals
        if (showCreatePostDialog) {
            CreatePostDialog(
                onDismiss = { showCreatePostDialog = false },
                onSubmitPost = { caption, media, location, tags ->
                    coroutineScope.launch {
                        repository.createPost(caption, media, "image", location, tags)
                        showCreatePostDialog = false
                    }
                }
            )
        }

        if (showCreateStoryDialog) {
            CreateStoryDialog(
                onDismiss = { showCreateStoryDialog = false },
                onSubmitStory = { mediaUrl, caption ->
                    coroutineScope.launch {
                        repository.createStory(mediaUrl, caption)
                        showCreateStoryDialog = false
                    }
                }
            )
        }

        activeStoryToView?.let { story ->
            StoryViewerDialog(
                story = story,
                onDismiss = { activeStoryToView = null },
                onReact = { /* Reaction recorded */ }
            )
        }

        activePostForComments?.let { post ->
            CommentsBottomSheet(
                comments = activeComments,
                onDismiss = { activePostForComments = null },
                onSendComment = { text ->
                    coroutineScope.launch {
                        repository.addComment(post.id, text)
                    }
                }
            )
        }
    }
}
