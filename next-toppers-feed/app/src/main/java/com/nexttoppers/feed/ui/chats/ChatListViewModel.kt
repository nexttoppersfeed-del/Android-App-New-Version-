package com.nexttoppers.feed.ui.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexttoppers.feed.data.model.Chat
import com.nexttoppers.feed.data.repository.AuthRepository
import com.nexttoppers.feed.data.repository.ChatRepository
import com.nexttoppers.feed.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatListUiState>(ChatListUiState.Loading)
    val uiState: StateFlow<ChatListUiState> = _uiState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _allChats = MutableStateFlow<List<Chat>>(emptyList())

    private val _pendingNavigation = MutableStateFlow<String?>(null)
    val pendingNavigation: StateFlow<String?> = _pendingNavigation

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
                        val enriched = enrichParticipantNames(chats, uid)
                        _allChats.value = enriched
                        applySearch(_searchQuery.value, enriched)
                    }
                    .onFailure { err ->
                        _uiState.value = ChatListUiState.Error(err.message ?: "Failed to load chats")
                    }
            }
        }
    }

    private suspend fun enrichParticipantNames(chats: List<Chat>, myUid: String): List<Chat> {
        return chats.map { chat ->
            val missingUids = chat.participants.filter { uid ->
                uid != myUid && chat.participantNames[uid].isNullOrBlank()
            }
            if (missingUids.isEmpty()) return@map chat

            val fetchedNames  = mutableMapOf<String, String>()
            val fetchedPhotos = mutableMapOf<String, String>()
            for (uid in missingUids) {
                userRepository.getUser(uid).onSuccess { user ->
                    fetchedNames[uid]  = user.name
                    fetchedPhotos[uid] = user.photoUrl
                }
            }

            if (fetchedNames.isEmpty()) return@map chat

            chat.copy(
                participantNames  = chat.participantNames + fetchedNames,
                participantPhotos = chat.participantPhotos + fetchedPhotos
            )
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

    fun openDm(otherUid: String) {
        val myUid = currentUid
        if (myUid.isEmpty() || otherUid.isEmpty() || otherUid == myUid) return
        viewModelScope.launch {
            val myUser    = userRepository.getUser(myUid).getOrNull() ?: return@launch
            val otherUser = userRepository.getUser(otherUid).getOrNull() ?: return@launch
            val chatId = chatRepository.getOrCreatePrivateChat(
                myUid    = myUid,
                myName   = myUser.name,
                myPhoto  = myUser.photoUrl,
                otherUid   = otherUid,
                otherName  = otherUser.name,
                otherPhoto = otherUser.photoUrl
            ).getOrNull() ?: return@launch
            _pendingNavigation.value = chatId
        }
    }

    fun consumeNavigation() {
        _pendingNavigation.value = null
    }
}
