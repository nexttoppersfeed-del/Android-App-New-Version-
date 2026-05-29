package com.nexttoppers.feed.ui.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexttoppers.feed.data.model.User
import com.nexttoppers.feed.data.repository.AuthRepository
import com.nexttoppers.feed.data.repository.LeaderboardRepository
import com.nexttoppers.feed.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val user: User) : ProfileUiState()
    object SignedOut : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val leaderboardRepository: LeaderboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState

    private val _userRank = MutableStateFlow(0)
    val userRank: StateFlow<Int> = _userRank

    private val uid get() = authRepository.currentUser?.uid ?: ""

    init {
        observeUser()
        loadUserRank()
    }

    private fun observeUser() {
        val uid = authRepository.currentUser?.uid ?: run {
            _uiState.value = ProfileUiState.Error("Not authenticated")
            return
        }
        viewModelScope.launch {
            userRepository.observeUser(uid).collect { result ->
                result
                    .onSuccess { user -> _uiState.value = ProfileUiState.Success(user) }
                    .onFailure { err -> _uiState.value = ProfileUiState.Error(err.message ?: "Failed") }
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

    fun signOut(context: Context) {
        viewModelScope.launch {
            authRepository.signOut(context)
            _uiState.value = ProfileUiState.SignedOut
        }
    }
}
