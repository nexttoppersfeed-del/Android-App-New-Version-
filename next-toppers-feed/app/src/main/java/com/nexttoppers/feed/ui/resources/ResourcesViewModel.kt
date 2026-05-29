package com.nexttoppers.feed.ui.resources

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexttoppers.feed.data.model.Resource
import com.nexttoppers.feed.data.model.ResourceSubject
import com.nexttoppers.feed.data.model.ResourceType
import com.nexttoppers.feed.data.repository.ResourcesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubjectCount(val subject: ResourceSubject, val count: Int)

sealed class ResourcesUiState {
    object Loading : ResourcesUiState()
    data class Success(
        val subjectCounts: List<SubjectCount>,
        val recent: List<Resource>
    ) : ResourcesUiState()
    data class Error(val message: String) : ResourcesUiState()
}

// Search state for the global search overlay
sealed class SearchState {
    object Idle : SearchState()
    object Searching : SearchState()
    data class Results(val items: List<Resource>) : SearchState()
    object Empty : SearchState()
}

@OptIn(FlowPreview::class)
@HiltViewModel
class ResourcesViewModel @Inject constructor(
    private val repository: ResourcesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ResourcesUiState>(ResourcesUiState.Loading)
    val uiState: StateFlow<ResourcesUiState> = _uiState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchState: StateFlow<SearchState> = _searchState

    private var allResources: List<Resource> = emptyList()

    init {
        loadDashboard()
        observeAllForSearch()
        setupSearchDebounce()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            try {
                val counts = repository.getCountBySubject()
                val recent = repository.getRecent(8).getOrDefault(emptyList())
                val subjectCounts = ResourceSubject.values().map { subject ->
                    SubjectCount(subject, counts[subject.name] ?: 0)
                }
                _uiState.value = ResourcesUiState.Success(subjectCounts, recent)
            } catch (e: Exception) {
                _uiState.value = ResourcesUiState.Error(e.message ?: "Failed to load")
            }
        }
    }

    private fun observeAllForSearch() {
        viewModelScope.launch {
            repository.observeAll().collect { result ->
                result.onSuccess { items -> allResources = items }
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun setupSearchDebounce() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isBlank()) {
                        _searchState.value = SearchState.Idle
                    } else {
                        _searchState.value = SearchState.Searching
                        val results = allResources.filter { r ->
                            r.title.contains(query, ignoreCase = true) ||
                            r.subject.contains(query, ignoreCase = true) ||
                            r.type.contains(query, ignoreCase = true) ||
                            r.description.contains(query, ignoreCase = true) ||
                            r.tags.any { it.contains(query, ignoreCase = true) }
                        }
                        _searchState.value = if (results.isEmpty()) SearchState.Empty
                        else SearchState.Results(results)
                    }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchState.value = SearchState.Idle
    }

    fun refresh() { loadDashboard() }
}
