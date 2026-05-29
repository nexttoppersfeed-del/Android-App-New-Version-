package com.nexttoppers.feed.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.nexttoppers.feed.data.model.Announcement
import com.nexttoppers.feed.data.model.Resource
import com.nexttoppers.feed.data.model.User
import com.nexttoppers.feed.data.repository.AdminRepository
import com.nexttoppers.feed.data.repository.AdminStats
import com.nexttoppers.feed.data.repository.AnnouncementsRepository
import com.nexttoppers.feed.data.repository.ResourceManagementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminDashboardUiState(
    val isLoading: Boolean = true,
    val isAdmin: Boolean = false,
    val stats: AdminStats = AdminStats(),
    val recentUsers: List<User> = emptyList(),
    val recentResources: List<Resource> = emptyList(),
    val recentAnnouncements: List<Announcement> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val resourceManagementRepository: ResourceManagementRepository,
    private val announcementsRepository: AnnouncementsRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    init {
        checkAdminAndLoad()
    }

    private fun checkAdminAndLoad() {
        viewModelScope.launch {
            val isAdmin = adminRepository.isCurrentUserAdmin()
            if (!isAdmin) {
                _uiState.value = AdminDashboardUiState(isLoading = false, isAdmin = false)
                return@launch
            }
            _uiState.value = _uiState.value.copy(isAdmin = true)
            loadAll()
        }
    }

    fun refresh() {
        viewModelScope.launch { loadAll() }
    }

    private fun loadAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val statsResult = adminRepository.getAdminStats()
            statsResult.onSuccess { stats ->
                _uiState.value = _uiState.value.copy(stats = stats)
            }
        }

        viewModelScope.launch {
            adminRepository.observeRecentUsers(8).collect { result ->
                result.onSuccess { users ->
                    _uiState.value = _uiState.value.copy(recentUsers = users, isLoading = false)
                }.onFailure { err ->
                    _uiState.value = _uiState.value.copy(
                        error = err.message, isLoading = false
                    )
                }
            }
        }

        viewModelScope.launch {
            resourceManagementRepository.observeAllResources(6).collect { result ->
                result.onSuccess { resources ->
                    _uiState.value = _uiState.value.copy(recentResources = resources)
                }
            }
        }

        viewModelScope.launch {
            announcementsRepository.observeAnnouncements().collect { result ->
                result.onSuccess { announcements ->
                    _uiState.value = _uiState.value.copy(recentAnnouncements = announcements.take(3))
                }
            }
        }
    }
}
