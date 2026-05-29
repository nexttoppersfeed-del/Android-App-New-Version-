package com.nexttoppers.feed.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexttoppers.feed.data.model.NotificationFilter
import com.nexttoppers.feed.data.model.NotificationGroup
import com.nexttoppers.feed.data.model.NotificationType
import com.nexttoppers.feed.data.model.NtfNotification
import com.nexttoppers.feed.data.repository.AuthRepository
import com.nexttoppers.feed.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class NotificationUiState {
    object Loading : NotificationUiState()
    object Empty   : NotificationUiState()
    data class Success(val groups: List<NotificationGroup>) : NotificationUiState()
    data class Error(val message: String) : NotificationUiState()
}

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificationUiState>(NotificationUiState.Loading)
    val uiState: StateFlow<NotificationUiState> = _uiState

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount

    private val _activeFilter = MutableStateFlow(NotificationFilter.ALL)
    val activeFilter: StateFlow<NotificationFilter> = _activeFilter

    private val _allNotifications = MutableStateFlow<List<NtfNotification>>(emptyList())

    private val uid get() = authRepository.currentUser?.uid ?: ""

    init {
        observeNotifications()
        observeUnreadCount()
    }

    private fun observeNotifications() {
        if (uid.isEmpty()) { _uiState.value = NotificationUiState.Error("Not authenticated"); return }
        viewModelScope.launch {
            notificationRepository.observeNotifications(uid).collect { result ->
                result
                    .onSuccess  { items -> _allNotifications.value = items; applyFilter() }
                    .onFailure  { err   -> _uiState.value = NotificationUiState.Error(err.message ?: "Failed to load") }
            }
        }
    }

    private fun observeUnreadCount() {
        if (uid.isEmpty()) return
        viewModelScope.launch {
            notificationRepository.observeUnreadCount(uid).collect { count ->
                _unreadCount.value = count
            }
        }
    }

    fun setFilter(filter: NotificationFilter) {
        _activeFilter.value = filter
        applyFilter()
    }

    private fun applyFilter() {
        val all     = _allNotifications.value
        val filter  = _activeFilter.value

        val filtered = when (filter) {
            NotificationFilter.ALL           -> all
            NotificationFilter.UNREAD        -> all.filter { !it.read }
            NotificationFilter.XP            -> all.filter { it.notificationType in listOf(NotificationType.XP_EARNED, NotificationType.LEVEL_UP) }
            NotificationFilter.ACHIEVEMENTS  -> all.filter { it.notificationType == NotificationType.ACHIEVEMENT_UNLOCKED }
            NotificationFilter.ANNOUNCEMENTS -> all.filter { it.notificationType == NotificationType.ANNOUNCEMENT }
            NotificationFilter.SYSTEM        -> all.filter { it.notificationType == NotificationType.SYSTEM }
        }

        if (filtered.isEmpty()) { _uiState.value = NotificationUiState.Empty; return }

        // Group into Unread / Today / This Week / Earlier
        val unread   = filtered.filter { !it.read }
        val today    = filtered.filter {  it.read && it.isToday }
        val thisWeek = filtered.filter {  it.read && !it.isToday && it.isThisWeek }
        val earlier  = filtered.filter {  it.read && !it.isThisWeek }

        val groups = buildList {
            if (unread.isNotEmpty())   add(NotificationGroup("Unread (${unread.size})", unread))
            if (today.isNotEmpty())    add(NotificationGroup("Today",     today))
            if (thisWeek.isNotEmpty()) add(NotificationGroup("This Week", thisWeek))
            if (earlier.isNotEmpty())  add(NotificationGroup("Earlier",   earlier))
        }

        _uiState.value = if (groups.isEmpty()) NotificationUiState.Empty
                         else NotificationUiState.Success(groups)
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
        }
    }

    fun markAllAsRead() {
        if (uid.isEmpty()) return
        viewModelScope.launch {
            notificationRepository.markAllAsRead(uid)
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(notificationId)
        }
    }

    fun seedSampleNotifications() {
        if (uid.isEmpty()) return
        viewModelScope.launch {
            notificationRepository.seedSampleNotifications(uid)
        }
    }
}
