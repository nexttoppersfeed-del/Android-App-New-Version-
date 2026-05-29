package com.nexttoppers.feed.data.model

import com.google.firebase.Timestamp

enum class ChatType { PRIVATE, GROUP, STUDY_ROOM, SUBJECT_GROUP }

data class Chat(
    val chatId: String = "",
    val type: String = ChatType.PRIVATE.name,
    val participants: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val participantPhotos: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val lastMessageTime: Timestamp = Timestamp.now(),
    val lastMessageSender: String = "",
    val unreadCount: Map<String, Int> = emptyMap(),
    val pinned: Boolean = false,
    val groupName: String = "",
    val groupPhoto: String = "",
    val createdAt: Timestamp = Timestamp.now()
) {
    fun getDisplayName(myUid: String): String {
        return if (type == ChatType.PRIVATE.name) {
            participantNames.entries
                .firstOrNull { it.key != myUid }
                ?.value ?: "Unknown"
        } else {
            groupName
        }
    }

    fun getDisplayPhoto(myUid: String): String {
        return if (type == ChatType.PRIVATE.name) {
            participantPhotos.entries
                .firstOrNull { it.key != myUid }
                ?.value ?: ""
        } else {
            groupPhoto
        }
    }

    fun getUnreadCount(uid: String): Int = unreadCount[uid] ?: 0

    fun toMap(): Map<String, Any?> = mapOf(
        "chatId"              to chatId,
        "type"                to type,
        "participants"        to participants,
        "participantNames"    to participantNames,
        "participantPhotos"   to participantPhotos,
        "lastMessage"         to lastMessage,
        "lastMessageTime"     to lastMessageTime,
        "lastMessageSender"   to lastMessageSender,
        "unreadCount"         to unreadCount,
        "pinned"              to pinned,
        "groupName"           to groupName,
        "groupPhoto"          to groupPhoto,
        "createdAt"           to createdAt
    )
}
