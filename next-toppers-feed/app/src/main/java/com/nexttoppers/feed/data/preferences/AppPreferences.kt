package com.nexttoppers.feed.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Typed DataStore wrapper for all user settings in Next Toppers Feed.
 * Provides reactive flows + suspend update functions for each preference.
 */
@Singleton
class AppPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    // ── Notification settings ─────────────────────────────────────────────────

    val pushNotificationsEnabled: Flow<Boolean> = dataStore.data
        .safeCatch()
        .map { it[SettingsKeys.PUSH_NOTIFICATIONS_ENABLED] ?: true }

    val announcementAlertsEnabled: Flow<Boolean> = dataStore.data
        .safeCatch()
        .map { it[SettingsKeys.ANNOUNCEMENT_ALERTS_ENABLED] ?: true }

    val quizRemindersEnabled: Flow<Boolean> = dataStore.data
        .safeCatch()
        .map { it[SettingsKeys.QUIZ_REMINDERS_ENABLED] ?: false }

    val streakRemindersEnabled: Flow<Boolean> = dataStore.data
        .safeCatch()
        .map { it[SettingsKeys.STREAK_REMINDERS_ENABLED] ?: true }

    suspend fun setPushNotifications(enabled: Boolean) {
        dataStore.edit { it[SettingsKeys.PUSH_NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setAnnouncementAlerts(enabled: Boolean) {
        dataStore.edit { it[SettingsKeys.ANNOUNCEMENT_ALERTS_ENABLED] = enabled }
    }

    suspend fun setQuizReminders(enabled: Boolean) {
        dataStore.edit { it[SettingsKeys.QUIZ_REMINDERS_ENABLED] = enabled }
    }

    suspend fun setStreakReminders(enabled: Boolean) {
        dataStore.edit { it[SettingsKeys.STREAK_REMINDERS_ENABLED] = enabled }
    }

    // ── Download settings ─────────────────────────────────────────────────────

    val wifiOnlyDownloads: Flow<Boolean> = dataStore.data
        .safeCatch()
        .map { it[SettingsKeys.WIFI_ONLY_DOWNLOADS] ?: false }

    val autoDeleteAfterDays: Flow<Int> = dataStore.data
        .safeCatch()
        .map { it[SettingsKeys.AUTO_DELETE_AFTER_DAYS] ?: 0 }

    suspend fun setWifiOnlyDownloads(enabled: Boolean) {
        dataStore.edit { it[SettingsKeys.WIFI_ONLY_DOWNLOADS] = enabled }
    }

    suspend fun setAutoDeleteAfterDays(days: Int) {
        dataStore.edit { it[SettingsKeys.AUTO_DELETE_AFTER_DAYS] = days }
    }

    // ── Appearance ────────────────────────────────────────────────────────────

    val themeMode: Flow<String> = dataStore.data
        .safeCatch()
        .map { it[SettingsKeys.THEME_MODE] ?: "system" }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { it[SettingsKeys.THEME_MODE] = mode }
    }

    // ── Privacy ───────────────────────────────────────────────────────────────

    val analyticsEnabled: Flow<Boolean> = dataStore.data
        .safeCatch()
        .map { it[SettingsKeys.ANALYTICS_ENABLED] ?: true }

    suspend fun setAnalyticsEnabled(enabled: Boolean) {
        dataStore.edit { it[SettingsKeys.ANALYTICS_ENABLED] = enabled }
    }

    // ── Cache ─────────────────────────────────────────────────────────────────

    val lastCacheClearMs: Flow<Long> = dataStore.data
        .safeCatch()
        .map { it[SettingsKeys.LAST_CACHE_CLEAR_MS] ?: 0L }

    suspend fun recordCacheClear() {
        dataStore.edit { it[SettingsKeys.LAST_CACHE_CLEAR_MS] = System.currentTimeMillis() }
    }

    // ── Bulk reset ────────────────────────────────────────────────────────────

    suspend fun resetToDefaults() {
        dataStore.edit { prefs ->
            prefs[SettingsKeys.PUSH_NOTIFICATIONS_ENABLED]  = true
            prefs[SettingsKeys.ANNOUNCEMENT_ALERTS_ENABLED] = true
            prefs[SettingsKeys.QUIZ_REMINDERS_ENABLED]      = false
            prefs[SettingsKeys.STREAK_REMINDERS_ENABLED]    = true
            prefs[SettingsKeys.WIFI_ONLY_DOWNLOADS]         = false
            prefs[SettingsKeys.AUTO_DELETE_AFTER_DAYS]      = 0
            prefs[SettingsKeys.THEME_MODE]                  = "dark"
            prefs[SettingsKeys.ANALYTICS_ENABLED]           = true
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun Flow<Preferences>.safeCatch(): Flow<Preferences> =
        catch { e ->
            if (e is IOException) emit(androidx.datastore.preferences.core.emptyPreferences())
            else throw e
        }
}
