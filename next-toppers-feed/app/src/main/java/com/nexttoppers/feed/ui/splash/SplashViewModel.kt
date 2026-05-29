package com.nexttoppers.feed.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexttoppers.feed.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SplashState {
    object Loading : SplashState()
    object Authenticated : SplashState()
    object Unauthenticated : SplashState()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<SplashState>(SplashState.Loading)
    val state: StateFlow<SplashState> = _state

    private val _videoFinished = MutableStateFlow(false)
    val videoFinished: StateFlow<Boolean> = _videoFinished

    private val _authReady = MutableStateFlow(false)
    val authReady: StateFlow<Boolean> = _authReady

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            _state.value = if (authRepository.isLoggedIn) {
                SplashState.Authenticated
            } else {
                SplashState.Unauthenticated
            }
            _authReady.value = true
        }
    }

    fun onVideoFinished() {
        _videoFinished.value = true
    }

    fun onVideoFallback() {
        _videoFinished.value = true
    }
}
