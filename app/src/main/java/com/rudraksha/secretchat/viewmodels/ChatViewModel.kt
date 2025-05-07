package com.rudraksha.secretchat.viewmodels

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudraksha.secretchat.data.entity.MessageEntity
import com.rudraksha.secretchat.data.model.MessageType
import com.rudraksha.secretchat.network.SecureChatService
import com.rudraksha.secretchat.database.ChatDatabase
import com.rudraksha.secretchat.repo.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = ChatDatabase.getDatabase(application)
    private val chatRepository = ChatRepository(
        context = application,
        messageDao = database.messageDao(),
        chatDao = database.chatDao()
    )
    
    private val _messages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val messages: StateFlow<List<MessageEntity>> = _messages.asStateFlow()
    
    private val _currentChatId = MutableStateFlow("")
    val currentChatId: StateFlow<String> = _currentChatId.asStateFlow()
    
    private val _connectionState = MutableStateFlow<SecureChatService.ConnectionState>(
        SecureChatService.ConnectionState.Disconnected
    )
    val connectionState: StateFlow<SecureChatService.ConnectionState> = _connectionState.asStateFlow()
    
    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()
    
    init {
        // Observe connection state changes
        viewModelScope.launch {
            chatRepository.connectionState.collect { state ->
                _connectionState.value = state
            }
        }
    }
    
    /**
     * Connect to the chat server
     */
    fun connect(username: String) {
        chatRepository.connect(username)
    }
    
    /**
     * Disconnect from the chat server
     */
    fun disconnect() {
        viewModelScope.launch {
            chatRepository.disconnect()
        }
    }
    
    /**
     * Set the current chat ID and load messages
     */
    fun setChatId(userId: String, recipientId: String) {
        val chatId = generateChatId(userId, recipientId)
        _currentChatId.value = chatId
        
        // Load messages for this chat
        viewModelScope.launch {
            chatRepository.getMessagesForChat(chatId).collect { msgs ->
                _messages.value = msgs
            }
        }
    }
    
    /**
     * Send a text message
     */
    fun sendTextMessage(senderId: String, recipientId: String, content: String) {
        if (content.isBlank()) return
        
        viewModelScope.launch {
            _isSending.value = true
            try {
                chatRepository.sendTextMessage(senderId, recipientId, content)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send message: ${e.message}")
            } finally {
                _isSending.value = false
            }
        }
    }
    
    /**
     * Send a self-destructing message
     */
    fun sendSelfDestructMessage(
        senderId: String,
        recipientId: String,
        content: String,
        timeToLiveSeconds: Long
    ) {
        if (content.isBlank()) return
        
        viewModelScope.launch {
            _isSending.value = true
            try {
                chatRepository.sendSelfDestructMessage(
                    senderId, recipientId, content, timeToLiveSeconds
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send self-destruct message: ${e.message}")
            } finally {
                _isSending.value = false
            }
        }
    }
    
    /**
     * Send a file message (image, video, or document)
     */
    fun sendFile(senderId: String, recipientId: String, uri: Uri, type: MessageType) {
        viewModelScope.launch {
            _isSending.value = true
            try {
                // Create a temporary file to handle content URIs
                val file = createTempFileFromUri(uri, type)
                if (file != null) {
                    chatRepository.sendFile(senderId, recipientId, file, type)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send file: ${e.message}")
            } finally {
                _isSending.value = false
            }
        }
    }
    
    /**
     * Create a temporary file from a content URI
     */
    private fun createTempFileFromUri(uri: Uri, type: MessageType): File? {
        return try {
            val context = getApplication<Application>()
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            
            // Generate file extension based on type
            val extension = when (type) {
                MessageType.IMAGE -> ".jpg"
                MessageType.VIDEO -> ".mp4"
                MessageType.AUDIO -> ".m4a"
                else -> ".bin"
            }
            
            // Create temp file
            val fileName = "secure_chat_${System.currentTimeMillis()}$extension"
            val tempFile = File(context.cacheDir, fileName)
            
            // Copy content to file
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            
            tempFile
        } catch (e: Exception) {
            Log.e(TAG, "Error creating temp file: ${e.message}")
            null
        }
    }
    
    /**
     * Mark a message as read
     */
    fun markMessageAsRead(messageId: String) {
        viewModelScope.launch {
            database.messageDao().updateMessageReadStatus(messageId, true)
        }
    }
    
    /**
     * Delete a message (for self-destruct or manual deletion)
     */
    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            database.messageDao().deleteMessage(messageId)
        }
    }
    
    /**
     * Generate a consistent chat ID for two users
     */
    private fun generateChatId(user1: String, user2: String): String {
        val users = listOf(user1, user2).sorted()
        return "${users[0]}_${users[1]}"
    }
    
    companion object {
        private const val TAG = "ChatViewModel"
    }
} 