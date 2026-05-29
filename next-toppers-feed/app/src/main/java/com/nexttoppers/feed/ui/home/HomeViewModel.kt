package com.nexttoppers.feed.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexttoppers.feed.data.model.LeaderboardEntry
import com.nexttoppers.feed.data.model.User
import com.nexttoppers.feed.data.repository.AuthRepository
import com.nexttoppers.feed.data.repository.LeaderboardRepository
import com.nexttoppers.feed.data.repository.NotificationRepository
import com.nexttoppers.feed.data.repository.UserRepository
import com.nexttoppers.feed.data.repository.XpRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val user: User) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val xpRepository: XpRepository,
    private val leaderboardRepository: LeaderboardRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    private val _userRank = MutableStateFlow(0)
    val userRank: StateFlow<Int> = _userRank

    private val _topEntries = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val topEntries: StateFlow<List<LeaderboardEntry>> = _topEntries

    private val _levelUpXp = MutableStateFlow<Int?>(null)
    val levelUpXp: StateFlow<Int?> = _levelUpXp

    private val _unreadNotificationCount = MutableStateFlow(0)
    val unreadNotificationCount: StateFlow<Int> = _unreadNotificationCount

    private val uid get() = authRepository.currentUser?.uid ?: ""

    init {
        observeUser()
        loadUserRank()
        observeTopEntries()
        triggerDailyLogin()
        observeUnreadCount()
    }

    private fun observeUnreadCount() {
        if (uid.isEmpty()) return
        viewModelScope.launch {
            notificationRepository.observeUnreadCount(uid).collect { count ->
                _unreadNotificationCount.value = count
            }
        }
    }

    private fun observeUser() {
        val uid = authRepository.currentUser?.uid ?: run {
            _uiState.value = HomeUiState.Error("Not authenticated")
            return
        }
        viewModelScope.launch {
            userRepository.observeUser(uid).collect { result ->
                result
                    .onSuccess { user -> _uiState.value = HomeUiState.Success(user) }
                    .onFailure { err -> _uiState.value = HomeUiState.Error(err.message ?: "Failed to load user") }
            }
        }
    }

    private fun loadUserRank() {
        if (uid.isEmpty()) return
        viewModelScope.launch {
            leaderboardRepository.getUserRank(uid)
                .onSuccess { rank -> _userRank.value = rank }
        }
    }

    private fun observeTopEntries() {
        viewModelScope.launch {
            leaderboardRepository.observeGlobalLeaderboard(limit = 3).collect { result ->
                result.onSuccess { entries -> _topEntries.value = entries }
            }
        }
    }

    private fun triggerDailyLogin() {
        if (uid.isEmpty()) return
        viewModelScope.launch {
            xpRepository.awardDailyLoginXp(uid)
        }
    }

    fun dismissLevelUp() {
        _levelUpXp.value = null
    }
}
