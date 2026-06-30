package com.nexttoppers.feed.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nexttoppers.feed.data.model.Chat
import com.nexttoppers.feed.data.model.ChatMessage
import com.nexttoppers.feed.data.model.ChatType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val chatMetaCol     = firestore.collection("privateChatMeta")
    private val privateChatsCol = firestore.collection("privateChats")

    // ── Mappers ───────────────────────────────────────────────────────────────

    private fun mapToChat(doc: DocumentSnapshot): Chat? {
        val data = doc.data ?: return null
        return try {
            val uid = auth.currentUser?.uid ?: ""
            // F13: website uses flat "unread_{uid}" fields — not a nested "unread" map
            val myUnread = (data["unread_$uid"] as? Long)?.toInt()
                // legacy fallback: nested map written by older app versions
                ?: (data["unread"] as? Map<*, *>)?.let { map ->
                    (map[uid] as? Long)?.toInt() ?: (map[uid] as? Int)
                } ?: 0

            Chat(
                chatId            = doc.id,
                type              = data["type"] as? String ?: ChatType.PRIVATE.name,
                participants      = (data["participants"] as? List<*>)
                                        ?.filterIsInstance<String>() ?: emptyList(),
                participantNames  = (data["participantNames"] as? Map<*, *>)
                                        ?.mapKeys   { it.key as? String ?: "" }
                                        ?.mapValues { it.value as? String ?: "" }
                                    ?: emptyMap(),
                participantPhotos = (data["participantPhotos"] as? Map<*, *>)
                                        ?.mapKeys   { it.key as? String ?: "" }
                                        ?.mapValues { it.value as? String ?: "" }
                                    ?: emptyMap(),
                lastMessage       = data["lastMessage"] as? String ?: "",
                lastMessageTime   = data["lastMessageAt"] as? Timestamp
                                    ?: data["updatedAt"] as? Timestamp
                                    ?: data["lastMessageTime"] as? Timestamp
                                    ?: Timestamp.now(),
                lastMessageSender = data["lastSenderId"] as? String
                                    ?: data["lastMessageSender"] as? String ?: "",
                // F13: return current user's flat unread count
                unreadCount       = mapOf(uid to myUnread),
                pinned            = data["pinned"] as? Boolean ?: false,
                groupName         = data["groupName"] as? String ?: "",
                groupPhoto        = data["groupPhoto"] as? String ?: "",
                createdAt         = data["createdAt"] as? Timestamp ?: Timestamp.now()
            )
        } catch (e: Exception) { null }
    }

    private fun mapToMessage(doc: DocumentSnapshot): ChatMessage? {
        val data = doc.data ?: return null
        return try {
            ChatMessage(
                messageId     = doc.id,
                senderId      = data["senderId"] as? String ?: "",
                senderName    = data["senderName"] as? String ?: "",
                // F12: website writes "message" field — read it with "text" / "content" as fallback
                message       = data["message"] as? String
                                ?: data["text"] as? String
                                ?: data["content"] as? String ?: "",
                // F12: website uses "createdAt" — read it with "timestamp" as fallback
                timestamp     = data["createdAt"] as? Timestamp
                                ?: data["timestamp"] as? Timestamp
                                ?: Timestamp.now(),
                type          = data["type"] as? String ?: "TEXT",
                seen          = data["seen"] as? Boolean ?: false,
                deleted       = data["deleted"] as? Boolean ?: false,
                attachmentUrl = data["attachmentUrl"] as? String ?: ""
            )
        } catch (e: Exception) { null }
    }

    // ── Chat list ─────────────────────────────────────────────────────────────

    fun observeUserChats(uid: String): Flow<Result<List<Chat>>> = callbackFlow {
        val query = chatMetaCol
            .whereArrayContains("participants", uid)
            .orderBy("lastMessageAt", Query.Direction.DESCENDING)

        val listener = query.addSnapshotListener { snap, err ->
            if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
            val chats = snap?.documents?.mapNotNull { mapToChat(it) } ?: emptyList()
            trySend(Result.success(chats))
        }
        awaitClose { listener.remove() }
    }

    fun observeChat(chatId: String): Flow<Result<Chat>> = callbackFlow {
        val listener = chatMetaCol.document(chatId).addSnapshotListener { snap, err ->
            if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
            val chat = snap?.let { mapToChat(it) }
            if (chat != null) trySend(Result.success(chat))
            else trySend(Result.failure(Exception("Chat not found")))
        }
        awaitClose { listener.remove() }
    }

    // ── Messages ──────────────────────────────────────────────────────────────

    fun observeMessages(chatId: String, limit: Long = 200): Flow<Result<List<ChatMessage>>> =
        callbackFlow {
            val query = privateChatsCol.document(chatId)
                .collection("messages")
                // F12: website orders by "createdAt"
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .limitToLast(limit)

            val listener = query.addSnapshotListener { snap, err ->
                if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
                val messages = snap?.documents?.mapNotNull { mapToMessage(it) } ?: emptyList()
                trySend(Result.success(messages))
            }
            awaitClose { listener.remove() }
        }

    suspend fun sendMessage(chatId: String, message: ChatMessage): Result<String> = runCatching {
        val id  = UUID.randomUUID().toString()
        val now = Timestamp.now()

        // F12: write "message" field and "createdAt" to match website schema
        privateChatsCol.document(chatId)
            .collection("messages")
            .document(id)
            .set(mapOf(
                "senderId"      to message.senderId,
                "senderName"    to message.senderName,
                "message"       to message.message,
                "createdAt"     to now,
                "type"          to message.type,
                "seen"          to message.seen,
                "deleted"       to message.deleted,
                "attachmentUrl" to message.attachmentUrl
            )).await()

        chatMetaCol.document(chatId).update(mapOf(
            "lastMessage"   to message.message,
            "lastMessageAt" to now,
            "lastSenderId"  to message.senderId,
            "updatedAt"     to now
        )).await()
        id
    }

    // F13: use flat "unread_{uid}" field to match website schema
    suspend fun markMessagesAsSeen(chatId: String, uid: String): Result<Unit> = runCatching {
        chatMetaCol.document(chatId).update("unread_$uid", 0).await()
    }

    suspend fun incrementUnread(chatId: String, recipientUid: String): Result<Unit> = runCatching {
        chatMetaCol.document(chatId).update("unread_$recipientUid", FieldValue.increment(1)).await()
    }

    suspend fun deleteMessage(chatId: String, messageId: String): Result<Unit> = runCatching {
        privateChatsCol.document(chatId)
            .collection("messages")
            .document(messageId)
            // website soft-deletes: sets message to "" and deleted to true
            .update(mapOf(
                "deleted" to true,
                "message" to ""
            ))
            .await()
    }

    // ── Create / find chats ───────────────────────────────────────────────────

    suspend fun getOrCreatePrivateChat(
        myUid: String,
        myName: String,
        myPhoto: String,
        otherUid: String,
        otherName: String,
        otherPhoto: String
    ): Result<String> = runCatching {
        // chatId format: sorted alphabetically — matches website's Chat.tsx
        val chatId = if (myUid < otherUid) "${myUid}_${otherUid}" else "${otherUid}_${myUid}"
        val docRef = chatMetaCol.document(chatId)
        val snap   = docRef.get().await()

        if (!snap.exists()) {
            // F13: write flat "unread_{uid}" fields to match website schema
            docRef.set(mapOf(
                "participants"           to listOf(myUid, otherUid),
                "participantNames"       to mapOf(myUid to myName, otherUid to otherName),
                "participantPhotos"      to mapOf(myUid to myPhoto, otherUid to otherPhoto),
                "type"                   to ChatType.PRIVATE.name,
                "lastMessage"            to "",
                "lastMessageAt"          to Timestamp.now(),
                "lastSenderId"           to "",
                "unread_$myUid"          to 0,
                "unread_$otherUid"       to 0,
                "createdAt"              to Timestamp.now()
            )).await()
        }
        chatId
    }

    suspend fun createGroupChat(
        groupName: String,
        participants: List<String>,
        participantNames: Map<String, String>
    ): Result<String> = runCatching {
        val chatId = UUID.randomUUID().toString()
        val unreadMap = participants.associate { "unread_$it" to 0 }
        chatMetaCol.document(chatId).set(mapOf(
            "type"             to ChatType.GROUP.name,
            "groupName"        to groupName,
            "participants"     to participants,
            "participantNames" to participantNames,
            "lastMessage"      to "",
            "lastMessageAt"    to Timestamp.now(),
            "createdAt"        to Timestamp.now()
        ) + unreadMap).await()
        chatId
    }

    suspend fun reportMessage(chatId: String, messageId: String, reason: String): Result<Unit> =
        runCatching {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
            firestore.collection("reports").add(mapOf(
                "type"       to "message",
                "chatId"     to chatId,
                "messageId"  to messageId,
                "reportedBy" to uid,
                "reason"     to reason,
                "timestamp"  to Timestamp.now()
            )).await()
        }
}
