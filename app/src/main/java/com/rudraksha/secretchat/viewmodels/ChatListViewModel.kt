package com.rudraksha.secretchat.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rudraksha.secretchat.database.ChatDatabase
import com.rudraksha.secretchat.data.model.Chat
import com.rudraksha.secretchat.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatListViewModel(application: Application) : AndroidViewModel(application) {
    private val chatDao = ChatDatabase.getDatabase(application).chatDao()
    private val userDao = ChatDatabase.getDatabase(application).userDao()

    private val _chatList = MutableStateFlow<List<Chat>>(emptyList())
    val chatList: StateFlow<List<Chat>> = _chatList.asStateFlow()

    private val _registeredUser = MutableStateFlow<User?>(null)
    val registeredUser: StateFlow<User?> = _registeredUser.asStateFlow()

    init {
        getAllChats()
    }

    fun addChat(chat: Chat) {
        viewModelScope.launch { chatDao.insertChat(chat) }
    }

    fun getRegisteredUser() {
        viewModelScope.launch { _registeredUser.value = userDao.getRegisteredUser() }
    }

    fun getAllChats() {
        viewModelScope.launch {
            chatDao.getAllChats().collect { chats ->
                _chatList.value = chats
            }
        }
    }
}
