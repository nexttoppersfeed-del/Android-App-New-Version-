package com.nexttoppers.feed.ui.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexttoppers.feed.data.model.CommunityPost
import com.nexttoppers.feed.data.model.PostType
import com.nexttoppers.feed.data.repository.AuthRepository
import com.nexttoppers.feed.data.repository.CommunityRepository
import com.nexttoppers.feed.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CommunityUiState {
    object Loading : CommunityUiState()
    data class Success(val posts: List<CommunityPost>) : CommunityUiState()
    data class Error(val message: String) : CommunityUiState()
}

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val communityRepository: CommunityRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CommunityUiState>(CommunityUiState.Loading)
    val uiState: StateFlow<CommunityUiState> = _uiState

    private val _selectedFilter = MutableStateFlow<String?>(null)
    val selectedFilter: StateFlow<String?> = _selectedFilter

    private val _likeAnimations = MutableStateFlow<Set<String>>(emptySet())
    val likeAnimations: StateFlow<Set<String>> = _likeAnimations

    private val _messageInput = MutableStateFlow("")
    val messageInput: StateFlow<String> = _messageInput

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending

    val currentUid: String get() = authRepository.currentUser?.uid ?: ""

    private var postsJob: Job? = null

    init {
        observePosts()
    }

    private fun observePosts() {
        postsJob?.cancel()
        postsJob = viewModelScope.launch {
            communityRepository.observePosts(filterType = _selectedFilter.value).collect { result ->
                result
                    .onSuccess { posts -> _uiState.value = CommunityUiState.Success(posts) }
                    .onFailure { err -> _uiState.value = CommunityUiState.Error(err.message ?: "Failed to load") }
            }
        }
    }

    fun setFilter(type: PostType?) {
        _selectedFilter.value = type?.name
        _uiState.value = CommunityUiState.Loading
        observePosts()
    }

    fun setMessageInput(text: String) {
        _messageInput.value = text
    }

    fun sendQuickMessage() {
        val text = _messageInput.value.trim()
        val uid = currentUid
        if (text.isBlank() || uid.isEmpty() || _isSending.value) return
        _isSending.value = true
        _messageInput.value = ""
        viewModelScope.launch {
            try {
                val user = userRepository.getUser(uid).getOrNull()
                val post = CommunityPost(
                    userId    = uid,
                    username  = user?.name?.ifBlank { "Student" } ?: "Student",
                    userPhoto = user?.photoUrl ?: "",
                    type      = PostType.DISCUSSION.name,
                    title     = "",
                    content   = text
                )
                communityRepository.createPost(post)
            } finally {
                _isSending.value = false
            }
        }
    }

    fun toggleLike(post: CommunityPost) {
        val uid = currentUid
        if (uid.isEmpty()) return
        val isLiked = post.isLikedBy(uid)
        viewModelScope.launch {
            _likeAnimations.value = _likeAnimations.value + post.postId
            communityRepository.toggleLike(post.postId, uid, isLiked)
            kotlinx.coroutines.delay(400)
            _likeAnimations.value = _likeAnimations.value - post.postId
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch { communityRepository.deletePost(postId) }
    }

    fun reportPost(postId: String, reason: String = "Inappropriate content") {
        viewModelScope.launch { communityRepository.reportPost(postId, reason) }
    }

    fun refresh() {
        _uiState.value = CommunityUiState.Loading
        observePosts()
    }
}
