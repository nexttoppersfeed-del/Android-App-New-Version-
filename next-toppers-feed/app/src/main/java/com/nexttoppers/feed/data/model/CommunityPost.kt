package com.nexttoppers.feed.data.model

import com.google.firebase.Timestamp

enum class PostType(val label: String, val emoji: String) {
    DISCUSSION("Discussion", "💬"),
    QUESTION("Question", "❓"),
    DOUBT("Doubt", "🤔"),
    MOTIVATION("Motivation", "🔥"),
    RESOURCE("Resource", "📚"),
    TOPPER_TIP("Topper Tip", "⭐"),
    ACHIEVEMENT("Achievement", "🏆")
}

data class CommunityPost(
    val postId: String = "",
    val userId: String = "",
    val username: String = "",
    val userPhoto: String = "",
    val type: String = PostType.DISCUSSION.name,
    val title: String = "",
    val content: String = "",
    val subject: String = "",
    val attachments: List<String> = emptyList(),
    val likes: List<String> = emptyList(),
    val commentsCount: Int = 0,
    val createdAt: Timestamp = Timestamp.now(),
    val edited: Boolean = false,
    val premiumOnly: Boolean = false,
    val pinned: Boolean = false,
    val hot: Boolean = false
) {
    val likeCount: Int get() = likes.size

    fun isLikedBy(uid: String) = likes.contains(uid)

    fun toMap(): Map<String, Any?> = mapOf(
        "postId"        to postId,
        "userId"        to userId,
        "username"      to username,
        "userPhoto"     to userPhoto,
        "type"          to type,
        "title"         to title,
        "content"       to content,
        "subject"       to subject,
        "attachments"   to attachments,
        "likes"         to likes,
        "commentsCount" to commentsCount,
        "createdAt"     to createdAt,
        "edited"        to edited,
        "premiumOnly"   to premiumOnly,
        "pinned"        to pinned,
        "hot"           to hot
    )
}
