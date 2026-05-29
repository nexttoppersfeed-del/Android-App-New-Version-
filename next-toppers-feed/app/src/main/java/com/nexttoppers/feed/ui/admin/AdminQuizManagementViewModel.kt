package com.nexttoppers.feed.ui.admin

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class AdminQuizUiState(
    val quizCount: Int = 0,
    val premiumQuizCount: Int = 0,
    val isPlaceholder: Boolean = true
)

@HiltViewModel
class AdminQuizManagementViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(AdminQuizUiState())
    val uiState: StateFlow<AdminQuizUiState> = _uiState.asStateFlow()
}
