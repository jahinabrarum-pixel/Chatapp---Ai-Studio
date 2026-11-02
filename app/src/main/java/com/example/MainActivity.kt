package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.data.models.CallRecord
import com.example.data.models.UserProfile
import com.example.data.repository.VyroRepository
import com.example.ui.navigation.MainTab
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.auth.signOut
import com.example.ui.screens.calls.ActiveCallScreen
import com.example.ui.screens.calls.IncomingCallDialog
import com.example.ui.screens.chat.ChatListScreen
import com.example.ui.screens.chat.ConversationDetailScreen
import com.example.ui.screens.explore.ExploreScreen
import com.example.ui.screens.feed.CreatePostDialog
import com.example.ui.screens.feed.FeedScreen
import com.example.ui.screens.notifications.NotificationsScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.theme.VyroAccentPrimary
import com.example.ui.theme.VyroAccentSecondary
import com.example.ui.theme.VyroBackground
import com.example.ui.theme.VyroGlassBorder
import com.example.ui.theme.VyroGlassDark
import com.example.ui.theme.VyroGlassSurface
import com.example.ui.theme.VyroTextPrimary
import com.example.ui.theme.VyroTextSecondary
import com.example.ui.theme.VyroTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VyroTheme {
                VyroRootApp()
            }
        }
    }
}

@Composable
fun VyroRootApp() {
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    if (currentUser == null) {
        AuthScreen(
            onAuthSuccess = {
                currentUser = Firebase.auth.currentUser
            }
        )
    } else {
        // Authenticated user session
        val repository = remember(currentUser?.uid) {
            VyroRepository(context)
        }

        // Auto-initialize profile record if first time
        DisposableEffect(currentUser?.uid) {
            currentUser?.let { user ->
                coroutineScope.launch {
                    repository.saveUserProfile(
                        UserProfile(
                            userId = user.uid,
                            displayName = user.displayName ?: "Vyro Explorer",
                            email = user.email ?: "",
                            avatarUrl = user.photoUrl?.toString() ?: "",
                            bio = "Connecting beyond limits with 3D Glassmorphism"
                        )
                    )
                }
            }
            onDispose { }
        }

        VyroMainAppContent(
            repository = repository,
            onSignOut = {
                signOut(
                    context = context,
                    credentialManager = credentialManager,
                    onSignOutComplete = {
                        currentUser = null
                    },
                    scope = coroutineScope
                )
            }
        )
    }
}

@Composable
fun VyroMainAppContent(
    repository: VyroRepository,
    onSignOut: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(MainTab.HOME) }
    var activeConversation by remember { mutableStateOf<Pair<String, String>?>(null) } // id to title
    var activeCall by remember { mutableStateOf<Pair<String, String>?>(null) } // contactName to type
    var showNotifications by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showCreatePostFromNav by remember { mutableStateOf(false) }

    // Listen for incoming calls
    val incomingCalls by repository.observeActiveCalls().collectAsState(initial = emptyList())
    val activeIncomingCall: CallRecord? = incomingCalls.firstOrNull()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = VyroBackground,
            bottomBar = {
                // Translucent Glass Bottom Navigation Bar
                if (activeConversation == null && activeCall == null && !showNotifications && !showSettings) {
                    VyroGlassBottomBar(
                        selectedTab = selectedTab,
                        onTabSelected = { tab ->
                            if (tab == MainTab.CREATE) {
                                showCreatePostFromNav = true
                            } else {
                                selectedTab = tab
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                when (selectedTab) {
                    MainTab.HOME -> FeedScreen(
                        repository = repository,
                        onNavigateToNotifications = { showNotifications = true },
                        onNavigateToExplore = { selectedTab = MainTab.EXPLORE }
                    )
                    MainTab.EXPLORE -> ExploreScreen(
                        repository = repository
                    )
                    MainTab.CREATE -> {
                        // Handled via modal, show Feed
                        FeedScreen(
                            repository = repository,
                            onNavigateToNotifications = { showNotifications = true },
                            onNavigateToExplore = { selectedTab = MainTab.EXPLORE }
                        )
                    }
                    MainTab.MESSAGES -> ChatListScreen(
                        repository = repository,
                        onConversationClick = { id, title ->
                            activeConversation = id to title
                        }
                    )
                    MainTab.PROFILE -> ProfileScreen(
                        repository = repository,
                        onNavigateToSettings = { showSettings = true }
                    )
                }
            }
        }

        // Sub-screen overlays
        activeConversation?.let { (convId, convTitle) ->
            ConversationDetailScreen(
                conversationId = convId,
                title = convTitle,
                repository = repository,
                onBackClick = { activeConversation = null },
                onStartCall = { type ->
                    activeCall = convTitle to type
                }
            )
        }

        if (showNotifications) {
            NotificationsScreen(
                repository = repository,
                onBackClick = { showNotifications = false }
            )
        }

        if (showSettings) {
            SettingsScreen(
                onBackClick = { showSettings = false },
                onSignOut = onSignOut
            )
        }

        activeCall?.let { (contact, type) ->
            ActiveCallScreen(
                contactName = contact,
                callType = type,
                onEndCall = {
                    activeCall = null
                }
            )
        }

        // Incoming Call Dialog
        activeIncomingCall?.let { incoming ->
            IncomingCallDialog(
                callerName = incoming.callerName,
                callType = incoming.callType,
                onAccept = {
                    coroutineScope.launch {
                        repository.updateCallStatus(incoming.id, "accepted")
                        activeCall = incoming.callerName to incoming.callType
                    }
                },
                onReject = {
                    coroutineScope.launch {
                        repository.updateCallStatus(incoming.id, "rejected")
                    }
                }
            )
        }

        // Create Post Modal from Bottom Nav
        if (showCreatePostFromNav) {
            CreatePostDialog(
                onDismiss = { showCreatePostFromNav = false },
                onSubmitPost = { caption, media, location, tags ->
                    coroutineScope.launch {
                        repository.createPost(caption, media, "image", location, tags)
                        showCreatePostFromNav = false
                        selectedTab = MainTab.HOME
                    }
                }
            )
        }
    }
}

@Composable
private fun VyroGlassBottomBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(VyroGlassDark)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        VyroAccentPrimary.copy(alpha = 0.4f),
                        VyroAccentSecondary.copy(alpha = 0.2f),
                        VyroGlassBorder
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MainTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                val icon = if (isSelected) tab.selectedIcon else tab.unselectedIcon

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("tab_${tab.name.lowercase()}")
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) VyroAccentPrimary.copy(alpha = 0.25f) else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = tab.title,
                            tint = if (isSelected) VyroAccentSecondary else VyroTextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isSelected) Color.White else VyroTextSecondary,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
