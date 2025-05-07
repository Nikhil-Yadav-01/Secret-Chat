package com.rudraksha.secretchat.repo

import android.content.Context
import android.util.Log
import com.rudraksha.secretchat.data.entity.MessageEntity
import com.rudraksha.secretchat.data.model.MessageType
import com.rudraksha.secretchat.network.SecureChatService
import com.rudraksha.secretchat.database.ChatDao
import com.rudraksha.secretchat.database.MessageDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// Data Classes
data class Chat(
    val chatId: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String? = null,
    val lastMessageTimestamp: Long? = null
)

data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

class ChatRepository(
    private val context: Context,
    private val messageDao: MessageDao,
    private val chatDao: ChatDao
) {
    private val secureChatService = SecureChatService(context)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val _connectionState = MutableStateFlow<SecureChatService.ConnectionState>(
        SecureChatService.ConnectionState.Disconnected
    )
    val connectionState = _connectionState.asStateFlow()
    
    init {
        // Observe connection state changes from the service
        scope.launch {
            secureChatService.connectionState.collect { state ->
                _connectionState.value = state
                
                // If connected, start collecting incoming messages
                if (state is SecureChatService.ConnectionState.Connected) {
                    collectIncomingMessages()
                }
            }
        }
    }
    
    /**
     * Connect to the chat server
     */
    fun connect(username: String) {
        secureChatService.connect(username)
    }
    
    /**
     * Disconnect from the chat server
     */
    suspend fun disconnect() {
        secureChatService.disconnect()
    }
    
    /**
     * Send a text message to a recipient
     */
    suspend fun sendTextMessage(senderId: String, recipientId: String, content: String): MessageEntity {
        val chatId = generateChatId(senderId, recipientId)
        
        val message = MessageEntity(
            senderId = senderId,
            receiversId = recipientId,
            chatId = chatId,
            type = MessageType.TEXT,
            content = content
        )
        
        // Save message to local database
        messageDao.insertMessage(message)
        
        // Send through secure service
        secureChatService.sendMessage(message)
        
        return message
    }
    
    /**
     * Send a self-destructing message
     */
    suspend fun sendSelfDestructMessage(
        senderId: String, 
        recipientId: String, 
        content: String,
        timeToLiveSeconds: Long
    ): MessageEntity {
        val chatId = generateChatId(senderId, recipientId)
        
        val message = MessageEntity(
            senderId = senderId,
            receiversId = recipientId,
            chatId = chatId,
            type = MessageType.SELF_DESTRUCT,
            content = content
        )
        
        // Save message to local database (with flag for self-destructing)
        messageDao.insertMessage(message)
        
        // Send through secure service
        secureChatService.sendSelfDestructMessage(content, recipientId, timeToLiveSeconds)
        
        return message
    }
    
    /**
     * Send a file message (image, video, or document)
     */
    suspend fun sendFile(
        senderId: String,
        recipientId: String,
        file: File,
        messageType: MessageType
    ): MessageEntity {
        val chatId = generateChatId(senderId, recipientId)
        
        val message = MessageEntity(
            senderId = senderId,
            receiversId = recipientId,
            chatId = chatId,
            type = messageType,
            content = file.name
        )
        
        // Save message to local database
        messageDao.insertMessage(message)
        
        // Send through secure service
        secureChatService.sendFile(file, recipientId, messageType)
        
        return message
    }
    
    /**
     * Get all messages for a specific chat
     */
    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>> {
        return messageDao.getMessagesByChatId(chatId)
    }
    
    /**
     * Collect incoming messages from the service and save to database
     */
    private fun collectIncomingMessages() {
        scope.launch {
            secureChatService.incomingMessages.collect { message ->
                // Handle self-destructing messages
                if (message.type == MessageType.SELF_DESTRUCT) {
                    handleSelfDestructMessage(message)
                } else {
                    // Save regular messages to database
                    messageDao.insertMessage(message)
                }
            }
        }
    }
    
    /**
     * Handle self-destructing messages with timer
     */
    private suspend fun handleSelfDestructMessage(message: MessageEntity) {
        withContext(Dispatchers.IO) {
            try {
                // Extract time-to-live from content (format: "content|ttlSeconds")
                val content = message.content ?: return@withContext
                val parts = content.split("|")
                if (parts.size == 2) {
                    val actualContent = parts[0]
                    val ttlSeconds = parts[1].toLongOrNull() ?: 0
                    
                    // Store with actual content
                    val newMessage = message.copy(content = actualContent)
                    messageDao.insertMessage(newMessage)
                    
                    // Schedule deletion after TTL
                    if (ttlSeconds > 0) {
                        kotlinx.coroutines.delay(ttlSeconds * 1000)
                        messageDao.deleteMessage(newMessage.messageId)
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatRepository", "Error handling self-destruct message: ${e.message}")
            }
        }
    }
    
    /**
     * Generate a consistent chat ID for two users
     */
    private fun generateChatId(user1: String, user2: String): String {
        val users = listOf(user1, user2).sorted()
        return "${users[0]}_${users[1]}"
    }
}
