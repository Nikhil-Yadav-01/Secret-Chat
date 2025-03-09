package com.rudraksha.secretchat.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.rudraksha.secretchat.data.model.Message
import com.rudraksha.secretchat.data.remote.WebSocketManager
import com.rudraksha.secretchat.utils.getReceivers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InvisibleChatViewModel(application: Application, private val username: String) : AndroidViewModel(application) {
    private val webSocketManager = WebSocketManager(username = username)

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    init {
        viewModelScope.launch {
            webSocketManager.connect()
            webSocketManager.setOnMessageReceivedListener { message ->
                if (message.chatId.endsWith("invisible")) {
                    viewModelScope.launch {
                        _messages.value += message
                    }
                }
                message.content?.let { Log.d("Received", it) }
            }
        }
    }

    fun sendMessage(text: String, chatId: String) {
        val message = Message(
            senderId = username,
            chatId = chatId + "invisible",
            receiversId = getReceivers(chatId, username), // For a private chat, assume chatId is the receiver.
            content = text,
        )
        viewModelScope.launch {
            webSocketManager.sendMessage(message)
        }
    }
}

class InvisibleChatViewModelFactory(
    private val username: String,
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(InvisibleChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InvisibleChatViewModel(application, username) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}