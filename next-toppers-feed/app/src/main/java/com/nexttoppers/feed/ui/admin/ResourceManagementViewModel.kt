package com.nexttoppers.feed.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexttoppers.feed.data.model.Resource
import com.nexttoppers.feed.data.repository.ResourceManagementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResourceManagementUiState(
    val isLoading: Boolean = true,
    val resources: List<Resource> = emptyList(),
    val searchQuery: String = "",
    val subjectFilter: String = "ALL",
    val showAddDialog: Boolean = false,
    val editingResource: Resource? = null,
    val processingId: String? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ResourceManagementViewModel @Inject constructor(
    private val repository: ResourceManagementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResourceManagementUiState())
    val uiState: StateFlow<ResourceManagementUiState> = _uiState.asStateFlow()

    init {
        observeResources()
    }

    private fun observeResources() {
        viewModelScope.launch {
            repository.observeAllResources(100).collect { result ->
                result.onSuccess { list ->
                    _uiState.value = _uiState.value.copy(resources = list, isLoading = false)
                }.onFailure { err ->
                    _uiState.value = _uiState.value.copy(errorMessage = err.message, isLoading = false)
                }
            }
        }
    }

    val filteredResources: List<Resource>
        get() {
            val state = _uiState.value
            return state.resources
                .filter { r ->
                    (state.subjectFilter == "ALL" || r.subject.equals(state.subjectFilter, ignoreCase = true)) &&
                    (state.searchQuery.isBlank() || r.title.contains(state.searchQuery, ignoreCase = true) ||
                     r.subject.contains(state.searchQuery, ignoreCase = true))
                }
        }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setSubjectFilter(subject: String) {
        _uiState.value = _uiState.value.copy(subjectFilter = subject)
    }

    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true, editingResource = null)
    }

    fun showEditDialog(resource: Resource) {
        _uiState.value = _uiState.value.copy(showAddDialog = true, editingResource = resource)
    }

    fun hideDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false, editingResource = null)
    }

    fun createResource(
        title: String, description: String, subject: String,
        type: String, fileUrl: String, thumbnailUrl: String,
        premium: Boolean, tags: List<String>
    ) {
        viewModelScope.launch {
            repository.createResource(title, description, subject, type, fileUrl, thumbnailUrl, premium, tags)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        showAddDialog = false,
                        successMessage = "✅ Resource created successfully"
                    )
                }
                .onFailure { err ->
                    _uiState.value = _uiState.value.copy(errorMessage = err.message ?: "Failed to create")
                }
        }
    }

    fun updateResource(
        id: String, title: String, description: String,
        subject: String, type: String, fileUrl: String,
        premium: Boolean, tags: List<String>
    ) {
        viewModelScope.launch {
            repository.updateResource(id, title, description, subject, type, fileUrl, premium, tags)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        showAddDialog = false, editingResource = null,
                        successMessage = "✅ Resource updated"
                    )
                }
                .onFailure { err ->
                    _uiState.value = _uiState.value.copy(errorMessage = err.message ?: "Update failed")
                }
        }
    }

    fun deleteResource(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(processingId = id)
            repository.deleteResource(id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        processingId = null,
                        successMessage = "🗑 Resource deleted"
                    )
                }
                .onFailure { err ->
                    _uiState.value = _uiState.value.copy(
                        processingId = null,
                        errorMessage = err.message ?: "Delete failed"
                    )
                }
        }
    }

    fun togglePremium(id: String, currentPremium: Boolean) {
        viewModelScope.launch {
            repository.togglePremium(id, currentPremium)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        successMessage = if (!currentPremium) "🔒 Marked as premium" else "🔓 Made free"
                    )
                }
                .onFailure { err ->
                    _uiState.value = _uiState.value.copy(errorMessage = err.message)
                }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(successMessage = null, errorMessage = null)
    }
}
