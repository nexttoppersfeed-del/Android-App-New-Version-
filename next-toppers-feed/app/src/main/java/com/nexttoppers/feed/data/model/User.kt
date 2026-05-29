package com.nexttoppers.feed.data.model

import com.google.firebase.Timestamp

data class User(

    val uid: String = "",

    val name: String = "",

    val email: String = "",

    val photoUrl: String = "",

    val xp: Long = 0L,

    val streak: Int = 0,

    val level: Int = 1,

    val quizzesCompleted: Int = 0,

    val resourcesOpened: Int = 0,

    // Premium fields
    val isPremium: Boolean = false,

    val premiumType: String = "free",

    val premiumStart: Timestamp? = null,

    val premiumEnd: Timestamp? = null,

    val premiumActive: Boolean = false,

    val membershipBadge: String = "",

    val joinedAt: Timestamp = Timestamp.now(),

    val lastSeen: Timestamp = Timestamp.now(),

    val lastActive: Timestamp? = null,

    // Admin fields
    val isAdmin: Boolean = false,

    val banned: Boolean = false
) {

    fun toMap(): Map<String, Any?> {

        return mapOf(

            "uid" to uid,

            "name" to name,

            "email" to email,

            "photoUrl" to photoUrl,

            "xp" to xp,

            "streak" to streak,

            "level" to level,

            "quizzesCompleted" to quizzesCompleted,

            "resourcesOpened" to resourcesOpened,

            "isPremium" to isPremium,

            "premiumType" to premiumType,

            "premiumStart" to premiumStart,

            "premiumEnd" to premiumEnd,

            "premiumActive" to premiumActive,

            "membershipBadge" to membershipBadge,

            "joinedAt" to joinedAt,

            "lastSeen" to lastSeen,

            "lastActive" to (lastActive ?: Timestamp.now()),

            "isAdmin" to isAdmin,

            "banned" to banned
        )
    }
}
