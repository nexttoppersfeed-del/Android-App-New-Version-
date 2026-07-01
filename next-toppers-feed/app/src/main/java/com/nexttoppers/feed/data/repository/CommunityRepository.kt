package com.nexttoppers.feed.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nexttoppers.feed.data.model.Comment
import com.nexttoppers.feed.data.model.CommunityPost
import com.nexttoppers.feed.data.model.Reply
import com.nexttoppers.feed.util.resolveTimestamp
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
    // communityMessages — live collection used by both website and app
    private val postsCol    = firestore.collection("communityMessages")
    private val commentsCol = firestore.collection("comments")
    private val repliesCol  = firestore.collection("replies")

    // Manual mapping to handle flexible field names across both website & app schemas
    private fun mapPost(doc: DocumentSnapshot): CommunityPost? {
        val data = doc.data ?: return null
        return try {
            CommunityPost(
                postId        = doc.id,
                userId        = data["userId"] as? String
                                ?: data["senderId"] as? String ?: "",
                username      = data["username"] as? String
                                ?: data["senderName"] as? String ?: "Anonymous",
                userPhoto     = data["userPhoto"] as? String
                                ?: data["userAvatar"] as? String
                                ?: data["photoUrl"] as? String ?: "",
                type          = data["type"] as? String ?: "DISCUSSION",
                title         = data["title"] as? String ?: "",
                content       = data["text"] as? String
                                ?: data["content"] as? String
                                ?: data["message"] as? String ?: "",
                subject       = data["subject"] as? String ?: "",
                likes         = (data["likes"] as? List<*>)
                                    ?.filterIsInstance<String>() ?: emptyList(),
                commentsCount = ((data["replyCount"] ?: data["commentsCount"] ?: 0L) as? Long)
                                    ?.toInt()
                                ?: (data["replyCount"] as? Int)
                                ?: (data["commentsCount"] as? Int) ?: 0,
                createdAt     = data["timestamp"] as? Timestamp
                                ?: data["createdAt"] as? Timestamp ?: Timestamp.now(),
                pinned        = data["pinned"] as? Boolean ?: false,
                hot           = data["hot"] as? Boolean ?: false,
                premiumOnly   = data["premiumOnly"] as? Boolean ?: false
            )
        } catch (e: Exception) { null }
    }

    // ── Posts ────────────────────────────────────────────────────────────────────

    fun observePosts(filterType: String? = null, limit: Long = 50):
            Flow<Result<List<CommunityPost>>> = callbackFlow {
        var baseQuery: Query = postsCol.limit(limit)
        if (!filterType.isNullOrBlank()) {
            baseQuery = baseQuery.whereEqualTo("type", filterType)
        }

        var createdAtItems: List<CommunityPost> = emptyList()
        var timestampItems: List<CommunityPost> = emptyList()

        fun mergeAndSend() {
            val merged = (createdAtItems + timestampItems)
                .distinctBy { it.postId }
                .sortedByDescending { it.createdAt.seconds }
            trySend(Result.success(merged))
        }

        val createdAtListener = baseQuery
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
                createdAtItems = snap?.documents?.mapNotNull { mapPost(it) } ?: emptyList()
                mergeAndSend()
            }

        val timestampListener = baseQuery
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                timestampItems = snap?.documents?.mapNotNull { mapPost(it) } ?: emptyList()
                mergeAndSend()
            }

        awaitClose {
            createdAtListener.remove()
            timestampListener.remove()
        }
    }

    fun observePostsBySubject(subject: String, limit: Long = 30):
            Flow<Result<List<CommunityPost>>> = callbackFlow {
        var createdAtItems: List<CommunityPost> = emptyList()
        var timestampItems: List<CommunityPost> = emptyList()

        fun mergeAndSend() {
            val merged = (createdAtItems + timestampItems)
                .distinctBy { it.postId }
                .sortedByDescending { it.createdAt.seconds }
            trySend(Result.success(merged))
        }

        val createdAtListener = postsCol
            .whereEqualTo("subject", subject)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
                createdAtItems = snap?.documents?.mapNotNull { mapPost(it) } ?: emptyList()
                mergeAndSend()
            }

        val timestampListener = postsCol
            .whereEqualTo("subject", subject)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snap, _ ->
                timestampItems = snap?.documents?.mapNotNull { mapPost(it) } ?: emptyList()
                mergeAndSend()
            }

        awaitClose {
            createdAtListener.remove()
            timestampListener.remove()
        }
    }

    fun observePost(postId: String): Flow<Result<CommunityPost>> = callbackFlow {
        val listener = postsCol.document(postId).addSnapshotListener { snap, err ->
            if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
            val post = snap?.let { mapPost(it) }
            if (post != null) trySend(Result.success(post))
            else trySend(Result.failure(Exception("Post not found")))
        }
        awaitClose { listener.remove() }
    }

    suspend fun createPost(post: CommunityPost): Result<String> = runCatching {
        val id  = UUID.randomUUID().toString()
        val now = Timestamp.now()
        postsCol.document(id).set(mapOf(
            // F19: write website field names ("senderId","senderName","senderPhoto","message","createdAt")
            // Also write legacy names so old app versions can still read the doc
            "senderId"    to post.userId,
            "senderName"  to post.username,
            "senderPhoto" to post.userPhoto,
            "message"     to post.content,
            // Legacy names kept for backwards compat reads from older app installs
            "userId"      to post.userId,
            "username"    to post.username,
            "userPhoto"   to post.userPhoto,
            "text"        to post.content,
            "type"        to post.type,
            "title"       to post.title,
            "subject"     to post.subject,
            "likes"       to post.likes,
            "replyCount"  to post.commentsCount,
            "createdAt"   to now,
            "pinned"      to post.pinned,
            "hot"         to post.hot,
            "premiumOnly" to post.premiumOnly
        )).await()
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
            "type"       to "post",
            "targetId"   to postId,
            "reportedBy" to uid,
            "reason"     to reason,
            "timestamp"  to Timestamp.now()
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
                try {
                    doc.toObject(Comment::class.java)
                        ?.copy(commentId = doc.id, createdAt = doc.resolveTimestamp("createdAt"))
                } catch (e: Exception) { null }
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
            .update("replyCount", FieldValue.increment(1)).await()
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
                try {
                    doc.toObject(Reply::class.java)
                        ?.copy(replyId = doc.id, createdAt = doc.resolveTimestamp("createdAt"))
                } catch (e: Exception) { null }
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
