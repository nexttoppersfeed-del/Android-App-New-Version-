package com.nexttoppers.feed.data.repository

import com.google.firebase.auth.FirebaseAuth
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
    private val chatsCol = firestore.collection("chats")

    // ── Chat list ────────────────────────────────────────────────────────────────

    fun observeUserChats(uid: String): Flow<Result<List<Chat>>> = callbackFlow {
        val query = chatsCol
            .whereArrayContains("participants", uid)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)

        val listener = query.addSnapshotListener { snap, err ->
            if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
            val chats = snap?.documents?.mapNotNull { doc ->
                try { doc.toObject(Chat::class.java)?.copy(chatId = doc.id) } catch (e: Exception) { null }
            } ?: emptyList()
            trySend(Result.success(chats))
        }
        awaitClose { listener.remove() }
    }

    fun observeChat(chatId: String): Flow<Result<Chat>> = callbackFlow {
        val listener = chatsCol.document(chatId).addSnapshotListener { snap, err ->
            if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
            val chat = snap?.toObject(Chat::class.java)?.copy(chatId = snap.id)
            if (chat != null) trySend(Result.success(chat))
            else trySend(Result.failure(Exception("Chat not found")))
        }
        awaitClose { listener.remove() }
    }

    // ── Messages ─────────────────────────────────────────────────────────────────

    fun observeMessages(chatId: String, limit: Long = 50): Flow<Result<List<ChatMessage>>> =
        callbackFlow {
            val query = chatsCol.document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .limitToLast(limit)

            val listener = query.addSnapshotListener { snap, err ->
                if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
                val messages = snap?.documents?.mapNotNull { doc ->
                    try { doc.toObject(ChatMessage::class.java)?.copy(messageId = doc.id) } catch (e: Exception) { null }
                } ?: emptyList()
                trySend(Result.success(messages))
            }
            awaitClose { listener.remove() }
        }

    suspend fun sendMessage(chatId: String, message: ChatMessage): Result<String> = runCatching {
        val id = UUID.randomUUID().toString()
        val newMsg = message.copy(messageId = id)
        chatsCol.document(chatId)
            .collection("messages")
            .document(id)
            .set(newMsg.toMap())
            .await()

        chatsCol.document(chatId).update(mapOf(
            "lastMessage"      to message.message,
            "lastMessageTime"  to message.timestamp,
            "lastMessageSender" to message.senderId
        )).await()
        id
    }

    suspend fun markMessagesAsSeen(chatId: String, uid: String): Result<Unit> = runCatching {
        chatsCol.document(chatId).update("unreadCount.$uid", 0).await()
    }

    suspend fun deleteMessage(chatId: String, messageId: String): Result<Unit> = runCatching {
        chatsCol.document(chatId)
            .collection("messages")
            .document(messageId)
            .update("deleted", true, "message", "This message was deleted")
            .await()
    }

    // ── Create / find chats ──────────────────────────────────────────────────────

    suspend fun getOrCreatePrivateChat(
        myUid: String,
        myName: String,
        myPhoto: String,
        otherUid: String,
        otherName: String,
        otherPhoto: String
    ): Result<String> = runCatching {
        val chatId = if (myUid < otherUid) "${myUid}_${otherUid}" else "${otherUid}_${myUid}"
        val docRef = chatsCol.document(chatId)
        val snap = docRef.get().await()

        if (!snap.exists()) {
            val chat = Chat(
                chatId = chatId,
                type = ChatType.PRIVATE.name,
                participants = listOf(myUid, otherUid),
                participantNames = mapOf(myUid to myName, otherUid to otherName),
                participantPhotos = mapOf(myUid to myPhoto, otherUid to otherPhoto)
            )
            docRef.set(chat.toMap()).await()
        }
        chatId
    }

    suspend fun createGroupChat(
        groupName: String,
        participants: List<String>,
        participantNames: Map<String, String>
    ): Result<String> = runCatching {
        val chatId = UUID.randomUUID().toString()
        val chat = Chat(
            chatId = chatId,
            type = ChatType.GROUP.name,
            participants = participants,
            participantNames = participantNames,
            groupName = groupName
        )
        chatsCol.document(chatId).set(chat.toMap()).await()
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
                "timestamp"  to com.google.firebase.Timestamp.now()
            )).await()
        }
}
