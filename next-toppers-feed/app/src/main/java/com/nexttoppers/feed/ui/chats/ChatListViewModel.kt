package com.nexttoppers.feed.ui.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexttoppers.feed.data.model.Chat
import com.nexttoppers.feed.data.repository.AuthRepository
import com.nexttoppers.feed.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ChatListUiState {
    object Loading : ChatListUiState()
    data class Success(val chats: List<Chat>) : ChatListUiState()
    data class Error(val message: String) : ChatListUiState()
}

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatListUiState>(ChatListUiState.Loading)
    val uiState: StateFlow<ChatListUiState> = _uiState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _allChats = MutableStateFlow<List<Chat>>(emptyList())

    val currentUid: String get() = authRepository.currentUser?.uid ?: ""

    init {
        observeChats()
        observeSearch()
    }

    private fun observeChats() {
        val uid = currentUid
        if (uid.isEmpty()) {
            _uiState.value = ChatListUiState.Success(emptyList())
            return
        }
        viewModelScope.launch {
            chatRepository.observeUserChats(uid).collect { result ->
                result
                    .onSuccess { chats ->
                        _allChats.value = chats
                        applySearch(_searchQuery.value, chats)
                    }
                    .onFailure { err ->
                        _uiState.value = ChatListUiState.Error(err.message ?: "Failed to load chats")
                    }
            }
        }
    }

    private fun observeSearch() {
        viewModelScope.launch {
            _searchQuery.collect { query ->
                applySearch(query, _allChats.value)
            }
        }
    }

    private fun applySearch(query: String, chats: List<Chat>) {
        val filtered = if (query.isBlank()) chats
        else chats.filter { chat ->
            chat.getDisplayName(currentUid).contains(query, ignoreCase = true) ||
                    chat.lastMessage.contains(query, ignoreCase = true)
        }
        _uiState.value = ChatListUiState.Success(filtered)
    }

    fun setSearchQuery(q: String) { _searchQuery.value = q }

    fun getTotalUnread(): Int {
        val uid = currentUid
        return _allChats.value.sumOf { it.getUnreadCount(uid) }
    }
}
