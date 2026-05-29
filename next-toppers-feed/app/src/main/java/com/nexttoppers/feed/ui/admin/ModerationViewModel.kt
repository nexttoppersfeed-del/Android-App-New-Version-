package com.nexttoppers.feed.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexttoppers.feed.data.repository.ModerationLog
import com.nexttoppers.feed.data.repository.ModerationRepository
import com.nexttoppers.feed.data.repository.Report
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ModerationUiState(
    val isLoading: Boolean = true,
    val reports: List<Report> = emptyList(),
    val logs: List<ModerationLog> = emptyList(),
    val showResolved: Boolean = false,
    val processingId: String? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ModerationViewModel @Inject constructor(
    private val repository: ModerationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ModerationUiState())
    val uiState: StateFlow<ModerationUiState> = _uiState.asStateFlow()

    init {
        observeReports(false)
        observeLogs()
    }

    private fun observeReports(resolved: Boolean) {
        viewModelScope.launch {
            repository.observeReports(resolved).collect { result ->
                result.onSuccess { list ->
                    _uiState.value = _uiState.value.copy(reports = list, isLoading = false)
                }.onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    private fun observeLogs() {
        viewModelScope.launch {
            repository.observeModerationLogs().collect { result ->
                result.onSuccess { list ->
                    _uiState.value = _uiState.value.copy(logs = list)
                }
            }
        }
    }

    fun toggleShowResolved() {
        val show = !_uiState.value.showResolved
        _uiState.value = _uiState.value.copy(showResolved = show, isLoading = true)
        observeReports(show)
    }

    fun resolveReport(reportId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(processingId = reportId)
            repository.resolveReport(reportId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        processingId = null, successMessage = "✅ Report resolved"
                    )
                }
                .onFailure { err ->
                    _uiState.value = _uiState.value.copy(
                        processingId = null, errorMessage = err.message
                    )
                }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            repository.deletePost(postId)
                .onSuccess { _uiState.value = _uiState.value.copy(successMessage = "🗑 Post removed") }
                .onFailure { err -> _uiState.value = _uiState.value.copy(errorMessage = err.message) }
        }
    }

    fun banUser(uid: String, reason: String = "") {
        viewModelScope.launch {
            repository.banUser(uid, reason)
                .onSuccess { _uiState.value = _uiState.value.copy(successMessage = "🔨 User banned") }
                .onFailure { err -> _uiState.value = _uiState.value.copy(errorMessage = err.message) }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(successMessage = null, errorMessage = null)
    }
}
