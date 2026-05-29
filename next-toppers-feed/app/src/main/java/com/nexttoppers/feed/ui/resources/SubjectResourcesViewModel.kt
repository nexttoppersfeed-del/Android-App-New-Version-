package com.nexttoppers.feed.ui.resources

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexttoppers.feed.data.model.Resource
import com.nexttoppers.feed.data.model.ResourceType
import com.nexttoppers.feed.data.repository.ResourcesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SubjectResourcesUiState {
    object Loading : SubjectResourcesUiState()
    object Empty : SubjectResourcesUiState()
    data class Success(val items: List<Resource>) : SubjectResourcesUiState()
    data class Error(val message: String) : SubjectResourcesUiState()
}

@HiltViewModel
class SubjectResourcesViewModel @Inject constructor(
    private val repository: ResourcesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val subject: String = savedStateHandle.get<String>("subject") ?: ""

    private val _uiState = MutableStateFlow<SubjectResourcesUiState>(SubjectResourcesUiState.Loading)
    val uiState: StateFlow<SubjectResourcesUiState> = _uiState

    private val _selectedType = MutableStateFlow<ResourceType?>(null)
    val selectedType: StateFlow<ResourceType?> = _selectedType

    private val _premiumFilter = MutableStateFlow<Boolean?>(null) // null = all, true = premium only, false = free
    val premiumFilter: StateFlow<Boolean?> = _premiumFilter

    private var allItems: List<Resource> = emptyList()

    private val _isGridView = MutableStateFlow(false)
    val isGridView: StateFlow<Boolean> = _isGridView

    init {
        observeResources()
    }

    private fun observeResources() {
        viewModelScope.launch {
            repository.observeBySubject(subject).collect { result ->
                result
                    .onSuccess { items ->
                        allItems = items
                        applyFilters()
                    }
                    .onFailure { err ->
                        _uiState.value = SubjectResourcesUiState.Error(err.message ?: "Failed")
                    }
            }
        }
    }

    fun selectType(type: ResourceType?) {
        _selectedType.value = type
        applyFilters()
    }

    fun setPremiumFilter(premium: Boolean?) {
        _premiumFilter.value = premium
        applyFilters()
    }

    fun toggleViewMode() {
        _isGridView.value = !_isGridView.value
    }

    private fun applyFilters() {
        var filtered = allItems
        _selectedType.value?.let { type ->
            filtered = filtered.filter { it.type.equals(type.name, ignoreCase = true) }
        }
        _premiumFilter.value?.let { isPremium ->
            filtered = filtered.filter { it.premium == isPremium }
        }
        _uiState.value = if (filtered.isEmpty()) SubjectResourcesUiState.Empty
        else SubjectResourcesUiState.Success(filtered)
    }
}
