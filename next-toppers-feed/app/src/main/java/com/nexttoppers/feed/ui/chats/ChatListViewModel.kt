package com.nexttoppers.feed.ui.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexttoppers.feed.data.model.Chat
import com.nexttoppers.feed.data.model.User
import com.nexttoppers.feed.data.repository.AuthRepository
import com.nexttoppers.feed.data.repository.ChatRepository
import com.nexttoppers.feed.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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

    private val _totalUnread = MutableStateFlow(0)
    val totalUnread: StateFlow<Int> = _totalUnread

    private val _pendingNavigation = MutableStateFlow<String?>(null)
    val pendingNavigation: StateFlow<String?> = _pendingNavigation

    private val _isFirstLoad = MutableStateFlow(true)
    val isFirstLoad: StateFlow<Boolean> = _isFirstLoad

    private val _userSearchResults = MutableStateFlow<List<User>>(emptyList())
    val userSearchResults: StateFlow<List<User>> = _userSearchResults

    private val _isSearchingUsers = MutableStateFlow(false)
    val isSearchingUsers: StateFlow<Boolean> = _isSearchingUsers

    val currentUid: String get() = authRepository.currentUser?.uid ?: ""

    init {
        observeChats()
        observeSearch()
    }

    private fun observeChats() {
        val uid = currentUid
        if (uid.isEmpty()) {
            _isFirstLoad.value = false
            _uiState.value = ChatListUiState.Success(emptyList())
            return
        }
        viewModelScope.launch {
            chatRepository.observeUserChats(uid).collect { result ->
                result
                    .onSuccess { chats ->
                        // Show chats immediately with whatever names Firestore has —
                        // this makes the list appear in <1s from the first snapshot.
                        _isFirstLoad.value = false
                        _allChats.value = chats
                        applySearch(_searchQuery.value, chats)

                        // Enrich any missing participant names in parallel, then
                        // re-publish only if something actually changed.
                        val enriched = enrichParticipantNames(chats, uid)
                        if (enriched != chats) {
                            _allChats.value = enriched
                            applySearch(_searchQuery.value, enriched)
                        }
                    }
                    .onFailure { err ->
                        _isFirstLoad.value = false
                        _uiState.value = ChatListUiState.Error(err.message ?: "Failed to load chats")
                    }
            }
        }
    }

    private suspend fun enrichParticipantNames(chats: List<Chat>, myUid: String): List<Chat> =
        coroutineScope {
            chats.map { chat ->
                async {
                    val missingUids = chat.participants.filter { uid ->
                        uid != myUid && chat.participantNames[uid].isNullOrBlank()
                    }
                    if (missingUids.isEmpty()) return@async chat

                    // Fetch all missing users in parallel — one concurrent request per user
                    // instead of the previous sequential for-loop.
                    val fetchedNames  = mutableMapOf<String, String>()
                    val fetchedPhotos = mutableMapOf<String, String>()
                    missingUids
                        .map { uid -> async { userRepository.getUser(uid).getOrNull()?.let { uid to it } } }
                        .awaitAll()
                        .filterNotNull()
                        .forEach { (uid, user) ->
                            fetchedNames[uid]  = user.name
                            fetchedPhotos[uid] = user.photoURL
                        }

                    if (fetchedNames.isEmpty()) return@async chat
                    chat.copy(
                        participantNames  = chat.participantNames + fetchedNames,
                        participantPhotos = chat.participantPhotos + fetchedPhotos
                    )
                }
            }.awaitAll()
        }

    private fun observeSearch() {
        viewModelScope.launch {
            _searchQuery.collect { query ->
                applySearch(query, _allChats.value)
            }
        }
    }

    private fun applySearch(query: String, chats: List<Chat>) {
        val uid = currentUid
        _totalUnread.value = chats.sumOf { it.getUnreadCount(uid) }
        val filtered = if (query.isBlank()) chats
        else chats.filter { chat ->
            chat.getDisplayName(uid).contains(query, ignoreCase = true) ||
                    chat.lastMessage.contains(query, ignoreCase = true)
        }
        _uiState.value = ChatListUiState.Success(filtered)
    }

    fun setSearchQuery(q: String) { _searchQuery.value = q }

    fun getTotalUnread(): Int {
        val uid = currentUid
        return _allChats.value.sumOf { it.getUnreadCount(uid) }
    }

    fun searchUsers(query: String) {
        if (query.length < 2) {
            _userSearchResults.value = emptyList()
            return
        }
        _isSearchingUsers.value = true
        viewModelScope.launch {
            userRepository.searchUsers(query)
                .onSuccess { users -> _userSearchResults.value = users.filter { it.uid != currentUid } }
                .onFailure   { _userSearchResults.value = emptyList() }
            _isSearchingUsers.value = false
        }
    }

    fun clearUserSearch() { _userSearchResults.value = emptyList() }

    fun openDm(otherUid: String) {
        val myUid = currentUid
        if (myUid.isEmpty() || otherUid.isEmpty() || otherUid == myUid) return
        viewModelScope.launch {
            val myUser    = userRepository.getUser(myUid).getOrNull() ?: return@launch
            val otherUser = userRepository.getUser(otherUid).getOrNull() ?: return@launch
            val chatId = chatRepository.getOrCreatePrivateChat(
                myUid      = myUid,
                myName     = myUser.name,
                myPhoto    = myUser.photoURL,
                otherUid   = otherUid,
                otherName  = otherUser.name,
                otherPhoto = otherUser.photoURL
            ).getOrNull() ?: return@launch
            _pendingNavigation.value = chatId
        }
    }

    fun consumeNavigation() { _pendingNavigation.value = null }
}
