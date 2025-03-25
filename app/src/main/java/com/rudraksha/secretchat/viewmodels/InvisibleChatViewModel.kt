package com.rudraksha.secretchat.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.rudraksha.secretchat.data.WebSocketData
import com.rudraksha.secretchat.data.model.ChatEntity
import com.rudraksha.secretchat.data.model.ChatType
import com.rudraksha.secretchat.data.model.MessageEntity
import com.rudraksha.secretchat.data.remote.WebSocketManager
import com.rudraksha.secretchat.data.toMessage
import com.rudraksha.secretchat.database.ChatDatabase
import com.rudraksha.secretchat.utils.createChatId
import com.rudraksha.secretchat.utils.getReceivers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

class InvisibleChatViewModel(application: Application, private val webSocketManager: WebSocketManager) : AndroidViewModel(application) {
    private var _invisibleChatUiState = MutableStateFlow(InvisibleChatUiState())
    val invisibleChatUiState: StateFlow<InvisibleChatUiState> = _invisibleChatUiState.asStateFlow()

    init {
        viewModelScope.launch {
            _invisibleChatUiState.value.isLoading = true
            webSocketManager.connect()
            webSocketManager.setOnDataReceivedListener { webSocketData ->
                viewModelScope.launch {
                    when (webSocketData) {
                        is WebSocketData.ConnectionStatus -> {
                            _invisibleChatUiState.value.isConnected = webSocketData.status
                            if (webSocketData.status) {
                                println("WebSocket is connected")
                            } else {
                                println("WebSocket is disconnected")
                            }
                        }
                        is WebSocketData.Message -> {
                            loadMessages(webSocketData)
                        }
                        else -> {
                            Log.e("Received", "Else case $webSocketData")
                        }
                    }
                }
            }
            _invisibleChatUiState.value.isLoading = false

            observeConnectionStatus()
        }
    }

    private fun observeConnectionStatus() {
        viewModelScope.launch {
            webSocketManager.isConnected.collectLatest { isConnected ->
                _invisibleChatUiState.value.isConnected = isConnected
                if (_invisibleChatUiState.value.isConnected) {
                    println("WebSocket is connected")
                } else {
                    println("WebSocket is disconnected")
                }
            }
        }
    }

    private fun loadMessages(message: WebSocketData.Message? = null) {
        viewModelScope.launch {
            message?.let {
                _invisibleChatUiState.value = _invisibleChatUiState.value.copy(
                    messageEntities = invisibleChatUiState.value.messageEntities?.plus(
                        it.toMessage()
                    )
                )
            }
        }
        Log.d("LM IXVM", message.toString())
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            _invisibleChatUiState.value.isLoading = true
            _invisibleChatUiState.value.chatEntity?.let {
                val message = WebSocketData.Message(
                    id = UUID.randomUUID().toString(),
                    sender = webSocketManager.username,
                    receivers = getReceivers(it.participants, webSocketManager.username),
                    chatId = it.chatId,
                    content = text,
                    timestamp = System.currentTimeMillis(),
                )
                webSocketManager.sendData(message)
                loadMessages(message)
            }
            _invisibleChatUiState.value.isLoading = false
        }
    }

    fun deleteMessage(messageEntity: MessageEntity) {
        viewModelScope.launch {
            _invisibleChatUiState.value.isLoading = true
            _invisibleChatUiState.value = _invisibleChatUiState.value.copy(
                messageEntities = invisibleChatUiState.value.messageEntities?.minus(messageEntity)
            )
            loadMessages()
            _invisibleChatUiState.value.isLoading = false
        }
    }

    fun createChat(receivers: List<String>) {
        val newChat = ChatEntity(
            chatId = createChatId(receivers + webSocketManager.username, chatType = ChatType.SECRET),
            type = ChatType.SECRET,
            participants = receivers.joinToString(",") + "," + webSocketManager.username,
            createdBy = webSocketManager.username,
        )
        _invisibleChatUiState.value.chatEntity = newChat
    }
}

class InvisibleChatViewModelFactory(
    private val application: Application,
    private val webSocketManager: WebSocketManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(InvisibleChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InvisibleChatViewModel(application, webSocketManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class InvisibleChatUiState(
    var messageEntities: List<MessageEntity>? = null,
    var isLoading: Boolean = false,
    var isConnected: Boolean = false,
    var errorMessage: String? = null,
    var toastMessage: String? = null,
    var chatEntity: ChatEntity? = null,
)