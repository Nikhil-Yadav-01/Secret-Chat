package com.rudraksha.secretchat.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.rudraksha.secretchat.data.WebSocketData
import com.rudraksha.secretchat.data.entity.ChatEntity
import com.rudraksha.secretchat.data.model.ChatType
import com.rudraksha.secretchat.data.entity.UserEntity
import com.rudraksha.secretchat.network.WebSocketManager
import com.rudraksha.secretchat.database.ChatDatabase
import com.rudraksha.secretchat.utils.createChatId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MiscellaneousViewModel(application: Application, private val webSocketManager: WebSocketManager)
    : AndroidViewModel(application) {

    private val chatDao = ChatDatabase.getDatabase(application).chatDao()
    private var _miscellaneousUiState = MutableStateFlow(MiscellaneousUiState())
    val miscellaneousUiState: StateFlow<MiscellaneousUiState> = _miscellaneousUiState.asStateFlow()

    init {
        viewModelScope.launch {
            webSocketManager.connect()
            webSocketManager.setOnDataReceivedListener { webSocketData ->
                viewModelScope.launch {
                    when (webSocketData) {
                        is WebSocketData.GetUsers -> {
                            Log.d("MVM Listener GU", webSocketData.toString())
//                            _miscellaneousUiState.value = _miscellaneousUiState.value.copy(
//                                users = webSocketData
//                            )
                        }
                        is WebSocketData.ChatList -> {
                            Log.d("MVM Listener CL", webSocketData.toString())
                            _miscellaneousUiState.value = _miscellaneousUiState.value.copy(
                                chats = webSocketData
                            )
                        }
                        is WebSocketData.UserList -> {
                            Log.d("MVM Listener UL", webSocketData.toString())
                            _miscellaneousUiState.value = _miscellaneousUiState.value.copy(
                                users = webSocketData
                            )
                        }
                        else -> {
                            Log.d("MVM Listener Else", webSocketData.toString())
                            _miscellaneousUiState.value = _miscellaneousUiState.value.copy(
                                data = _miscellaneousUiState.value.data?.plus(webSocketData) ?: listOf(webSocketData)
                            )
                        }
                    }
                }
            }
        }
    }

    fun saveUser(userEntity: UserEntity) {
        viewModelScope.launch {
            webSocketManager.sendData(WebSocketData.SaveUser(userEntity.username))
        }
    }

    fun saveChat(chatEntity: ChatEntity) {
        viewModelScope.launch {
            webSocketManager.sendData(WebSocketData.SaveChat(chatEntity.chatId))
        }
    }

    fun fetchUsers(username: String) {
        viewModelScope.launch {
            webSocketManager.sendData(WebSocketData.GetUsers(username))
        }
    }

    fun fetchChats(username: String) {
        viewModelScope.launch {
            webSocketManager.sendData(WebSocketData.GetChats(username))
        }
    }

    suspend fun createOrGetChat(creator: String, receivers: List<String>, chatName: String, chatType: ChatType): String = withContext(Dispatchers.IO) {
        // Simulate checking for an existing chat.
        val chatId = createChatId(receivers + creator, chatType = chatType)
        val existingChat = findExistingChat(chatId)

        if (existingChat != null) {
            Log.d("ChatViewModel", "Chat already exists with ID: ${existingChat.chatId}")
            return@withContext existingChat.chatId
        }

        // Simulate creating a new chat.
        val newChatId = createNewChat(
            chatId = chatId, chatName = chatName, chatType = chatType,
            participants = receivers + creator, createdBy = creator
        )
        Log.d("ChatViewModel", "New chat created with ID: $newChatId")
        return@withContext newChatId
    }

    private suspend fun findExistingChat(chatId: String): ChatEntity? {
        // Check if a chat already exists for the given members
        val chatList = listOf<ChatEntity>()
        val chatFound = chatList.find { it.chatId == chatId }
//        val chatFound = _miscellaneousUiState.value.chats.chats.find { it == chatId }
        return chatFound
    }

    private suspend fun createNewChat(
        chatId: String, chatName: String, chatType: ChatType,
        createdBy: String,  participants: List<String>,
    ): String {
        val newChat = ChatEntity(
            chatId = chatId,
            name = chatName,
            type = chatType,
            participants = participants.joinToString(","),
            createdBy = createdBy,
        )
        chatDao.insertChat(newChat)
        return newChat.chatId
    }
}

class MiscellaneousViewModelFactory(
    private val application: Application,
    private val webSocketManager: WebSocketManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(MiscellaneousViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MiscellaneousViewModel(application = application, webSocketManager = webSocketManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class MiscellaneousUiState(
    var chats: WebSocketData.ChatList? = null,
    var users: WebSocketData.UserList? = null,
    var data: List<WebSocketData>? = null,
    var isLoading: Boolean = false,
    var isConnected: Boolean = false,
    var errorMessage: String? = null,
    var toastMessage: String? = null,
)
