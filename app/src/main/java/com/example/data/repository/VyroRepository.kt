package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.models.CallRecord
import com.example.data.models.Comment
import com.example.data.models.Conversation
import com.example.data.models.Message
import com.example.data.models.NotificationItem
import com.example.data.models.Post
import com.example.data.models.Story
import com.example.data.models.UserProfile
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class VyroRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google to access VYRO.")
    }

    fun currentUserIdOrNull(): String? = auth.currentUser?.uid

    // --- User Profile ---

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val path = "users/$userId"
        db.collection("users").document(userId).snapshots()
            .map { it.toObject(UserProfile::class.java) }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
                throw error
            }
            .collect { emit(it) }
    }

    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        return try {
            val payload = mapOf(
                "userId" to uid,
                "displayName" to profile.displayName.ifBlank { "User" },
                "username" to profile.username.ifBlank { "vyro_${uid.take(6)}" },
                "email" to (auth.currentUser?.email ?: profile.email),
                "avatarUrl" to (profile.avatarUrl.ifBlank { auth.currentUser?.photoUrl?.toString() ?: "" }),
                "bio" to profile.bio,
                "dateOfBirth" to profile.dateOfBirth,
                "followersCount" to profile.followersCount,
                "followingCount" to profile.followingCount,
                "postsCount" to profile.postsCount,
                "isPrivate" to profile.isPrivate,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    // --- Posts ---

    fun observeFeedPosts(): Flow<List<Post>> = flow {
        val path = "posts"
        db.collection("posts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .snapshots()
            .map { snapshot -> snapshot.toObjects(Post::class.java) }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
            .collect { emit(it) }
    }

    suspend fun createPost(
        caption: String,
        mediaUrls: List<String> = emptyList(),
        mediaType: String = "image",
        location: String = "",
        tags: List<String> = emptyList()
    ): Result<String> {
        val uid = requireUserId()
        val docRef = db.collection("posts").document()
        return try {
            val user = auth.currentUser
            val payload = mapOf(
                "userId" to uid,
                "username" to (user?.displayName ?: "Vyro Explorer"),
                "userAvatarUrl" to (user?.photoUrl?.toString() ?: ""),
                "caption" to caption,
                "mediaUrls" to mediaUrls,
                "mediaType" to mediaType,
                "location" to location,
                "tags" to tags,
                "likesCount" to 0L,
                "commentsCount" to 0L,
                "bookmarksCount" to 0L,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun toggleLike(postId: String, isCurrentlyLiked: Boolean): Result<Unit> {
        val uid = requireUserId()
        val likeRef = db.collection("posts").document(postId).collection("likes").document(uid)
        val postRef = db.collection("posts").document(postId)
        return try {
            if (isCurrentlyLiked) {
                likeRef.delete().await()
                postRef.update("likesCount", FieldValue.increment(-1), "updatedAt", FieldValue.serverTimestamp()).await()
            } else {
                likeRef.set(mapOf("userId" to uid, "createdAt" to FieldValue.serverTimestamp())).await()
                postRef.update("likesCount", FieldValue.increment(1), "updatedAt", FieldValue.serverTimestamp()).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, postRef.path)
            Result.failure(e)
        }
    }

    suspend fun toggleBookmark(postId: String, isCurrentlyBookmarked: Boolean): Result<Unit> {
        val uid = requireUserId()
        val bookmarkRef = db.collection("users").document(uid).collection("bookmarks").document(postId)
        val postRef = db.collection("posts").document(postId)
        return try {
            if (isCurrentlyBookmarked) {
                bookmarkRef.delete().await()
                postRef.update("bookmarksCount", FieldValue.increment(-1), "updatedAt", FieldValue.serverTimestamp()).await()
            } else {
                bookmarkRef.set(mapOf("postId" to postId, "savedAt" to FieldValue.serverTimestamp())).await()
                postRef.update("bookmarksCount", FieldValue.increment(1), "updatedAt", FieldValue.serverTimestamp()).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, postRef.path)
            Result.failure(e)
        }
    }

    // --- Comments ---

    fun observeComments(postId: String): Flow<List<Comment>> = flow {
        val path = "posts/$postId/comments"
        db.collection("posts").document(postId).collection("comments")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .snapshots()
            .map { it.toObjects(Comment::class.java) }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
            .collect { emit(it) }
    }

    suspend fun addComment(postId: String, content: String): Result<String> {
        val uid = requireUserId()
        val commentRef = db.collection("posts").document(postId).collection("comments").document()
        val postRef = db.collection("posts").document(postId)
        return try {
            val user = auth.currentUser
            val payload = mapOf(
                "postId" to postId,
                "userId" to uid,
                "username" to (user?.displayName ?: "User"),
                "userAvatarUrl" to (user?.photoUrl?.toString() ?: ""),
                "content" to content,
                "createdAt" to FieldValue.serverTimestamp()
            )
            commentRef.set(payload).await()
            postRef.update("commentsCount", FieldValue.increment(1), "updatedAt", FieldValue.serverTimestamp()).await()
            Result.success(commentRef.id)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, commentRef.path)
            Result.failure(e)
        }
    }

    // --- Stories ---

    fun observeStories(): Flow<List<Story>> = flow {
        val path = "stories"
        db.collection("stories")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(30)
            .snapshots()
            .map { it.toObjects(Story::class.java) }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
            .collect { emit(it) }
    }

    suspend fun createStory(mediaUrl: String, caption: String = ""): Result<String> {
        val uid = requireUserId()
        val docRef = db.collection("stories").document()
        return try {
            val user = auth.currentUser
            val payload = mapOf(
                "userId" to uid,
                "username" to (user?.displayName ?: "Explorer"),
                "userAvatarUrl" to (user?.photoUrl?.toString() ?: ""),
                "mediaUrl" to mediaUrl,
                "caption" to caption,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    // --- Conversations & Chat ---

    fun observeConversations(): Flow<List<Conversation>> = flow {
        val uid = requireUserId()
        val path = "conversations"
        db.collection("conversations")
            .whereArrayContains("participantIds", uid)
            .snapshots()
            .map { it.toObjects(Conversation::class.java) }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
            .collect { emit(it) }
    }

    suspend fun getOrCreateDirectConversation(otherUserId: String, otherUserName: String): Result<String> {
        val uid = requireUserId()
        return try {
            val existing = db.collection("conversations")
                .whereArrayContains("participantIds", uid)
                .get().await()

            val found = existing.documents.firstOrNull { doc ->
                val participants = doc.get("participantIds") as? List<*>
                val isGroup = doc.getBoolean("isGroup") ?: false
                !isGroup && participants != null && participants.contains(otherUserId)
            }

            if (found != null) {
                Result.success(found.id)
            } else {
                val newDoc = db.collection("conversations").document()
                val payload = mapOf(
                    "participantIds" to listOf(uid, otherUserId),
                    "isGroup" to false,
                    "groupName" to otherUserName,
                    "lastMessage" to "Started conversation",
                    "lastSenderId" to uid,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                newDoc.set(payload).await()
                Result.success(newDoc.id)
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "conversations")
            Result.failure(e)
        }
    }

    suspend fun createGroupConversation(groupName: String, memberIds: List<String>): Result<String> {
        val uid = requireUserId()
        val newDoc = db.collection("conversations").document()
        return try {
            val allMembers = (memberIds + uid).distinct()
            val payload = mapOf(
                "participantIds" to allMembers,
                "isGroup" to true,
                "groupName" to groupName,
                "groupPhotoUrl" to "",
                "adminIds" to listOf(uid),
                "lastMessage" to "Group created",
                "lastSenderId" to uid,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            newDoc.set(payload).await()
            Result.success(newDoc.id)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, newDoc.path)
            Result.failure(e)
        }
    }

    fun observeMessages(conversationId: String): Flow<List<Message>> = flow {
        val path = "conversations/$conversationId/messages"
        db.collection("conversations").document(conversationId).collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .limit(100)
            .snapshots()
            .map { it.toObjects(Message::class.java) }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
            .collect { emit(it) }
    }

    suspend fun sendMessage(
        conversationId: String,
        content: String,
        type: String = "text",
        mediaUrl: String = "",
        voiceDurationSeconds: Long = 0L
    ): Result<String> {
        val uid = requireUserId()
        val msgRef = db.collection("conversations").document(conversationId).collection("messages").document()
        val convRef = db.collection("conversations").document(conversationId)
        return try {
            val user = auth.currentUser
            val payload = mapOf(
                "conversationId" to conversationId,
                "senderId" to uid,
                "senderName" to (user?.displayName ?: "User"),
                "senderAvatar" to (user?.photoUrl?.toString() ?: ""),
                "content" to content,
                "type" to type,
                "mediaUrl" to mediaUrl,
                "voiceDurationSeconds" to voiceDurationSeconds,
                "status" to "sent",
                "createdAt" to FieldValue.serverTimestamp()
            )
            msgRef.set(payload).await()
            convRef.update(
                mapOf(
                    "lastMessage" to (if (type == "voice") "🎙️ Voice message" else if (type == "image") "📷 Photo" else content),
                    "lastSenderId" to uid,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(msgRef.id)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, msgRef.path)
            Result.failure(e)
        }
    }

    // --- Calls ---

    suspend fun initiateCall(
        receiverId: String,
        receiverName: String,
        callType: String = "voice"
    ): Result<String> {
        val uid = requireUserId()
        val callRef = db.collection("calls").document()
        return try {
            val user = auth.currentUser
            val payload = mapOf(
                "callerId" to uid,
                "callerName" to (user?.displayName ?: "Caller"),
                "callerAvatar" to (user?.photoUrl?.toString() ?: ""),
                "receiverId" to receiverId,
                "receiverName" to receiverName,
                "receiverAvatar" to "",
                "callType" to callType,
                "status" to "ringing",
                "durationSeconds" to 0L,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            callRef.set(payload).await()
            Result.success(callRef.id)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, callRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateCallStatus(callId: String, status: String, duration: Long = 0L): Result<Unit> {
        val callRef = db.collection("calls").document(callId)
        return try {
            callRef.update(
                "status", status,
                "durationSeconds", duration,
                "updatedAt", FieldValue.serverTimestamp()
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, callRef.path)
            Result.failure(e)
        }
    }

    fun observeActiveCalls(): Flow<List<CallRecord>> = flow {
        val uid = requireUserId()
        val path = "calls"
        db.collection("calls")
            .whereEqualTo("receiverId", uid)
            .whereEqualTo("status", "ringing")
            .snapshots()
            .map { it.toObjects(CallRecord::class.java) }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
            .collect { emit(it) }
    }

    // --- Notifications ---

    fun observeNotifications(): Flow<List<NotificationItem>> = flow {
        val uid = requireUserId()
        val path = "notifications"
        db.collection("notifications")
            .whereEqualTo("userId", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .snapshots()
            .map { it.toObjects(NotificationItem::class.java) }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
            .collect { emit(it) }
    }

    suspend fun markNotificationRead(notificationId: String): Result<Unit> {
        val docRef = db.collection("notifications").document(notificationId)
        return try {
            docRef.update("isRead", true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    // --- Search & Discovery ---

    fun searchUsers(queryText: String): Flow<List<UserProfile>> = flow {
        val path = "users"
        db.collection("users")
            .orderBy("username")
            .startAt(queryText.lowercase())
            .endAt(queryText.lowercase() + "\uf8ff")
            .limit(20)
            .snapshots()
            .map { it.toObjects(UserProfile::class.java) }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
            .collect { emit(it) }
    }
}
