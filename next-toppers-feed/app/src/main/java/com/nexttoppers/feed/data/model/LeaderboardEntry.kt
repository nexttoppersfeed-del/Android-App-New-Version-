package com.nexttoppers.feed.data.model

data class LeaderboardEntry(
    val uid: String = "",
    val name: String = "",
    val photoUrl: String = "",
    val xp: Long = 0L,
    val streak: Int = 0,
    val level: Int = 1,
    val premium: Boolean = false,
    val rank: Int = 0
)
