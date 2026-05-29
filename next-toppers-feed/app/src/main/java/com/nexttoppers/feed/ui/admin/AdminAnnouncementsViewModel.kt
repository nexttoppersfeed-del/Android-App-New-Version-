package com.nexttoppers.feed.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexttoppers.feed.data.model.Announcement
import com.nexttoppers.feed.data.repository.AnnouncementsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminAnnouncementsUiState(
    val isLoading: Boolean = true,
    val announcements: List<Announcement> = emptyList(),
    val showCreateDialog: Boolean = false,
    val editingAnnouncement: Announcement? = null,
    val processingId: String? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class AdminAnnouncementsViewModel @Inject constructor(
    private val repository: AnnouncementsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminAnnouncementsUiState())
    val uiState: StateFlow<AdminAnnouncementsUiState> = _uiState.asStateFlow()

    init {
        observeAnnouncements()
    }

    private fun observeAnnouncements() {
        viewModelScope.launch {
            repository.observeAnnouncements().collect { result ->
                result.onSuccess { list ->
                    _uiState.value = _uiState.value.copy(announcements = list, isLoading = false)
                }.onFailure { err ->
                    _uiState.value = _uiState.value.copy(errorMessage = err.message, isLoading = false)
                }
            }
        }
    }

    fun showCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = true, editingAnnouncement = null)
    }

    fun showEditDialog(announcement: Announcement) {
        _uiState.value = _uiState.value.copy(showCreateDialog = true, editingAnnouncement = announcement)
    }

    fun hideDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = false, editingAnnouncement = null)
    }

    fun createAnnouncement(
        title: String, message: String, imageUrl: String,
        pinned: Boolean, important: Boolean, priority: Int
    ) {
        viewModelScope.launch {
            repository.createAnnouncement(title, message, imageUrl, pinned, important, priority)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        showCreateDialog = false,
                        successMessage = "📢 Announcement published"
                    )
                }
                .onFailure { err ->
                    _uiState.value = _uiState.value.copy(errorMessage = err.message ?: "Failed")
                }
        }
    }

    fun updateAnnouncement(
        id: String, title: String, message: String,
        imageUrl: String, pinned: Boolean, important: Boolean, priority: Int
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(processingId = id)
            repository.updateAnnouncement(id, title, message, imageUrl, pinned, important, priority)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        processingId = null, showCreateDialog = false, editingAnnouncement = null,
                        successMessage = "✅ Announcement updated"
                    )
                }
                .onFailure { err ->
                    _uiState.value = _uiState.value.copy(processingId = null, errorMessage = err.message)
                }
        }
    }

    fun deleteAnnouncement(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(processingId = id)
            repository.deleteAnnouncement(id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        processingId = null, successMessage = "🗑 Announcement deleted"
                    )
                }
                .onFailure { err ->
                    _uiState.value = _uiState.value.copy(processingId = null, errorMessage = err.message)
                }
        }
    }

    fun setPinned(id: String, pinned: Boolean) {
        viewModelScope.launch {
            repository.setPinned(id, pinned)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        successMessage = if (pinned) "📌 Announcement pinned" else "Unpinned"
                    )
                }
                .onFailure { err ->
                    _uiState.value = _uiState.value.copy(errorMessage = err.message)
                }
        }
    }

    fun setUrgent(id: String, urgent: Boolean) {
        viewModelScope.launch {
            repository.setUrgent(id, urgent)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        successMessage = if (urgent) "🚨 Marked urgent" else "Urgency removed"
                    )
                }
                .onFailure { err ->
                    _uiState.value = _uiState.value.copy(errorMessage = err.message)
                }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(successMessage = null, errorMessage = null)
    }
}
