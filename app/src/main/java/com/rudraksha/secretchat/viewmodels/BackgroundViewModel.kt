package com.rudraksha.secretchat.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.rudraksha.secretchat.data.WebSocketData
import com.rudraksha.secretchat.network.WebSocketManager
import com.rudraksha.secretchat.data.toMessage
import com.rudraksha.secretchat.database.ChatDatabase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BackgroundViewModel(application: Application, private val username: String) :
    AndroidViewModel(application) {
    private val messageDao = ChatDatabase.getDatabase(application).messageDao()
    private val chatDao = ChatDatabase.getDatabase(application).chatDao()
    private val webSocketManager = WebSocketManager(username = username)

    init {
        viewModelScope.launch {
            webSocketManager.connect()
            webSocketManager.setOnDataReceivedListener { webSocketData ->
                viewModelScope.launch {
                    when (webSocketData) {
                        is WebSocketData.Message -> {
                            messageDao.insertMessage(webSocketData.toMessage())
                            val chat = chatDao.getChatById(webSocketData.chatId)
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

                        else -> {
                            Log.e("Received", "Else case")
                        }
                    }
                    Log.d("Inserted", webSocketData.toString())
                }
            }
            observeConnectionStatus()
        }
    }

    private fun observeConnectionStatus() {
        viewModelScope.launch {
            webSocketManager.isConnected.collectLatest { isConnected ->
                // You can perform actions here based on the connection status
                if (isConnected) {
                    println("WebSocket is connected")
                } else {
                    println("WebSocket is disconnected")
                }
            }
        }
    }
}


class BackgroundViewModelFactory(
    private val username: String,
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(BackgroundViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BackgroundViewModel(application, username) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}