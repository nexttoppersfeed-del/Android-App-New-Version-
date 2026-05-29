package com.nexttoppers.feed.data.model

import com.google.firebase.Timestamp

enum class MessageType { TEXT, IMAGE, ATTACHMENT, SYSTEM }

data class ChatMessage(
    val messageId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val message: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val type: String = MessageType.TEXT.name,
    val seen: Boolean = false,
    val deleted: Boolean = false,
    val attachmentUrl: String = ""
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "messageId"     to messageId,
        "senderId"      to senderId,
        "senderName"    to senderName,
        "message"       to message,
        "timestamp"     to timestamp,
        "type"          to type,
        "seen"          to seen,
        "deleted"       to deleted,
        "attachmentUrl" to attachmentUrl
    )
}
