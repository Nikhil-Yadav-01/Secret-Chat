package com.rudraksha.secretchat.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.rudraksha.secretchat.data.WebSocketData
import com.rudraksha.secretchat.data.model.Chat
import com.rudraksha.secretchat.database.ChatDatabase
import com.rudraksha.secretchat.data.model.Message
import com.rudraksha.secretchat.data.remote.WebSocketManager
import com.rudraksha.secretchat.data.toMessage
import com.rudraksha.secretchat.utils.getReceivers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

class ChatDetailViewModel(application: Application, private val username: String, private val chatId: String) : AndroidViewModel(application) {
    private val messageDao = ChatDatabase.getDatabase(application).messageDao()
    private val chatDao = ChatDatabase.getDatabase(application).chatDao()
    private val userDao = ChatDatabase.getDatabase(application).userDao()
    private val webSocketManager = WebSocketManager(username = username)

    private var _chatDetailUiState = MutableStateFlow(ChatDetailUiState())
    val chatDetailUiState: StateFlow<ChatDetailUiState> = _chatDetailUiState.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    init {
        viewModelScope.launch {
            _chatDetailUiState.value.isLoading = true
            webSocketManager.connect()
            loadMessages()
            webSocketManager.setOnDataReceivedListener { webSocketData ->
                viewModelScope.launch {
                    when (webSocketData) {
                        is WebSocketData.Message -> {
                            if (webSocketData.chatId == "") {
                                _chatDetailUiState.value.toastMessage = webSocketData.content
                            } else {
                                messageDao.insertMessage(webSocketData.toMessage())
                                var chat = chatDao.getChatById(webSocketData.chatId)
                                if (chat == null) {
                                    Log.e("Received", "Chat not found")
                                } else {
                                    val updatedChat = chat.copy(
                                        unreadCount = chat.unreadCount + 1,
                                    )
                                    Log.d("Received", "Chat found $chat")
                                    chatDao.insertChat(updatedChat)
                                    Log.d("Received", "Chat updated $updatedChat")
                                }
                            }
                        }
                        else -> {
                            Log.e("Received", "Else case")
                        }
                    }
                    Log.d("Inserted", webSocketData.toString())
                    loadMessages()
                }
            }
            _chatDetailUiState.value.isLoading = false

            observeConnectionStatus()
        }
    }

    private fun observeConnectionStatus() {
        viewModelScope.launch {
            webSocketManager.isConnected.collectLatest { isConnected ->
                _isConnected.value = isConnected
                // You can perform actions here based on the connection status
                if (isConnected) {
                    println("WebSocket is connected")
                } else {
                    println("WebSocket is disconnected")
                }
            }
        }
    }

    private suspend fun loadMessages() {
        _chatDetailUiState.value.isLoading = true
        _chatDetailUiState.value.messages = messageDao.getMessagesForChat(chatId)
        _chatDetailUiState.value.isLoading = false
    }

    fun sendMessage(text: String, chat: Chat) {
        viewModelScope.launch {
            _chatDetailUiState.value.isLoading = true
            val message = WebSocketData.Message(
                id = UUID.randomUUID().toString(),
                sender = username,
                receivers = getReceivers(chat.participants, username),
                chatId = chatId,
                content = text,
                timestamp = System.currentTimeMillis(),
            )
            messageDao.insertMessage(message.toMessage())
            webSocketManager.sendData(message)
            loadMessages()
            _chatDetailUiState.value.isLoading = false
        }
    }

    fun deleteMessage(message: Message) {
        viewModelScope.launch {
            _chatDetailUiState.value.isLoading = true
//            messageDao.deleteMessage(message)
            loadMessages()
            _chatDetailUiState.value.isLoading = false
        }
    }


    fun updateAllMessages(messageList: List<Message>) {
        viewModelScope.launch {
            _chatDetailUiState.value.isLoading = true
            messageList.forEach {
                messageDao.insertMessage(it.copy(isRead = true))
            }
            _chatDetailUiState.value.isLoading = false
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
    var messages: List<Message>? = null,
    var isLoading: Boolean = false,
    var isConnected: Boolean = false,
    var errorMessage: String? = null,
    var toastMessage: String? = null,
)