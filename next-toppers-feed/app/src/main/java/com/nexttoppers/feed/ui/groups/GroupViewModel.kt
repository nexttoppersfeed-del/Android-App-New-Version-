package com.nexttoppers.feed.ui.groups

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexttoppers.feed.data.model.Group
import com.nexttoppers.feed.data.model.defaultGroups
import com.nexttoppers.feed.data.repository.AuthRepository
import com.nexttoppers.feed.data.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class GroupListUiState {
    object Loading : GroupListUiState()
    data class Success(val groups: List<Group>) : GroupListUiState()
    data class Error(val message: String) : GroupListUiState()
}

sealed class GroupDetailUiState {
    object Loading : GroupDetailUiState()
    data class Success(val group: Group) : GroupDetailUiState()
    data class Error(val message: String) : GroupDetailUiState()
}

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val groupId: String = savedStateHandle.get<String>("groupId") ?: ""

    private val _listState = MutableStateFlow<GroupListUiState>(GroupListUiState.Loading)
    val listState: StateFlow<GroupListUiState> = _listState

    private val _detailState = MutableStateFlow<GroupDetailUiState>(GroupDetailUiState.Loading)
    val detailState: StateFlow<GroupDetailUiState> = _detailState

    private val _joinSuccess = MutableStateFlow<String?>(null)
    val joinSuccess: StateFlow<String?> = _joinSuccess

    val currentUid: String get() = authRepository.currentUser?.uid ?: ""

    init {
        observeAllGroups()
        if (groupId.isNotBlank()) observeGroupDetail()
    }

    private fun observeAllGroups() {
        viewModelScope.launch {
            groupRepository.observeAllGroups().collect { result ->
                result
                    .onSuccess { groups -> _listState.value = GroupListUiState.Success(groups) }
                    .onFailure { _listState.value = GroupListUiState.Success(defaultGroups) }
            }
        }
    }

    private fun observeGroupDetail() {
        viewModelScope.launch {
            groupRepository.observeGroup(groupId).collect { result ->
                result
                    .onSuccess { group -> _detailState.value = GroupDetailUiState.Success(group) }
                    .onFailure { err -> _detailState.value = GroupDetailUiState.Error(err.message ?: "Failed") }
            }
        }
    }

    fun joinGroup(gId: String) {
        val uid = currentUid
        if (uid.isEmpty()) return
        viewModelScope.launch {
            groupRepository.joinGroup(gId, uid)
                .onSuccess { _joinSuccess.value = gId }
        }
    }

    fun leaveGroup(gId: String) {
        val uid = currentUid
        if (uid.isEmpty()) return
        viewModelScope.launch { groupRepository.leaveGroup(gId, uid) }
    }

    fun clearJoinSuccess() { _joinSuccess.value = null }
}
