package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.models.Post
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class VyroRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createPost_authenticatedUser_createsDocumentAndReturnsId() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repo = VyroRepository(firestore)

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.createPost(
                caption = "Testing VYRO social network feed item",
                mediaUrls = listOf("https://images.unsplash.com/photo-1"),
                mediaType = "image"
            )
        }

        assertTrue("Expected post creation to succeed", result.isSuccess)
        val postId = result.getOrThrow()
        assertNotNull(postId)
        assertTrue(postId.isNotEmpty())
    }

    @Test
    fun observeFeedPosts_authenticatedUser_emitsPosts() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repo = VyroRepository(firestore)

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.createPost(caption = "Real-time feed item for observation")
        }
        assertTrue(createResult.isSuccess)
        val postId = createResult.getOrThrow()

        val posts = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeFeedPosts().first { list -> list.any { it.id == postId } }
        }
        assertTrue(posts.any { it.id == postId })
    }

    @Test
    fun observeFeedPosts_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repo = VyroRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repo.observeFeedPosts().first()
            }
            fail("Expected unauthenticated query to throw exception")
        } catch (e: Exception) {
            val firestoreException = e.unwrapFirestoreException()
            assertNotNull("Expected root FirebaseFirestoreException, got: $e", firestoreException)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException?.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice_vyro@test.com"
        const val DEFAULT_TIMEOUT_MS = 6000L
        const val FLOW_TIMEOUT_MS = 6000L
    }
}
