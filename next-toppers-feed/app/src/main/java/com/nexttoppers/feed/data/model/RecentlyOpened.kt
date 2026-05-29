package com.nexttoppers.feed.data.model

/**
 * Tracks the last N resources the user opened, persisted in DataStore.
 */
data class RecentlyOpened(
    val resourceId: String,
    val title: String,
    val type: String,
    val subject: String,
    val localPath: String,   // empty if not downloaded (was opened online)
    val openedAt: Long = System.currentTimeMillis()
) {
    fun encode(): String = listOf(resourceId, title, type, subject, localPath, openedAt.toString())
        .joinToString("|§|")

    companion object {
        fun decode(raw: String): RecentlyOpened? = runCatching {
            val p = raw.split("|§|")
            RecentlyOpened(p[0], p[1], p[2], p[3], p[4], p[5].toLong())
        }.getOrNull()
    }
}
