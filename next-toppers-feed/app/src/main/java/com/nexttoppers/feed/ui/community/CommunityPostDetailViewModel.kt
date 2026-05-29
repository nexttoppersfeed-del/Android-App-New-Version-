package com.nexttoppers.feed.ui.community

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexttoppers.feed.data.model.Comment
import com.nexttoppers.feed.data.model.CommunityPost
import com.nexttoppers.feed.data.repository.AuthRepository
import com.nexttoppers.feed.data.repository.CommunityRepository
import com.nexttoppers.feed.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityPostDetailViewModel @Inject constructor(
    private val communityRepository: CommunityRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val postId: String = savedStateHandle.get<String>("postId") ?: ""

    private val _post = MutableStateFlow<CommunityPost?>(null)
    val post: StateFlow<CommunityPost?> = _post

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    private val _commentInput = MutableStateFlow("")
    val commentInput: StateFlow<String> = _commentInput

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending

    private val _expandedReplies = MutableStateFlow<Set<String>>(emptySet())
    val expandedReplies: StateFlow<Set<String>> = _expandedReplies

    val currentUid: String get() = authRepository.currentUser?.uid ?: ""

    init {
        if (postId.isNotBlank()) {
            observePost()
            observeComments()
        }
    }

    private fun observePost() {
        viewModelScope.launch {
            communityRepository.observePost(postId).collect { result ->
                result.onSuccess { _post.value = it }
            }
        }
    }

    private fun observeComments() {
        viewModelScope.launch {
            communityRepository.observeComments(postId).collect { result ->
                result.onSuccess { _comments.value = it }
            }
        }
    }

    fun setCommentInput(text: String) { _commentInput.value = text }

    fun submitComment() {
        val uid = currentUid
        val text = _commentInput.value.trim()
        if (uid.isEmpty() || text.isEmpty()) return

        _isSending.value = true
        viewModelScope.launch {
            val user = userRepository.getUser(uid).getOrNull()
            val comment = Comment(
                postId    = postId,
                userId    = uid,
                username  = user?.name ?: "Student",
                userPhoto = user?.photoUrl ?: "",
                content   = text
            )
            communityRepository.addComment(comment)
                .onSuccess { _commentInput.value = "" }
            _isSending.value = false
        }
    }

    fun toggleLikeComment(comment: Comment) {
        val uid = currentUid
        if (uid.isEmpty()) return
        viewModelScope.launch {
            if (comment.isLikedBy(uid)) communityRepository.unlikeComment(comment.commentId, uid)
            else communityRepository.likeComment(comment.commentId, uid)
        }
    }

    fun toggleLikePost() {
        val uid = currentUid
        val post = _post.value ?: return
        viewModelScope.launch {
            communityRepository.toggleLike(post.postId, uid, post.isLikedBy(uid))
        }
    }

    fun toggleRepliesExpanded(commentId: String) {
        _expandedReplies.value = if (_expandedReplies.value.contains(commentId)) {
            _expandedReplies.value - commentId
        } else {
            _expandedReplies.value + commentId
        }
    }

    fun reportPost(reason: String = "Inappropriate content") {
        viewModelScope.launch { communityRepository.reportPost(postId, reason) }
    }
}
