package com.rudraksha.secretchat.network

import android.content.Context
import android.util.Log
import com.rudraksha.secretchat.data.entity.MessageEntity
import com.rudraksha.secretchat.data.model.MessageType
import com.rudraksha.secretchat.utils.EncryptionUtils
import com.rudraksha.secretchat.utils.KeyManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.time.Duration.Companion.seconds

/**
 * Service for handling secure chat communication through WebSockets
 * Implements end-to-end encryption using AES and RSA
 */
class SecureChatService(private val context: Context) {
    
    private val json = Json { ignoreUnknownKeys = true }
    private val keyManager = KeyManager(context)
    
    private val client = HttpClient(OkHttp) {
        install(WebSockets)
    }
    
    private var webSocketSession: WebSocketSession? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState = _connectionState.asStateFlow()
    
    private val _incomingMessages = Channel<MessageEntity>(Channel.BUFFERED)
    val incomingMessages: Flow<MessageEntity> = flow {
        for (message in _incomingMessages) {
            emit(message)
        }
    }
    
    private var connectionJob: Job? = null
    
    sealed class ConnectionState {
        data object Connected : ConnectionState()
        data object Connecting : ConnectionState()
        data object Disconnected : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }
    
    /**
     * Connect to the WebSocket server
     * @param username User identifier for the connection
     */
    fun connect(username: String) {
        // Cancel any existing connection
        connectionJob?.cancel()
        
        _connectionState.value = ConnectionState.Connecting
        
        connectionJob = scope.launch {
            try {
                // Ensure RSA keys are generated
                keyManager.getOrGenerateRSAKeyPair()
                
                connectAndListen(username)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to connect: ${e.message}", e)
                _connectionState.value = ConnectionState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    private suspend fun connectAndListen(username: String) {
        var reconnectDelay = 1L
        
        while (scope.isActive) {
            try {
                webSocketSession = client.webSocketSession(
                    urlString = "wss://chat-server-h5u5.onrender.com/chat/$username"
                )
                
                _connectionState.value = ConnectionState.Connected
                Log.d(TAG, "Connected to WebSocket server")
                
                // Reset reconnect delay on successful connection
                reconnectDelay = 1L
                
                // Send public key to server
                val myPublicKey = keyManager.getRSAPublicKey()
                if (myPublicKey != null) {
                    val keyMessage = MessageEntity(
                        senderId = username,
                        receiversId = "all",
                        type = MessageType.KEY_EXCHANGE,
                        content = EncryptionUtils.publicKeyToString(myPublicKey)
                    )
                    sendMessage(keyMessage)
                }
                
                // Listen for incoming messages
                webSocketSession?.incoming?.consumeEach { frame ->
                    when (frame) {
                        is Frame.Text -> processTextFrame(frame, username)
                        is Frame.Binary -> processBinaryFrame(frame)
                        else -> { /* Ignore other frame types */ }
                    }
                }
                
                // If we reach here, the connection was closed normally
                Log.d(TAG, "WebSocket connection closed")
                break
                
            } catch (e: Exception) {
                Log.e(TAG, "WebSocket error: ${e.message}", e)
                _connectionState.value = ConnectionState.Error(e.message ?: "Unknown error")
                
                // Close session if it exists
                webSocketSession?.close(CloseReason(CloseReason.Codes.NORMAL, "Reconnecting"))
                webSocketSession = null
                
                // Exponential backoff for reconnection attempts (max 30s)
                delay((reconnectDelay.seconds.inWholeMilliseconds).coerceAtMost(30_000))
                reconnectDelay = (reconnectDelay * 2).coerceAtMost(30)
                
                _connectionState.value = ConnectionState.Connecting
            }
        }
        
        // Final cleanup if coroutine is cancelled
        _connectionState.value = ConnectionState.Disconnected
        try {
            webSocketSession?.close()
        } catch (e: Exception) {
            // Ignore exceptions during cleanup
        }
    }
    
    private suspend fun processTextFrame(frame: Frame.Text, currentUsername: String) {
        val text = frame.readText()
        try {
            val message = json.decodeFromString<MessageEntity>(text)
            
            when (message.type) {
                MessageType.KEY_EXCHANGE -> {
                    if (message.senderId != currentUsername) {
                        // Store the public key for the sender
                        message.content?.let { keyString ->
                            keyManager.saveUserPublicKey(message.senderId, keyString)
                            
                            // After receiving a public key, we can establish a secure channel
                            initiateSecureChannel(message.senderId, currentUsername)
                        }
                    }
                }
                
                MessageType.SECURE_CHANNEL_INIT -> {
                    if (message.receiversId == currentUsername) {
                        // Process secure channel initialization
                        processSecureChannelInit(message)
                    }
                }
                
                MessageType.TEXT, MessageType.IMAGE, MessageType.VIDEO, MessageType.FILE -> {
                    if (message.receiversId == currentUsername || message.receiversId == "all") {
                        // Try to decrypt the message
                        val decryptedMessage = decryptMessage(message)
                        _incomingMessages.send(decryptedMessage)
                    }
                }
                
                MessageType.SELF_DESTRUCT -> {
                    if (message.receiversId == currentUsername) {
                        // Process self-destruct message
                        val decryptedMessage = decryptMessage(message)
                        // Mark as self-destruct in the UI/database
                        _incomingMessages.send(decryptedMessage)
                    }
                }
                
                else -> {
                    // Handle other message types
                    _incomingMessages.send(message)
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing message: ${e.message}", e)
        }
    }
    
    private fun processBinaryFrame(frame: Frame.Binary) {
        // Handle binary data (files, images, etc.)
        Log.d(TAG, "Received binary frame: ${frame.data.size} bytes")
        
        // This would typically be used for file transfers
        // Implementation would depend on your file transfer protocol
    }
    
    /**
     * Send a message to the server
     * If the message is a text, image, or file, it will be encrypted
     */
    suspend fun sendMessage(message: MessageEntity) {
        val session = webSocketSession ?: return
        
        try {
            // Encrypt message if it's a regular message and we have the recipient's key
            val encryptedMessage = when (message.type) {
                MessageType.KEY_EXCHANGE, MessageType.SECURE_CHANNEL_INIT -> message
                else -> encryptMessage(message)
            }
            
            val jsonMessage = json.encodeToString(encryptedMessage)
            session.send(Frame.Text(jsonMessage))
            
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * Disconnect from the WebSocket server
     */
    suspend fun disconnect() {
        connectionJob?.cancel()
        webSocketSession?.close(CloseReason(CloseReason.Codes.NORMAL, "User disconnected"))
        webSocketSession = null
        _connectionState.value = ConnectionState.Disconnected
    }
    
    /**
     * Initiate a secure channel with another user
     * Generates and shares an AES key using the recipient's public key
     */
    private suspend fun initiateSecureChannel(recipientId: String, currentUserId: String) {
        val recipientPublicKey = keyManager.getUserPublicKey(recipientId) ?: return
        
        // Create a chat ID
        val chatId = generateChatId(currentUserId, recipientId)
        
        // Generate a new AES key for this chat
        val secretKey = keyManager.generateAndSaveChatKey(chatId)
        
        // Encrypt the AES key with the recipient's public key
        val encryptedKey = keyManager.encryptKeyForUser(secretKey, recipientPublicKey)
        
        // Send the encrypted key to the recipient
        val keyExchangeMessage = MessageEntity(
            senderId = currentUserId,
            receiversId = recipientId,
            chatId = chatId,
            type = MessageType.SECURE_CHANNEL_INIT,
            content = encryptedKey
        )
        
        sendMessage(keyExchangeMessage)
    }
    
    /**
     * Process a secure channel initialization message
     * Decrypts the shared AES key and saves it
     */
    private fun processSecureChannelInit(message: MessageEntity) {
        try {
            val encryptedKey = message.content ?: return
            val secretKey = keyManager.decryptSharedKey(encryptedKey)
            
            // Save the key for this chat
            val chatId = message.chatId.ifEmpty { 
                generateChatId(message.receiversId, message.senderId) 
            }
            
            keyManager.saveChatKey(chatId, EncryptionUtils.secretKeyToString(secretKey))
            
            Log.d(TAG, "Secure channel established with ${message.senderId}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to process secure channel init: ${e.message}", e)
        }
    }
    
    /**
     * Encrypt a message using AES for the specific chat
     */
    private fun encryptMessage(message: MessageEntity): MessageEntity {
        val chatId = message.chatId.ifEmpty { 
            generateChatId(message.senderId, message.receiversId) 
        }
        
        // Get the secret key for this chat
        val secretKey = keyManager.getChatKey(chatId) ?: return message
        
        // Encrypt the content
        val encryptedContent = message.content?.let {
            EncryptionUtils.encryptWithAES(it, secretKey)
        }
        
        return message.copy(
            content = encryptedContent,
            chatId = chatId
        )
    }
    
    /**
     * Decrypt a message using AES for the specific chat
     */
    private fun decryptMessage(message: MessageEntity): MessageEntity {
        val chatId = message.chatId.ifEmpty { 
            generateChatId(message.receiversId, message.senderId) 
        }
        
        // Get the secret key for this chat
        val secretKey = keyManager.getChatKey(chatId) ?: return message
        
        // Decrypt the content
        val decryptedContent = message.content?.let {
            try {
                EncryptionUtils.decryptWithAES(it, secretKey)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to decrypt message: ${e.message}", e)
                message.content // Return original content if decryption fails
            }
        }
        
        return message.copy(
            content = decryptedContent,
            chatId = chatId
        )
    }
    
    /**
     * Generate a consistent chat ID for two users
     */
    private fun generateChatId(user1: String, user2: String): String {
        val users = listOf(user1, user2).sorted()
        return "${users[0]}_${users[1]}"
    }
    
    /**
     * Send a file securely
     */
    suspend fun sendFile(file: File, recipient: String, messageType: MessageType) {
        val chatId = generateChatId(recipient, recipient)
        
        // Get the AES key for encryption
        val secretKey = keyManager.getChatKey(chatId) 
            ?: throw IllegalStateException("No secure channel established")
        
        // Create a message with file metadata
        val fileMessage = MessageEntity(
            senderId = recipient,
            receiversId = recipient,
            chatId = chatId,
            type = messageType,
            content = file.name // The file path will be encrypted
        )
        
        // Send the encrypted file message
        sendMessage(fileMessage)
        
        // Read and encrypt the file in chunks
        val fileBytes = file.readBytes()
        val encryptedData = EncryptionUtils.encryptWithAES(
            String(fileBytes), secretKey
        )
        
        // Send the encrypted file data
        val session = webSocketSession ?: return
        session.send(Frame.Binary(true, encryptedData.toByteArray()))
    }
    
    /**
     * Create a self-destructing message
     * @param content Message content
     * @param recipient Recipient of the message
     * @param timeToLiveSeconds Time after which the message should self-destruct
     */
    suspend fun sendSelfDestructMessage(
        content: String,
        recipient: String,
        timeToLiveSeconds: Long
    ) {
        val chatId = generateChatId(recipient, recipient)
        
        val message = MessageEntity(
            senderId = recipient,
            receiversId = recipient,
            chatId = chatId,
            type = MessageType.SELF_DESTRUCT,
            content = "$content|$timeToLiveSeconds" // Attach TTL to content
        )
        
        sendMessage(message)
    }
    
    companion object {
        private const val TAG = "SecureChatService"
    }
} 