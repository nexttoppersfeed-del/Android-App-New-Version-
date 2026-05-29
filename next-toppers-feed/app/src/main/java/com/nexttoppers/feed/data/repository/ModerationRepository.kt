package com.nexttoppers.feed.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class Report(
    val reportId: String = "",
    val type: String = "",
    val targetId: String = "",
    val reportedBy: String = "",
    val reason: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val resolved: Boolean = false,
    val resolvedBy: String = "",
    val chatId: String = "",
    val messageId: String = ""
)

data class ModerationLog(
    val logId: String = "",
    val action: String = "",
    val targetType: String = "",
    val targetId: String = "",
    val performedBy: String = "",
    val note: String = "",
    val timestamp: Timestamp = Timestamp.now()
)

@Singleton
class ModerationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val reportsCol = firestore.collection("reports")
    private val logsCol = firestore.collection("moderationLogs")
    private val postsCol = firestore.collection("communityPosts")
    private val commentsCol = firestore.collection("comments")
    private val chatsCol = firestore.collection("chats")
    private val usersCol = firestore.collection("users")

    // ── Reports ───────────────────────────────────────────────────────────────────

    fun observeReports(resolved: Boolean = false): Flow<Result<List<Report>>> = callbackFlow {
        val query = reportsCol
            .whereEqualTo("resolved", resolved)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)

        val listener = query.addSnapshotListener { snap, err ->
            if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
            val reports = snap?.documents?.mapNotNull { doc ->
                try {
                    Report(
                        reportId   = doc.id,
                        type       = doc.getString("type") ?: "",
                        targetId   = doc.getString("targetId") ?: "",
                        reportedBy = doc.getString("reportedBy") ?: "",
                        reason     = doc.getString("reason") ?: "",
                        timestamp  = doc.getTimestamp("timestamp") ?: Timestamp.now(),
                        resolved   = doc.getBoolean("resolved") ?: false,
                        resolvedBy = doc.getString("resolvedBy") ?: "",
                        chatId     = doc.getString("chatId") ?: "",
                        messageId  = doc.getString("messageId") ?: ""
                    )
                } catch (e: Exception) { null }
            } ?: emptyList()
            trySend(Result.success(reports))
        }
        awaitClose { listener.remove() }
    }

    suspend fun resolveReport(reportId: String): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid ?: throw Exception("Not authenticated")
        reportsCol.document(reportId).update(mapOf(
            "resolved"   to true,
            "resolvedBy" to uid,
            "resolvedAt" to Timestamp.now()
        )).await()
        logAction("RESOLVE_REPORT", "report", reportId)
    }

    // ── Content moderation ────────────────────────────────────────────────────────

    suspend fun deletePost(postId: String, reason: String = "Violated community guidelines"): Result<Unit> =
        runCatching {
            postsCol.document(postId).delete().await()
            logAction("DELETE_POST", "post", postId, reason)
        }

    suspend fun deleteComment(commentId: String): Result<Unit> = runCatching {
        commentsCol.document(commentId).delete().await()
        logAction("DELETE_COMMENT", "comment", commentId)
    }

    suspend fun deleteMessage(chatId: String, messageId: String): Result<Unit> = runCatching {
        chatsCol.document(chatId).collection("messages")
            .document(messageId)
            .update(mapOf("deleted" to true, "message" to "⚠️ Removed by moderator")).await()
        logAction("DELETE_MESSAGE", "message", messageId)
    }

    // ── User moderation ───────────────────────────────────────────────────────────

    suspend fun banUser(uid: String, reason: String = ""): Result<Unit> = runCatching {
        usersCol.document(uid).update(mapOf(
            "banned"    to true,
            "bannedAt"  to Timestamp.now(),
            "banReason" to reason
        )).await()
        logAction("BAN_USER", "user", uid, reason)
    }

    suspend fun unbanUser(uid: String): Result<Unit> = runCatching {
        usersCol.document(uid).update("banned", false).await()
        logAction("UNBAN_USER", "user", uid)
    }

    // ── Moderation logs ───────────────────────────────────────────────────────────

    fun observeModerationLogs(limit: Long = 30): Flow<Result<List<ModerationLog>>> = callbackFlow {
        val query = logsCol.orderBy("timestamp", Query.Direction.DESCENDING).limit(limit)
        val listener = query.addSnapshotListener { snap, err ->
            if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
            val logs = snap?.documents?.mapNotNull { doc ->
                try {
                    ModerationLog(
                        logId       = doc.id,
                        action      = doc.getString("action") ?: "",
                        targetType  = doc.getString("targetType") ?: "",
                        targetId    = doc.getString("targetId") ?: "",
                        performedBy = doc.getString("performedBy") ?: "",
                        note        = doc.getString("note") ?: "",
                        timestamp   = doc.getTimestamp("timestamp") ?: Timestamp.now()
                    )
                } catch (e: Exception) { null }
            } ?: emptyList()
            trySend(Result.success(logs))
        }
        awaitClose { listener.remove() }
    }

    private suspend fun logAction(action: String, targetType: String, targetId: String, note: String = "") {
        runCatching {
            val uid = auth.currentUser?.uid ?: "system"
            logsCol.document(UUID.randomUUID().toString()).set(mapOf(
                "action"      to action,
                "targetType"  to targetType,
                "targetId"    to targetId,
                "performedBy" to uid,
                "note"        to note,
                "timestamp"   to Timestamp.now()
            )).await()
        }
    }
}
