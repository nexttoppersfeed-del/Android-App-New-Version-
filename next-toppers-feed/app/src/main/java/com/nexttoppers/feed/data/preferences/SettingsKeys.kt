package com.nexttoppers.feed.data.preferences

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * Type-safe DataStore preference key definitions for Next Toppers Feed.
 * All user-configurable settings live here.
 */
object SettingsKeys {

    // ── Notifications ─────────────────────────────────────────────────────────
    val PUSH_NOTIFICATIONS_ENABLED    = booleanPreferencesKey("push_notifications_enabled")
    val ANNOUNCEMENT_ALERTS_ENABLED   = booleanPreferencesKey("announcement_alerts_enabled")
    val QUIZ_REMINDERS_ENABLED        = booleanPreferencesKey("quiz_reminders_enabled")
    val STREAK_REMINDERS_ENABLED      = booleanPreferencesKey("streak_reminders_enabled")

    // ── Downloads ─────────────────────────────────────────────────────────────
    val WIFI_ONLY_DOWNLOADS           = booleanPreferencesKey("wifi_only_downloads")
    val AUTO_DELETE_AFTER_DAYS        = intPreferencesKey("auto_delete_after_days")   // 0 = never

    // ── Appearance (placeholder — dark mode only for now) ─────────────────────
    val THEME_MODE                    = stringPreferencesKey("theme_mode")             // "dark" | "light" | "system"

    // ── Privacy & Data ────────────────────────────────────────────────────────
    val ANALYTICS_ENABLED             = booleanPreferencesKey("analytics_enabled")
    val CRASH_REPORTING_ENABLED       = booleanPreferencesKey("crash_reporting_enabled")

    // ── Cache ─────────────────────────────────────────────────────────────────
    val LAST_CACHE_CLEAR_MS           = androidx.datastore.preferences.core.longPreferencesKey("last_cache_clear_ms")
}
