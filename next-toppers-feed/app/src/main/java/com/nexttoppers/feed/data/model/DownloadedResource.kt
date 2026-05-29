package com.nexttoppers.feed.data.model

/**
 * Represents a resource that has been fully downloaded to local storage.
 * Persisted in DataStore as pipe-delimited strings for simplicity.
 */
data class DownloadedResource(
    val id: String,
    val title: String,
    val subject: String,
    val type: String,
    val localPath: String,
    val sizeBytes: Long,
    val downloadedAt: Long = System.currentTimeMillis(),
    val premium: Boolean = false
) {
    /** Human-readable file size, e.g. "4.2 MB" */
    val sizeLabel: String get() = when {
        sizeBytes < 1024L              -> "$sizeBytes B"
        sizeBytes < 1024L * 1024L      -> "${sizeBytes / 1024} KB"
        sizeBytes < 1024L * 1024L * 1024L -> "%.1f MB".format(sizeBytes / (1024.0 * 1024.0))
        else                           -> "%.2f GB".format(sizeBytes / (1024.0 * 1024.0 * 1024.0))
    }

    fun encode(): String = listOf(id, title, subject, type, localPath,
        sizeBytes.toString(), downloadedAt.toString(), premium.toString())
        .joinToString("|§|")

    companion object {
        fun decode(raw: String): DownloadedResource? = runCatching {
            val p = raw.split("|§|")
            DownloadedResource(p[0], p[1], p[2], p[3], p[4],
                p[5].toLong(), p[6].toLong(), p[7].toBoolean())
        }.getOrNull()
    }
}

sealed class DownloadStatus {
    object Queued    : DownloadStatus()
    data class Progress(val percent: Int) : DownloadStatus()
    data class Completed(val localPath: String) : DownloadStatus()
    object Paused    : DownloadStatus()
    data class Failed(val reason: String = "Unknown error") : DownloadStatus()
    object NotStarted : DownloadStatus()
}
