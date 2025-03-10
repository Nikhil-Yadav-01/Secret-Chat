package com.rudraksha.secretchat.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.rudraksha.secretchat.database.ChatDatabase
import com.rudraksha.secretchat.data.model.Message
import com.rudraksha.secretchat.data.remote.WebSocketManager
import com.rudraksha.secretchat.utils.createChatId
import com.rudraksha.secretchat.utils.getReceivers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatDetailViewModel(application: Application, private val username: String, private val chatId: String) : AndroidViewModel(application) {
    private val messageDao = ChatDatabase.getDatabase(application).messageDao()
    private val webSocketManager = WebSocketManager(username = username)

    private var _chatDetailUiState = MutableStateFlow<ChatDetailUiState>(ChatDetailUiState())
    val chatDetailUiState: StateFlow<ChatDetailUiState> = _chatDetailUiState.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>?>(null)
    val messages = _messages.asStateFlow()

    private val _lastMessage = MutableStateFlow<Message?>(null)
    val lastMessage = _lastMessage.asStateFlow()

    init {
        viewModelScope.launch {
            _chatDetailUiState.value.isLoading = true
            webSocketManager.connect()
            loadMessages()
            webSocketManager.setOnMessageReceivedListener { message ->
                viewModelScope.launch {
                    messageDao.insertMessage(message)
                    Log.d("Inserted", message.toString())
                    loadMessages()
                }
            }
            _chatDetailUiState.value.isLoading = false
        }
    }

    private suspend fun loadMessages() {
        _chatDetailUiState.value.isLoading = true
        _messages.value = messageDao.getMessagesForChat(chatId)
        val temp = _messages.value?.firstOrNull()
        temp?.let { message ->
            _lastMessage.value?.let { last ->
                if (message.timestamp > last.timestamp) {
                    _lastMessage.value = temp
                }
            }
        }
        _lastMessage.value?.let { Log.d("LastMessage", it.toString()) }
        _chatDetailUiState.value.isLoading = false
    }

    fun sendMessage(text: String, chatId: String) {
        _chatDetailUiState.value.isLoading = true
        Log.d("Receivers", getReceivers(chatId, username))
        val message = Message(
            senderId = username,
            chatId = chatId,
            receiversId = getReceivers(chatId, username), // For a private chat, assume chatId is the receiver.
            content = text,
        )
        viewModelScope.launch {
            messageDao.insertMessage(message)
            webSocketManager.sendMessage(message)
            loadMessages()
        }
        _chatDetailUiState.value.isLoading = false
    }

    fun deleteMessage(message: Message) {
        viewModelScope.launch {
            _chatDetailUiState.value.isLoading = true
//            messageDao.deleteMessage(message)
            _chatDetailUiState.value.isLoading = false
            loadMessages()
        }
    }
}

class ChatDetailViewModelFactory(
    private val chatId: String,
    private val username: String,
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(ChatDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatDetailViewModel(application, chatId, username) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class ChatDetailUiState(
    var messages: List<Message> = emptyList(),
    var isLoading: Boolean = false,
    var errorMessage: String? = null,
    var lastMessage: Message? = null,
)