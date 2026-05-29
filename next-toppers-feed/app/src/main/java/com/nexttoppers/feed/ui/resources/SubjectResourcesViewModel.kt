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

enum class SortOrder(val label: String) {
    LATEST("Latest"),
    OLDEST("Oldest"),
    MOST_VIEWED("Most Viewed")
}

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

    private val _premiumFilter = MutableStateFlow<Boolean?>(null)
    val premiumFilter: StateFlow<Boolean?> = _premiumFilter

    private val _sortOrder = MutableStateFlow(SortOrder.LATEST)
    val sortOrder: StateFlow<SortOrder> = _sortOrder

    private var allItems: List<Resource> = emptyList()

    private val _isGridView = MutableStateFlow(false)
    val isGridView: StateFlow<Boolean> = _isGridView

    val hasActiveFilters: Boolean
        get() = _selectedType.value != null ||
                _premiumFilter.value != null ||
                _sortOrder.value != SortOrder.LATEST

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

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
        applyFilters()
    }

    fun clearAllFilters() {
        _selectedType.value = null
        _premiumFilter.value = null
        _sortOrder.value = SortOrder.LATEST
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
        filtered = when (_sortOrder.value) {
            SortOrder.LATEST      -> filtered.sortedByDescending { it.createdAt.seconds }
            SortOrder.OLDEST      -> filtered.sortedBy { it.createdAt.seconds }
            SortOrder.MOST_VIEWED -> filtered.sortedByDescending { it.views }
        }
        _uiState.value = if (filtered.isEmpty()) SubjectResourcesUiState.Empty
        else SubjectResourcesUiState.Success(filtered)
    }
}
