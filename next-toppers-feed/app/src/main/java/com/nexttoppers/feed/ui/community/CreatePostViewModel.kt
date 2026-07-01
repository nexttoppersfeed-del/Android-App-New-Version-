package com.nexttoppers.feed.ui.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexttoppers.feed.data.model.CommunityPost
import com.nexttoppers.feed.data.model.PostType
import com.nexttoppers.feed.data.repository.AuthRepository
import com.nexttoppers.feed.data.repository.CommunityRepository
import com.nexttoppers.feed.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CreatePostUiState {
    object Idle : CreatePostUiState()
    object Loading : CreatePostUiState()
    data class Success(val postId: String) : CreatePostUiState()
    data class Error(val message: String) : CreatePostUiState()
}

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val communityRepository: CommunityRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreatePostUiState>(CreatePostUiState.Idle)
    val uiState: StateFlow<CreatePostUiState> = _uiState

    val subjects = listOf("General", "Maths", "Science", "SST", "English", "Hindi", "Computer")
    val postTypes = PostType.values().toList()

    fun createPost(
        title: String,
        content: String,
        type: PostType,
        subject: String
    ) {
        val uid = authRepository.currentUser?.uid ?: run {
            _uiState.value = CreatePostUiState.Error("Not authenticated")
            return
        }

        if (title.isBlank() && content.isBlank()) {
            _uiState.value = CreatePostUiState.Error("Post cannot be empty")
            return
        }

        _uiState.value = CreatePostUiState.Loading
        viewModelScope.launch {
            val user = userRepository.getUser(uid).getOrNull()
            val post = CommunityPost(
                userId    = uid,
                username  = user?.name ?: "Student",
                userPhoto = user?.photoURL ?: "",
                type      = type.name,
                title     = title.trim(),
                content   = content.trim(),
                subject   = subject
            )
            communityRepository.createPost(post)
                .onSuccess { id -> _uiState.value = CreatePostUiState.Success(id) }
                .onFailure { err -> _uiState.value = CreatePostUiState.Error(err.message ?: "Failed to post") }
        }
    }

    fun resetState() { _uiState.value = CreatePostUiState.Idle }
}
