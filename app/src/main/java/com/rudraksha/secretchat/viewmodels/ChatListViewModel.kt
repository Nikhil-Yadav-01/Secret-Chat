package com.rudraksha.secretchat.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudraksha.secretchat.data.entity.ChatEntity
import com.rudraksha.secretchat.database.ChatDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatListViewModel(application: Application) : AndroidViewModel(application) {
    private val chatDao = ChatDatabase.getDatabase(application).chatDao()

    private val _chatEntityList = MutableStateFlow<List<ChatEntity>>(emptyList())
    val chatEntityList: StateFlow<List<ChatEntity>> = _chatEntityList.asStateFlow()

    init {
        getAllChats()
    }

    fun addChat(chatEntity: ChatEntity) {
        viewModelScope.launch { chatDao.insertChat(chatEntity) }
        Log.d("CLVM_add", "Chat added: $chatEntity")
    }

    fun getAllChats() {
        viewModelScope.launch {
            chatDao.getAllChats().collect { chats ->
                _chatEntityList.value = chats
            }
        }
    }
}
