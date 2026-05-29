package com.nexttoppers.feed.util

object LevelUtils {

    private val THRESHOLDS = listOf(
        0L, 200L, 500L, 900L, 1_400L, 2_000L, 2_700L, 3_500L, 4_400L, 5_400L,
        6_500L, 7_700L, 9_000L, 10_400L, 11_900L, 13_500L, 15_200L, 17_000L,
        18_900L, 20_900L
    )

    fun levelForXp(xp: Long): Int {
        val idx = THRESHOLDS.indexOfLast { xp >= it }
        return if (idx < 0) 1 else (idx + 1).coerceAtLeast(1)
    }

    fun xpForLevel(level: Int): Long {
        val idx = (level - 1).coerceIn(0, THRESHOLDS.lastIndex)
        return THRESHOLDS[idx]
    }

    fun xpForNextLevel(level: Int): Long {
        val nextIdx = level.coerceIn(0, THRESHOLDS.lastIndex)
        return THRESHOLDS[nextIdx]
    }

    fun progressToNextLevel(xp: Long): Float {
        val level   = levelForXp(xp)
        val current = xpForLevel(level)
        val next    = xpForNextLevel(level)
        if (next <= current) return 1f
        return ((xp - current).toFloat() / (next - current).toFloat()).coerceIn(0f, 1f)
    }

    fun xpToNextLevel(xp: Long): Long {
        val level = levelForXp(xp)
        return (xpForNextLevel(level) - xp).coerceAtLeast(0L)
    }

    fun levelTitle(level: Int): String = when (level) {
        1    -> "Newcomer"
        2    -> "Learner"
        3    -> "Scholar"
        4    -> "Achiever"
        5    -> "Challenger"
        6    -> "Expert"
        7    -> "Elite"
        8    -> "Master"
        9    -> "Grand Master"
        10   -> "Legend"
        else -> if (level > 10) "Next Topper ⭐" else "Newcomer"
    }

    fun rankBadgeEmoji(rank: Int): String = when (rank) {
        1    -> "🥇"
        2    -> "🥈"
        3    -> "🥉"
        else -> "#$rank"
    }
}
