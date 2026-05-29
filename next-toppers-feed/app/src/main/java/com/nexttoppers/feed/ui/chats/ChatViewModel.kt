package com.nexttoppers.feed.ui.chats

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexttoppers.feed.data.model.Chat
import com.nexttoppers.feed.data.model.ChatMessage
import com.nexttoppers.feed.data.model.MessageType
import com.nexttoppers.feed.data.repository.AuthRepository
import com.nexttoppers.feed.data.repository.ChatRepository
import com.nexttoppers.feed.data.repository.UserRepository
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

sealed class ChatUiState {
    object Loading : ChatUiState()
    data class Success(val chat: Chat, val messages: List<ChatMessage>) : ChatUiState()
    data class Error(val message: String) : ChatUiState()
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val chatId: String = savedStateHandle.get<String>("chatId") ?: ""

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Loading)
    val uiState: StateFlow<ChatUiState> = _uiState

    private val _messageInput = MutableStateFlow("")
    val messageInput: StateFlow<String> = _messageInput

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending

    private val _currentChat = MutableStateFlow<Chat?>(null)
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())

    val currentUid: String get() = authRepository.currentUser?.uid ?: ""

    init {
        if (chatId.isNotBlank()) {
            observeChat()
            observeMessages()
            markSeen()
        }
    }

    private fun observeChat() {
        viewModelScope.launch {
            chatRepository.observeChat(chatId).collect { result ->
                result.onSuccess { chat ->
                    _currentChat.value = chat
                    updateUiState()
                }
            }
        }
    }

    private fun observeMessages() {
        viewModelScope.launch {
            chatRepository.observeMessages(chatId).collect { result ->
                result.onSuccess { messages ->
                    _messages.value = messages
                    updateUiState()
                }
            }
        }
    }

    private fun updateUiState() {
        val chat = _currentChat.value
        if (chat != null) {
            _uiState.value = ChatUiState.Success(chat, _messages.value)
        }
    }

    private fun markSeen() {
        val uid = currentUid
        if (uid.isEmpty()) return
        viewModelScope.launch { chatRepository.markMessagesAsSeen(chatId, uid) }
    }

    fun setMessageInput(text: String) { _messageInput.value = text }

    fun sendMessage() {
        val uid = currentUid
        val text = _messageInput.value.trim()
        if (uid.isEmpty() || text.isEmpty() || _isSending.value) return

        _isSending.value = true
        viewModelScope.launch {
            val user = userRepository.getUser(uid).getOrNull()
            val message = ChatMessage(
                messageId  = UUID.randomUUID().toString(),
                senderId   = uid,
                senderName = user?.name ?: "Student",
                message    = text,
                timestamp  = Timestamp.now(),
                type       = MessageType.TEXT.name
            )
            chatRepository.sendMessage(chatId, message)
                .onSuccess { _messageInput.value = "" }
            _isSending.value = false
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch { chatRepository.deleteMessage(chatId, messageId) }
    }

    fun reportMessage(messageId: String, reason: String = "Inappropriate content") {
        viewModelScope.launch { chatRepository.reportMessage(chatId, messageId, reason) }
    }

    fun getDisplayName(): String = _currentChat.value?.getDisplayName(currentUid) ?: ""
    fun getDisplayPhoto(): String = _currentChat.value?.getDisplayPhoto(currentUid) ?: ""
}
