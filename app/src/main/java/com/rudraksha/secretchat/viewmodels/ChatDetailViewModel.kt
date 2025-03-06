package com.rudraksha.secretchat.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.rudraksha.secretchat.database.ChatDatabase
import com.rudraksha.secretchat.data.model.Message
import com.rudraksha.secretchat.data.remote.WebSocketManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ChatDetailViewModel(application: Application, private val username: String, private val chatId: String) : AndroidViewModel(application) {
    private val messageDao = ChatDatabase.getDatabase(application).messageDao()
    private val userDao = ChatDatabase.getDatabase(application).userDao()
    private val webSocketManager = WebSocketManager(username = username)

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    init {
        viewModelScope.launch {
            webSocketManager.connect(username)
            loadMessages()
            webSocketManager.setOnMessageReceivedListener { message ->
                // Optionally filter messages by chatId.
                viewModelScope.launch {
                    messageDao.insertMessage(message)
                    loadMessages()
                }
            }
        }
    }

    private suspend fun loadMessages() {
        _messages.value = messageDao.getMessagesForChat(chatId)
    }

    fun sendMessage(text: String, receivers: String) {
        val message = Message(
            senderId = username,
            receiversId = receivers, // For a private chat, assume chatId is the receiver.
            content = text,
        )
        viewModelScope.launch {
            messageDao.insertMessage(message)
            webSocketManager.sendMessage(message)
            loadMessages()
        }
    }

    fun deleteMessage(message: Message) {
        viewModelScope.launch {
//            messageDao.deleteMessage(message)
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