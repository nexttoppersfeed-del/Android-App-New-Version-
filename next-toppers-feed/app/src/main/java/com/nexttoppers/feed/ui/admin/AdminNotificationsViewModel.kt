package com.nexttoppers.feed.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexttoppers.feed.data.model.AdminNotification
import com.nexttoppers.feed.data.model.NotificationTarget
import com.nexttoppers.feed.data.model.NotificationType
import com.nexttoppers.feed.data.model.User
import com.nexttoppers.feed.data.repository.AdminRepository
import com.nexttoppers.feed.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminNotifUiState(
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val history: List<AdminNotification> = emptyList(),
    val allUsers: List<User> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class AdminNotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminNotifUiState())
    val uiState: StateFlow<AdminNotifUiState> = _uiState.asStateFlow()

    init {
        observeHistory()
        loadAllUsers()
    }

    private fun observeHistory() {
        viewModelScope.launch {
            notificationRepository.observeAdminNotificationHistory().collect { result ->
                result
                    .onSuccess { list ->
                        _uiState.value = _uiState.value.copy(history = list, isLoading = false)
                    }
                    .onFailure { err ->
                        _uiState.value = _uiState.value.copy(
                            error = err.message, isLoading = false
                        )
                    }
            }
        }
    }

    private fun loadAllUsers() {
        viewModelScope.launch {
            adminRepository.observeAllUsers(200).collect { result ->
                result.onSuccess { users ->
                    _uiState.value = _uiState.value.copy(allUsers = users)
                }
            }
        }
    }

    fun sendNotification(
        target: NotificationTarget,
        title: String,
        message: String,
        type: NotificationType,
        imageUrl: String = "",
        deepLink: String = "",
        selectedUserIds: List<String> = emptyList(),
        topic: String = ""
    ) {
        if (title.isBlank() || message.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Title and message are required.")
            return
        }
        _uiState.value = _uiState.value.copy(isSending = true, error = null, successMessage = null)
        viewModelScope.launch {
            val result = when (target) {
                NotificationTarget.ALL ->
                    notificationRepository.sendToAll(title, message, type, deepLink, imageUrl)
                NotificationTarget.PREMIUM_ONLY ->
                    notificationRepository.sendToPremium(title, message, type, deepLink, imageUrl)
                NotificationTarget.SELECTED ->
                    notificationRepository.sendToSelected(selectedUserIds, title, message, type, deepLink, imageUrl)
                NotificationTarget.TOPIC ->
                    notificationRepository.sendByTopic(topic, title, message, type, deepLink, imageUrl)
            }
            result
                .onSuccess { count ->
                    val msg = when (target) {
                        NotificationTarget.TOPIC    -> "Notification queued for topic '$topic'."
                        NotificationTarget.SELECTED -> "Notification sent to ${selectedUserIds.size} users."
                        else                        -> "Notification sent to $count users."
                    }
                    _uiState.value = _uiState.value.copy(isSending = false, successMessage = msg)
                }
                .onFailure { err ->
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        error = err.message ?: "Failed to send notification."
                    )
                }
        }
    }

    fun deleteHistory(historyId: String) {
        viewModelScope.launch {
            notificationRepository.deleteAdminNotification(historyId)
        }
    }

    fun resend(adminNotif: AdminNotification) {
        _uiState.value = _uiState.value.copy(isSending = true, error = null, successMessage = null)
        viewModelScope.launch {
            notificationRepository.resendAdminNotification(adminNotif)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        successMessage = "Notification resent successfully."
                    )
                }
                .onFailure { err ->
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        error = err.message ?: "Resend failed."
                    )
                }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
