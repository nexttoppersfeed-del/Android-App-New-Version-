package com.nexttoppers.feed.ui.resources

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexttoppers.feed.data.model.DownloadStatus
import com.nexttoppers.feed.data.model.RecentlyOpened
import com.nexttoppers.feed.data.model.Resource
import com.nexttoppers.feed.data.repository.DownloadRepository
import com.nexttoppers.feed.data.repository.OfflineRepository
import com.nexttoppers.feed.data.repository.ResourcesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ResourceDetailUiState {
    object Loading : ResourceDetailUiState()
    data class Success(val resource: Resource) : ResourceDetailUiState()
    data class Error(val message: String) : ResourceDetailUiState()
}

@HiltViewModel
class ResourceDetailViewModel @Inject constructor(
    private val resourcesRepository: ResourcesRepository,
    private val downloadRepository: DownloadRepository,
    private val offlineRepository: OfflineRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val resourceId: String = savedStateHandle["resourceId"] ?: ""

    private val _uiState = MutableStateFlow<ResourceDetailUiState>(ResourceDetailUiState.Loading)
    val uiState: StateFlow<ResourceDetailUiState> = _uiState

    private val _downloadStatus = MutableStateFlow<DownloadStatus>(DownloadStatus.NotStarted)
    val downloadStatus: StateFlow<DownloadStatus> = _downloadStatus

    private val _localPath = MutableStateFlow<String?>(null)
    val localPath: StateFlow<String?> = _localPath

    val isDownloaded: StateFlow<Boolean> = offlineRepository.isDownloaded(resourceId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        loadResource()
        checkLocalFile()
    }

    private fun loadResource() {
        viewModelScope.launch {
            resourcesRepository.getById(resourceId)
                .onSuccess { resource ->
                    _uiState.value = ResourceDetailUiState.Success(resource)
                    resourcesRepository.incrementViews(resourceId)
                }
                .onFailure { err ->
                    _uiState.value = ResourceDetailUiState.Error(err.message ?: "Not found")
                }
        }
    }

    private fun checkLocalFile() {
        viewModelScope.launch {
            val record = offlineRepository.downloads.first()
                .firstOrNull { it.id == resourceId }
            val path = record?.localPath
            if (path != null && java.io.File(path).exists()) {
                // File is on disk — safe to use
                _localPath.value = path
            } else if (path != null) {
                // DataStore has a record but the file was deleted from disk.
                // Remove the stale entry so the user can re-download cleanly.
                offlineRepository.removeDownload(resourceId)
                _localPath.value = null
            }
        }
    }

    /** Start a download. Emits progress via [downloadStatus]. */
    fun startDownload() {
        val resource = (uiState.value as? ResourceDetailUiState.Success)?.resource ?: return
        if (resource.fileUrl.isBlank()) {
            _downloadStatus.value = DownloadStatus.Failed("No download URL available")
            return
        }
        viewModelScope.launch {
            val downloadId = downloadRepository.startDownload(resource)
            downloadRepository.observeProgress(downloadId, resource).collect { status ->
                _downloadStatus.value = status
                if (status is DownloadStatus.Completed) {
                    _localPath.value = status.localPath
                    // Record in recents
                    offlineRepository.recordOpen(
                        RecentlyOpened(
                            resourceId = resource.id,
                            title      = resource.title,
                            type       = resource.type,
                            subject    = resource.subject,
                            localPath  = status.localPath
                        )
                    )
                }
            }
        }
    }

    fun retry() { loadResource() }

    fun resetDownloadStatus() { _downloadStatus.value = DownloadStatus.NotStarted }
}
