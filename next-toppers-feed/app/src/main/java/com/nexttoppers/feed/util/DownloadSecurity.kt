package com.nexttoppers.feed.util

import android.content.Context
import java.io.File
import java.security.MessageDigest

/**
 * Download security utilities for Next Toppers Feed.
 *
 * Responsibilities:
 * - Generate secure, non-guessable local file names
 * - Validate downloaded files
 * - Store downloads in app-private internal storage (not accessible to other apps)
 * - Detect duplicate downloads
 * - Handle corrupted files
 *
 * NOTE: Full DRM is NOT implemented here (scoped for a future prompt).
 *       This provides the storage + naming security layer only.
 */
object DownloadSecurity {

    private const val DOWNLOADS_DIR = "ntf_downloads"

    // ── Secure file naming ────────────────────────────────────────────────────

    /**
     * Generate a secure local file name from a resource ID.
     * Uses SHA-256 to avoid exposing resource IDs in the filesystem.
     */
    fun secureFileName(resourceId: String, extension: String = "pdf"): String {
        val hash = sha256(resourceId).take(16)
        return "ntf_${hash}.$extension"
    }

    /**
     * Get the app-private downloads directory.
     * Files here are NOT accessible by other apps (no READ_EXTERNAL_STORAGE required).
     */
    fun getDownloadsDir(context: Context): File {
        val dir = File(context.filesDir, DOWNLOADS_DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /**
     * Full local path for a downloaded resource.
     */
    fun localPath(context: Context, resourceId: String, extension: String = "pdf"): String {
        val dir = getDownloadsDir(context)
        return File(dir, secureFileName(resourceId, extension)).absolutePath
    }

    // ── Duplicate detection ───────────────────────────────────────────────────

    /**
     * Returns true if a file for this resourceId already exists and has content.
     */
    fun isAlreadyDownloaded(context: Context, resourceId: String, extension: String = "pdf"): Boolean {
        val file = File(localPath(context, resourceId, extension))
        return file.exists() && file.length() > 0L
    }

    // ── File validation ───────────────────────────────────────────────────────

    /**
     * Basic validation: check the file exists and has a reasonable size.
     * A file under 1 KB for a PDF is almost certainly corrupted.
     */
    fun isFileValid(path: String, minSizeBytes: Long = 1024L): Boolean {
        val file = File(path)
        return file.exists() && file.isFile && file.length() >= minSizeBytes
    }

    /**
     * Delete a corrupted or zero-length file.
     * Returns true if deletion succeeded.
     */
    fun deleteCorruptedFile(path: String): Boolean {
        return runCatching { File(path).delete() }.getOrDefault(false)
    }

    // ── Storage cleanup ───────────────────────────────────────────────────────

    /**
     * Calculate total bytes used by all downloads in the private directory.
     */
    fun totalDownloadBytes(context: Context): Long {
        val dir = getDownloadsDir(context)
        return dir.listFiles()?.sumOf { it.length() } ?: 0L
    }

    /**
     * Human-readable storage size.
     */
    fun formattedStorageSize(bytes: Long): String = when {
        bytes < 1024L                  -> "$bytes B"
        bytes < 1024L * 1024L          -> "${bytes / 1024} KB"
        bytes < 1024L * 1024L * 1024L  -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
        else                           -> "%.2f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
    }

    /**
     * Delete all downloaded files and return the count deleted.
     */
    fun clearAllDownloads(context: Context): Int {
        val dir = getDownloadsDir(context)
        val files = dir.listFiles() ?: return 0
        var count = 0
        files.forEach { file ->
            if (file.delete()) count++
        }
        return count
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
