package com.nexttoppers.feed.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexttoppers.feed.data.model.PremiumRequest
import com.nexttoppers.feed.data.model.PremiumRequestStatus
import com.nexttoppers.feed.data.repository.PremiumRequestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PremiumRequestsUiState(
    val isLoading: Boolean = true,
    val requests: List<PremiumRequest> = emptyList(),
    val activeFilter: String = PremiumRequestStatus.PENDING.name,
    val processingId: String? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class PremiumRequestsViewModel @Inject constructor(
    private val repository: PremiumRequestRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PremiumRequestsUiState())
    val uiState: StateFlow<PremiumRequestsUiState> = _uiState.asStateFlow()

    init {
        observeRequests(PremiumRequestStatus.PENDING.name)
    }

    fun setFilter(status: String) {
        _uiState.value = _uiState.value.copy(activeFilter = status, isLoading = true)
        observeRequests(if (status == "ALL") null else status)
    }

    private fun observeRequests(status: String?) {
        viewModelScope.launch {
            repository.observeAllRequests(status).collect { result ->
                result.onSuccess { list ->
                    _uiState.value = _uiState.value.copy(
                        requests = list, isLoading = false
                    )
                }.onFailure { err ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = err.message, isLoading = false
                    )
                }
            }
        }
    }

    fun approveRequest(requestId: String, userId: String, plan: String, durationDays: Int = 30) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(processingId = requestId)
            repository.approveRequest(requestId, userId, plan, durationDays)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        processingId = null,
                        successMessage = "✅ Request approved — premium activated"
                    )
                }
                .onFailure { err ->
                    _uiState.value = _uiState.value.copy(
                        processingId = null,
                        errorMessage = err.message ?: "Approval failed"
                    )
                }
        }
    }

    fun rejectRequest(requestId: String, note: String = "") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(processingId = requestId)
            repository.rejectRequest(requestId, note)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        processingId = null,
                        successMessage = "❌ Request rejected"
                    )
                }
                .onFailure { err ->
                    _uiState.value = _uiState.value.copy(
                        processingId = null,
                        errorMessage = err.message ?: "Rejection failed"
                    )
                }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(successMessage = null, errorMessage = null)
    }
}
