package com.nexttoppers.feed.ui.announcements

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexttoppers.feed.data.model.Announcement
import com.nexttoppers.feed.data.repository.AnnouncementsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AnnouncementDetailUiState {
    object Loading : AnnouncementDetailUiState()
    data class Success(val announcement: Announcement) : AnnouncementDetailUiState()
    data class Error(val message: String) : AnnouncementDetailUiState()
}

@HiltViewModel
class AnnouncementDetailViewModel @Inject constructor(
    private val repository: AnnouncementsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val announcementId: String = savedStateHandle["announcementId"] ?: ""

    private val _uiState = MutableStateFlow<AnnouncementDetailUiState>(AnnouncementDetailUiState.Loading)
    val uiState: StateFlow<AnnouncementDetailUiState> = _uiState

    init {
        loadAnnouncement()
    }

    private fun loadAnnouncement() {
        if (announcementId.isEmpty()) {
            _uiState.value = AnnouncementDetailUiState.Error("Announcement not found")
            return
        }
        viewModelScope.launch {
            repository.getAnnouncementById(announcementId)
                .onSuccess { a ->
                    if (a != null) _uiState.value = AnnouncementDetailUiState.Success(a)
                    else _uiState.value = AnnouncementDetailUiState.Error("Announcement not found")
                }
                .onFailure { err ->
                    _uiState.value = AnnouncementDetailUiState.Error(err.message ?: "Failed to load")
                }
        }
    }
}
