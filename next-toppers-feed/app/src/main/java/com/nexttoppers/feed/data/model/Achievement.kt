package com.nexttoppers.feed.data.model

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val xpReward: Int,
    val unlocked: Boolean = false
) {
    companion object {
        fun allAchievements(): List<Achievement> = listOf(
            Achievement("quiz_master",    "Quiz Master",    "Complete 10 quizzes",            "🏆", 100),
            Achievement("streak_7",       "7 Day Streak",   "Maintain a 7-day study streak",  "🔥", 150),
            Achievement("streak_30",      "30 Day Streak",  "Maintain a 30-day study streak", "⚡", 500),
            Achievement("top_10",         "Top 10 Rank",    "Reach top 10 on the leaderboard","👑", 200),
            Achievement("fast_learner",   "Fast Learner",   "Complete a quiz under 2 minutes","⏱", 75),
            Achievement("perfect_score",  "Perfect Score",  "Score 100% on any quiz",         "💯", 250),
            Achievement("resource_buff",  "Resource Buff",  "Open 20 study resources",        "📚", 100),
            Achievement("level_5",        "Level 5 Grind",  "Reach Level 5",                  "🎯", 300),
            Achievement("level_10",       "Legend Status",  "Reach Level 10",                 "🌟", 1000),
            Achievement("first_quiz",     "First Step",     "Complete your first quiz",       "🚀", 25)
        )
    }
}
