package com.nexttoppers.feed.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import com.nexttoppers.feed.data.preferences.AppPreferences
import com.nexttoppers.feed.util.AppLogger
import com.nexttoppers.feed.util.DownloadSecurity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val cacheCleared: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: AppPreferences
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(SettingsUiState())

    val uiState: StateFlow<SettingsUiState> =
        _uiState.asStateFlow()

    // ─────────────────────────────────────────────────────────
    // Preference Flows
    // ─────────────────────────────────────────────────────────

    val pushNotificationsEnabled =
        prefs.pushNotificationsEnabled.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            true
        )

    val announcementAlertsEnabled =
        prefs.announcementAlertsEnabled.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            true
        )

    val quizRemindersEnabled =
        prefs.quizRemindersEnabled.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            false
        )

    val streakRemindersEnabled =
        prefs.streakRemindersEnabled.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            true
        )

    val wifiOnlyDownloads =
        prefs.wifiOnlyDownloads.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            false
        )

    val analyticsEnabled =
        prefs.analyticsEnabled.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            true
        )

    // ─────────────────────────────────────────────────────────
    // Setters
    // ─────────────────────────────────────────────────────────

    fun setPushNotifications(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setPushNotifications(enabled)
        }
    }

    fun setAnnouncementAlerts(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setAnnouncementAlerts(enabled)
        }
    }

    fun setQuizReminders(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setQuizReminders(enabled)
        }
    }

    fun setStreakReminders(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setStreakReminders(enabled)
        }
    }

    fun setWifiOnlyDownloads(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setWifiOnlyDownloads(enabled)
        }
    }

    fun setAnalyticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setAnalyticsEnabled(enabled)
        }
    }

    // ─────────────────────────────────────────────────────────
    // Clear Cache
    // ─────────────────────────────────────────────────────────

    fun clearCache(context: Context) {

        viewModelScope.launch {

            runCatching {

                // Clear Coil image cache
                val imageLoader =
                    ImageLoader(context)

                imageLoader.diskCache?.clear()

                // Clear downloads
                val deletedFiles =
                    DownloadSecurity.clearAllDownloads(context)

                prefs.recordCacheClear()

                AppLogger.i(
                    "SettingsViewModel",
                    "Cache cleared: $deletedFiles files removed"
                )

                _uiState.value =
                    _uiState.value.copy(
                        cacheCleared = true,
                        successMessage = "✅ Cache cleared successfully"
                    )

            }.onFailure { error ->

                AppLogger.e(
                    "SettingsViewModel",
                    "Cache clear failed",
                    error
                )

                _uiState.value =
                    _uiState.value.copy(
                        errorMessage =
                            "Cache clear failed: ${error.message}"
                    )
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // Reset Settings
    // ─────────────────────────────────────────────────────────

    fun resetToDefaults() {

        viewModelScope.launch {

            prefs.resetToDefaults()

            _uiState.value =
                _uiState.value.copy(
                    successMessage =
                        "Settings reset to defaults"
                )
        }
    }

    fun clearMessages() {

        _uiState.value =
            _uiState.value.copy(
                successMessage = null,
                errorMessage = null
            )
    }
}
