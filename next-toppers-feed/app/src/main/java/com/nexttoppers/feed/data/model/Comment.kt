package com.nexttoppers.feed.data.model

import com.google.firebase.Timestamp

data class Comment(
    val commentId: String = "",
    val postId: String = "",
    val userId: String = "",
    val username: String = "",
    val userPhoto: String = "",
    val content: String = "",
    val likes: List<String> = emptyList(),
    val repliesCount: Int = 0,
    val createdAt: Timestamp = Timestamp.now(),
    val edited: Boolean = false
) {
    val likeCount: Int get() = likes.size
    fun isLikedBy(uid: String) = likes.contains(uid)

    fun toMap(): Map<String, Any?> = mapOf(
        "commentId"    to commentId,
        "postId"       to postId,
        "userId"       to userId,
        "username"     to username,
        "userPhoto"    to userPhoto,
        "content"      to content,
        "likes"        to likes,
        "repliesCount" to repliesCount,
        "createdAt"    to createdAt,
        "edited"       to edited
    )
}

data class Reply(
    val replyId: String = "",
    val commentId: String = "",
    val postId: String = "",
    val userId: String = "",
    val username: String = "",
    val userPhoto: String = "",
    val content: String = "",
    val likes: List<String> = emptyList(),
    val createdAt: Timestamp = Timestamp.now()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "replyId"   to replyId,
        "commentId" to commentId,
        "postId"    to postId,
        "userId"    to userId,
        "username"  to username,
        "userPhoto" to userPhoto,
        "content"   to content,
        "likes"     to likes,
        "createdAt" to createdAt
    )
}
