package com.nexttoppers.feed.ui.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexttoppers.feed.data.model.ActivityFeedItem
import com.nexttoppers.feed.data.model.FeedTab
import com.nexttoppers.feed.data.repository.ActivityFeedRepository
import com.nexttoppers.feed.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ActivityFeedUiState {
    object Loading : ActivityFeedUiState()
    object Empty   : ActivityFeedUiState()
    data class Success(val items: List<ActivityFeedItem>) : ActivityFeedUiState()
    data class Error(val message: String) : ActivityFeedUiState()
}

@HiltViewModel
class ActivityFeedViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val activityFeedRepository: ActivityFeedRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ActivityFeedUiState>(ActivityFeedUiState.Loading)
    val uiState: StateFlow<ActivityFeedUiState> = _uiState

    private val _activeTab = MutableStateFlow(FeedTab.PERSONAL)
    val activeTab: StateFlow<FeedTab> = _activeTab

    private val uid get() = authRepository.currentUser?.uid ?: ""

    init {
        observeFeed(FeedTab.PERSONAL)
    }

    fun selectTab(tab: FeedTab) {
        _activeTab.value = tab
        observeFeed(tab)
    }

    private fun observeFeed(tab: FeedTab) {
        viewModelScope.launch {
            _uiState.value = ActivityFeedUiState.Loading
            when (tab) {
                FeedTab.PERSONAL -> {
                    if (uid.isEmpty()) {
                        _uiState.value = ActivityFeedUiState.Error("Not authenticated")
                        return@launch
                    }
                    activityFeedRepository.observePersonalFeed(uid).collect { result ->
                        result
                            .onSuccess { items -> _uiState.value = if (items.isEmpty()) ActivityFeedUiState.Empty else ActivityFeedUiState.Success(items) }
                            .onFailure { err   -> _uiState.value = ActivityFeedUiState.Error(err.message ?: "Failed to load") }
                    }
                }
                FeedTab.GLOBAL -> {
                    activityFeedRepository.observeGlobalFeed().collect { result ->
                        result
                            .onSuccess { items -> _uiState.value = if (items.isEmpty()) ActivityFeedUiState.Empty else ActivityFeedUiState.Success(items) }
                            .onFailure { err   -> _uiState.value = ActivityFeedUiState.Error(err.message ?: "Failed to load") }
                    }
                }
                FeedTab.FRIENDS -> {
                    // Friends feed — placeholder; shows empty state with a coming-soon message
                    _uiState.value = ActivityFeedUiState.Empty
                }
            }
        }
    }
}
