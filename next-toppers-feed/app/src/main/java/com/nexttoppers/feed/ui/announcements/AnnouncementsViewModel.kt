package com.nexttoppers.feed.ui.announcements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexttoppers.feed.data.model.Announcement
import com.nexttoppers.feed.data.repository.AnnouncementsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AnnouncementsUiState {
    object Loading : AnnouncementsUiState()
    object Empty : AnnouncementsUiState()
    data class Success(val items: List<Announcement>) : AnnouncementsUiState()
    data class Error(val message: String) : AnnouncementsUiState()
}

@HiltViewModel
class AnnouncementsViewModel @Inject constructor(
    private val repository: AnnouncementsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AnnouncementsUiState>(AnnouncementsUiState.Loading)
    val uiState: StateFlow<AnnouncementsUiState> = _uiState

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    init {
        observeAnnouncements()
    }

    private fun observeAnnouncements() {
        viewModelScope.launch {
            repository.observeAnnouncements().collect { result ->
                result
                    .onSuccess { items ->
                        _uiState.value = if (items.isEmpty()) AnnouncementsUiState.Empty
                        else AnnouncementsUiState.Success(items)
                    }
                    .onFailure { err ->
                        _uiState.value = AnnouncementsUiState.Error(err.message ?: "Failed to load")
                    }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.refreshAnnouncements()
                .onSuccess { items ->
                    _uiState.value = if (items.isEmpty()) AnnouncementsUiState.Empty
                    else AnnouncementsUiState.Success(items)
                }
                .onFailure { err ->
                    _uiState.value = AnnouncementsUiState.Error(err.message ?: "Refresh failed")
                }
            _isRefreshing.value = false
        }
    }
}
