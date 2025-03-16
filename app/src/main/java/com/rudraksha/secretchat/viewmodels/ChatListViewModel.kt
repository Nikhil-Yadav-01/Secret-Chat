package com.rudraksha.secretchat.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudraksha.secretchat.data.model.Chat
import com.rudraksha.secretchat.database.ChatDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatListViewModel(application: Application) : AndroidViewModel(application) {
    private val chatDao = ChatDatabase.getDatabase(application).chatDao()

    private val _chatList = MutableStateFlow<List<Chat>>(emptyList())
    val chatList: StateFlow<List<Chat>> = _chatList.asStateFlow()

    init {
        getAllChats()
    }

    fun addChat(chat: Chat) {
        viewModelScope.launch { chatDao.insertChat(chat) }
        Log.d("CLVM_add", "Chat added: $chat")
    }

    fun getAllChats() {
        viewModelScope.launch {
            chatDao.getAllChats().collect { chats ->
                _chatList.value = chats
            }
        }
    }
}
