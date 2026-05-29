package com.nexttoppers.feed.ui.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexttoppers.feed.data.model.DownloadedResource
import com.nexttoppers.feed.data.model.RecentlyOpened
import com.nexttoppers.feed.data.repository.OfflineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val offlineRepository: OfflineRepository
) : ViewModel() {

    val downloads: StateFlow<List<DownloadedResource>> = offlineRepository.downloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recents: StateFlow<List<RecentlyOpened>> = offlineRepository.recents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _deleteSuccess = MutableStateFlow<String?>(null)
    val deleteSuccess: StateFlow<String?> = _deleteSuccess

    /** Filter query for the downloads list */
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    fun onSearchQueryChange(q: String) { _searchQuery.value = q }

    val filteredDownloads: StateFlow<List<DownloadedResource>> get() = downloads

    fun deleteDownload(resource: DownloadedResource) {
        viewModelScope.launch {
            // Delete local file
            runCatching { File(resource.localPath).delete() }
            // Remove from DataStore
            offlineRepository.removeDownload(resource.id)
            _deleteSuccess.value = "\"${resource.title}\" removed"
        }
    }

    fun clearDeleteSuccess() { _deleteSuccess.value = null }

    fun clearAllDownloads() {
        viewModelScope.launch {
            downloads.value.forEach { resource ->
                runCatching { File(resource.localPath).delete() }
            }
            offlineRepository.clearDownloads()
        }
    }

    fun clearRecents() {
        viewModelScope.launch { offlineRepository.clearRecents() }
    }

    /** Returns storage used by downloads in human-readable form */
    fun totalStorageUsed(): String {
        val bytes = downloads.value.sumOf { it.sizeBytes }
        return when {
            bytes < 1024L              -> "$bytes B"
            bytes < 1024L * 1024L      -> "${bytes / 1024} KB"
            bytes < 1024L * 1024L * 1024L -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
            else                       -> "%.2f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }
}
