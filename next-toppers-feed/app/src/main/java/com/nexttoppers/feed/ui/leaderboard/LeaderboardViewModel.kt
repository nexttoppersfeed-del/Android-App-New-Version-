package com.nexttoppers.feed.ui.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexttoppers.feed.data.model.LeaderboardEntry
import com.nexttoppers.feed.data.repository.AuthRepository
import com.nexttoppers.feed.data.repository.LeaderboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LeaderboardTab { GLOBAL, WEEKLY, FRIENDS, SUBJECT }

data class LeaderboardUiState(
    val entries: List<LeaderboardEntry> = emptyList(),
    val activeTab: LeaderboardTab = LeaderboardTab.GLOBAL,
    val userRank: Int = 0,
    val currentUid: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val leaderboardRepository: LeaderboardRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState

    private val uid get() = authRepository.currentUser?.uid ?: ""

    init {
        _uiState.value = _uiState.value.copy(currentUid = uid)
        loadUserRank()
        observeGlobal()
    }

    fun selectTab(tab: LeaderboardTab) {
        _uiState.value = _uiState.value.copy(activeTab = tab, isLoading = true)
        when (tab) {
            LeaderboardTab.GLOBAL  -> observeGlobal()
            LeaderboardTab.WEEKLY  -> observeWeekly()
            LeaderboardTab.FRIENDS,
            LeaderboardTab.SUBJECT -> _uiState.value = _uiState.value.copy(
                entries = emptyList(), isLoading = false
            )
        }
    }

    private fun observeGlobal() {
        viewModelScope.launch {
            leaderboardRepository.observeGlobalLeaderboard().collect { result ->
                result
                    .onSuccess { entries ->
                        _uiState.value = _uiState.value.copy(entries = entries, isLoading = false, error = null)
                    }
                    .onFailure { err ->
                        _uiState.value = _uiState.value.copy(isLoading = false, error = err.message)
                    }
            }
        }
    }

    private fun observeWeekly() {
        viewModelScope.launch {
            leaderboardRepository.observeWeeklyLeaderboard().collect { result ->
                result
                    .onSuccess { entries ->
                        _uiState.value = _uiState.value.copy(entries = entries, isLoading = false, error = null)
                    }
                    .onFailure { err ->
                        _uiState.value = _uiState.value.copy(isLoading = false, error = err.message)
                    }
            }
        }
    }

    private fun loadUserRank() {
        if (uid.isEmpty()) return
        viewModelScope.launch {
            leaderboardRepository.getUserRank(uid)
                .onSuccess { rank -> _uiState.value = _uiState.value.copy(userRank = rank) }
        }
    }
}
