package com.rudraksha.secretchat.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.rudraksha.secretchat.data.WebSocketData
import com.rudraksha.secretchat.data.model.ChatItem
import com.rudraksha.secretchat.database.ChatDatabase
import com.rudraksha.secretchat.data.entity.MessageEntity
import com.rudraksha.secretchat.network.WebSocketManager
import com.rudraksha.secretchat.data.toMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

class MessagesViewModel(
    application: Application, private val username: String, private val webSocketManager: WebSocketManager, private val chatId: String)
    : AndroidViewModel(application) {
    private val messageDao = ChatDatabase.getDatabase(application).messageDao()
    private val chatDao = ChatDatabase.getDatabase(application).chatDao()

    private var _messagesUiState = MutableStateFlow(MessagesUiState())
    val messagesUiState: StateFlow<MessagesUiState> = _messagesUiState.asStateFlow()

    init {
        viewModelScope.launch {
            _messagesUiState.value.isLoading = true
//            webSocketManager.connect()
            loadMessages()
            webSocketManager.setOnDataReceivedListener { webSocketData ->
                viewModelScope.launch {
                    when (webSocketData) {
                        is WebSocketData.Message -> {
                            if (webSocketData.chatId == "") {
                                _messagesUiState.value = _messagesUiState.value.copy(
                                    toastMessage = webSocketData.content
                                )
                            } else {
                                messageDao.insertMessage(webSocketData.toMessage())
                                val chat = chatDao.getChatById(webSocketData.chatId)
                                if (chat == null) {
                                    Log.e("Received", "Chat not found $chat")
                                } else {
                                    val updatedChat = webSocketData.content?.let {
                                        chat.copy(
                                            lastMessage = it,
                                            lastEventAt = webSocketData.timestamp,
                                            unreadCount = if (webSocketData.sender != username) chat.unreadCount + 1 else chat.unreadCount,
                                        )
                                    }
                                    Log.d("Received", "Chat found $chat")
                                    if (updatedChat != null) {
                                        chatDao.insertChat(updatedChat)
                                    }
                                    Log.d("Received", "Chat updated $updatedChat")
                                }
                            }
                        }
                        else -> {
                            Log.e("Received", "Else case $webSocketData")
                        }
                    }
                    Log.d("Inserted", webSocketData.toString())
                    loadMessages()
                }
            }

            observeConnectionStatus()
            _messagesUiState.value.isLoading = false
        }
    }

    private fun observeConnectionStatus() {
        viewModelScope.launch {
            webSocketManager.isConnected.collectLatest { isConnected ->
                _messagesUiState.value.isConnected = isConnected
                if (_messagesUiState.value.isConnected) {
                    println("WebSocket is connected")
                } else {
                    println("WebSocket is disconnected")
                }
            }
        }
    }

    private suspend fun loadMessages() {
        _messagesUiState.value = _messagesUiState.value.copy(
            isLoading = true
        )
        _messagesUiState.value = _messagesUiState.value.copy(
            messageEntities = messageDao.getMessagesForChat(chatId)
        )
        _messagesUiState.value = _messagesUiState.value.copy(
            isLoading = false
        )
    }

    fun sendMessage(text: String, chat: ChatItem) {
        viewModelScope.launch {
            _messagesUiState.value.isLoading = true
            val message = WebSocketData.Message(
                id = UUID.randomUUID().toString(),
                sender = username,
                receivers = chat.receivers,
                chatId = chat.id,
                content = text,
                timestamp = System.currentTimeMillis(),
            )
            messageDao.insertMessage(message.toMessage())
            webSocketManager.sendData(message)
            webSocketManager.sendData(WebSocketData.GetUsers(username))
            loadMessages()
            _messagesUiState.value.isLoading = false
        }
    }

    fun deleteMessage(messageEntity: MessageEntity) {
        viewModelScope.launch {
            _messagesUiState.value.isLoading = true
//            messageDao.deleteMessage(message)
            loadMessages()
            _messagesUiState.value.isLoading = false
        }
    }

    fun updateAllMessages(messageEntityList: List<MessageEntity>) {
        viewModelScope.launch {
            _messagesUiState.value.isLoading = true
            messageEntityList.forEach {
                messageDao.insertMessage(it.copy(isRead = true))
            }
            _messagesUiState.value.isLoading = false
        }
    }
}

class MessagesViewModelFactory(
    private val chatId: String,
    private val username: String,
    private val webSocketManager: WebSocketManager,
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(MessagesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MessagesViewModel(application = application, username = username, webSocketManager = webSocketManager, chatId = chatId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class MessagesUiState(
    var messageEntities: List<MessageEntity>? = null,
    var isLoading: Boolean = false,
    var isConnected: Boolean = false,
    var errorMessage: String? = null,
    var toastMessage: String? = null,
)