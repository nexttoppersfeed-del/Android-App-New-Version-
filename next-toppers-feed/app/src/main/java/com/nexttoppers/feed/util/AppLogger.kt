package com.nexttoppers.feed.util

import android.util.Log

/**
 * Centralized logging helper for Next Toppers Feed.
 *
 * Debug/verbose logs are stripped by ProGuard in release builds
 * (see proguard-rules.pro assumenosideeffects).
 *
 * Usage:
 *   AppLogger.d("Tag", "message")
 *   AppLogger.e("Tag", "error", exception)
 */
object AppLogger {

    private var isDebug = false
    private const val APP_TAG = "NTF"

    fun init(debugMode: Boolean) {
        isDebug = debugMode
    }

    // ── Public API ────────────────────────────────────────────────────────────

    fun v(tag: String, message: String) {
        if (isDebug) Log.v(appTag(tag), message)
    }

    fun d(tag: String, message: String) {
        if (isDebug) Log.d(appTag(tag), message)
    }

    fun i(tag: String, message: String) {
        Log.i(appTag(tag), message)
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.w(appTag(tag), message, throwable)
        } else {
            Log.w(appTag(tag), message)
        }
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(appTag(tag), message, throwable)
        } else {
            Log.e(appTag(tag), message)
        }
    }

    /** Alias for [e] used in nav-graph and global error handlers */
    fun error(tag: String, message: String, throwable: Throwable? = null) = e(tag, message, throwable)

    fun firestoreRead(collection: String, docCount: Int = 1) {
        d("FS", "READ [$collection] $docCount doc(s)")
    }

    fun firestoreWrite(collection: String) {
        d("FS", "WRITE [$collection]")
    }

    fun uiEvent(screen: String, event: String) {
        d("UI", "[$screen] $event")
    }

    // ── Repository helpers ────────────────────────────────────────────────────

    fun repoSuccess(tag: String, operation: String) {
        d(tag, "✅ $operation succeeded")
    }

    fun repoError(tag: String, operation: String, throwable: Throwable) {
        e(tag, "❌ $operation failed: ${throwable.message}", throwable)
    }

    fun firestoreQuery(tag: String, collection: String, count: Int) {
        d(tag, "📄 Firestore [$collection] returned $count documents")
    }

    fun downloadEvent(tag: String, event: String, resourceId: String) {
        d(tag, "⬇️ Download [$event] resourceId=$resourceId")
    }

    fun navEvent(route: String) {
        d("NavGraph", "🧭 Navigate → $route")
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private fun appTag(tag: String) = "$APP_TAG/$tag"
}
