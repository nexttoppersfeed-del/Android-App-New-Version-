package com.nexttoppers.feed.data.model

import com.google.firebase.Timestamp

data class Group(
    val groupId: String = "",
    val groupName: String = "",
    val groupPhoto: String = "",
    val description: String = "",
    val members: List<String> = emptyList(),
    val admins: List<String> = emptyList(),
    val createdAt: Timestamp = Timestamp.now(),
    val premiumOnly: Boolean = false,
    val subject: String = "",
    val emoji: String = ""
) {
    val memberCount: Int get() = members.size
    fun isMember(uid: String) = members.contains(uid)
    fun isAdmin(uid: String) = admins.contains(uid)

    fun toMap(): Map<String, Any?> = mapOf(
        "groupId"     to groupId,
        "groupName"   to groupName,
        "groupPhoto"  to groupPhoto,
        "description" to description,
        "members"     to members,
        "admins"      to admins,
        "createdAt"   to createdAt,
        "premiumOnly" to premiumOnly,
        "subject"     to subject,
        "emoji"       to emoji
    )
}

val defaultGroups = listOf(
    Group(groupId = "math_group",       groupName = "Mathematics",   emoji = "📐", subject = "Maths",    description = "Discuss maths problems, share shortcuts and solutions"),
    Group(groupId = "science_group",    groupName = "Science",       emoji = "🔬", subject = "Science",  description = "Physics, Chemistry, Biology discussions"),
    Group(groupId = "sst_group",        groupName = "SST",           emoji = "🌍", subject = "SST",      description = "Social Studies, History, Geography discussions"),
    Group(groupId = "motivation_group", groupName = "Motivation",    emoji = "🔥", subject = "General",  description = "Daily motivation, study tips and success stories"),
    Group(groupId = "premium_group",    groupName = "Premium Lounge",emoji = "⭐", subject = "Premium",  description = "Exclusive group for premium members", premiumOnly = true)
)
