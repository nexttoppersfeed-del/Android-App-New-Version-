package com.nexttoppers.feed.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nexttoppers.feed.data.model.Comment
import com.nexttoppers.feed.data.model.CommunityPost
import com.nexttoppers.feed.data.model.Reply
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommunityRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val postsCol   = firestore.collection("communityPosts")
    private val commentsCol = firestore.collection("comments")
    private val repliesCol  = firestore.collection("replies")

    // ── Posts ────────────────────────────────────────────────────────────────────

    fun observePosts(filterType: String? = null, limit: Long = 30): Flow<Result<List<CommunityPost>>> =
        callbackFlow {
            var query: Query = postsCol.orderBy("createdAt", Query.Direction.DESCENDING).limit(limit)
            if (!filterType.isNullOrBlank()) query = query.whereEqualTo("type", filterType)

            val listener = query.addSnapshotListener { snap, err ->
                if (err != null) {
                    trySend(Result.failure(err))
                    return@addSnapshotListener
                }
                val posts = snap?.documents?.mapNotNull { doc ->
                    try { doc.toObject(CommunityPost::class.java)?.copy(postId = doc.id) } catch (e: Exception) { null }
                } ?: emptyList()
                trySend(Result.success(posts))
            }
            awaitClose { listener.remove() }
        }

    fun observePostsBySubject(subject: String, limit: Long = 30): Flow<Result<List<CommunityPost>>> =
        callbackFlow {
            val query = postsCol
                .whereEqualTo("subject", subject)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit)

            val listener = query.addSnapshotListener { snap, err ->
                if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
                val posts = snap?.documents?.mapNotNull { doc ->
                    try { doc.toObject(CommunityPost::class.java)?.copy(postId = doc.id) } catch (e: Exception) { null }
                } ?: emptyList()
                trySend(Result.success(posts))
            }
            awaitClose { listener.remove() }
        }

    fun observePost(postId: String): Flow<Result<CommunityPost>> = callbackFlow {
        val listener = postsCol.document(postId).addSnapshotListener { snap, err ->
            if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
            val post = snap?.toObject(CommunityPost::class.java)?.copy(postId = snap.id)
            if (post != null) trySend(Result.success(post))
            else trySend(Result.failure(Exception("Post not found")))
        }
        awaitClose { listener.remove() }
    }

    suspend fun createPost(post: CommunityPost): Result<String> = runCatching {
        val id = UUID.randomUUID().toString()
        val newPost = post.copy(postId = id)
        postsCol.document(id).set(newPost.toMap()).await()
        id
    }

    suspend fun likePost(postId: String, uid: String): Result<Unit> = runCatching {
        postsCol.document(postId).update("likes", FieldValue.arrayUnion(uid)).await()
    }

    suspend fun unlikePost(postId: String, uid: String): Result<Unit> = runCatching {
        postsCol.document(postId).update("likes", FieldValue.arrayRemove(uid)).await()
    }

    suspend fun toggleLike(postId: String, uid: String, currentlyLiked: Boolean): Result<Unit> =
        if (currentlyLiked) unlikePost(postId, uid) else likePost(postId, uid)

    suspend fun deletePost(postId: String): Result<Unit> = runCatching {
        postsCol.document(postId).delete().await()
    }

    suspend fun reportPost(postId: String, reason: String): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
        firestore.collection("reports").add(mapOf(
            "type"      to "post",
            "targetId"  to postId,
            "reportedBy" to uid,
            "reason"    to reason,
            "timestamp" to com.google.firebase.Timestamp.now()
        )).await()
    }

    // ── Comments ─────────────────────────────────────────────────────────────────

    fun observeComments(postId: String): Flow<Result<List<Comment>>> = callbackFlow {
        val query = commentsCol
            .whereEqualTo("postId", postId)
            .orderBy("createdAt", Query.Direction.ASCENDING)

        val listener = query.addSnapshotListener { snap, err ->
            if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
            val comments = snap?.documents?.mapNotNull { doc ->
                try { doc.toObject(Comment::class.java)?.copy(commentId = doc.id) } catch (e: Exception) { null }
            } ?: emptyList()
            trySend(Result.success(comments))
        }
        awaitClose { listener.remove() }
    }

    suspend fun addComment(comment: Comment): Result<String> = runCatching {
        val id = UUID.randomUUID().toString()
        val newComment = comment.copy(commentId = id)
        commentsCol.document(id).set(newComment.toMap()).await()
        postsCol.document(comment.postId)
            .update("commentsCount", FieldValue.increment(1)).await()
        id
    }

    suspend fun likeComment(commentId: String, uid: String): Result<Unit> = runCatching {
        commentsCol.document(commentId).update("likes", FieldValue.arrayUnion(uid)).await()
    }

    suspend fun unlikeComment(commentId: String, uid: String): Result<Unit> = runCatching {
        commentsCol.document(commentId).update("likes", FieldValue.arrayRemove(uid)).await()
    }

    // ── Replies ──────────────────────────────────────────────────────────────────

    fun observeReplies(commentId: String): Flow<Result<List<Reply>>> = callbackFlow {
        val query = repliesCol
            .whereEqualTo("commentId", commentId)
            .orderBy("createdAt", Query.Direction.ASCENDING)

        val listener = query.addSnapshotListener { snap, err ->
            if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
            val replies = snap?.documents?.mapNotNull { doc ->
                try { doc.toObject(Reply::class.java)?.copy(replyId = doc.id) } catch (e: Exception) { null }
            } ?: emptyList()
            trySend(Result.success(replies))
        }
        awaitClose { listener.remove() }
    }

    suspend fun addReply(reply: Reply): Result<String> = runCatching {
        val id = UUID.randomUUID().toString()
        val newReply = reply.copy(replyId = id)
        repliesCol.document(id).set(newReply.toMap()).await()
        commentsCol.document(reply.commentId)
            .update("repliesCount", FieldValue.increment(1)).await()
        id
    }
}
