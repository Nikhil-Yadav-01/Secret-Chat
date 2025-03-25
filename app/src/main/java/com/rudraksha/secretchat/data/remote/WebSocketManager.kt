package com.rudraksha.secretchat.data.remote

import android.util.Log
import com.rudraksha.secretchat.data.WebSocketData
import com.rudraksha.secretchat.data.model.FileMetadata
import com.rudraksha.secretchat.data.webSocketDataModule
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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class WebSocketManager(
    val username: String = "default",
    private val password: String = "default",
) {
    private val client = HttpClient(OkHttp) {
        engine { }
        install(WebSockets) { }
    }
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
        allowStructuredMapKeys = true
        encodeDefaults = true
        classDiscriminator = "type" // Use "type" as the discriminator
        serializersModule = webSocketDataModule
    }
    private var webSocketSession: WebSocketSession? = null
    private var onDataReceived: ((WebSocketData) -> Unit)? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
//    private var connectionJob: Job? = null

    fun connect() {
        scope.launch {
            var retryDelay = 5000L // Initial retry delay (5s)

            while (isActive) { // Keep retrying while coroutine is active
                try {
                    // Close previous session if active
                    webSocketSession?.let {
                        if (it.isActive) {
                            Log.d("WebSocket", "🔴 Closing existing WebSocket session")
                            it.close()
                        }
                    }

                    // Establish new WebSocket connection
                    Log.d("WebSocket", "🌐 Attempting to connect to WebSocket...")
                    webSocketSession = client.webSocketSession(
//                        host = HOST, port = PORT, path = "chat/${username}/${password}"
                        urlString = "wss://chat-server-h5u5.onrender.com/chat/${username}/${password}"
                    )

                    _isConnected.value = true
                    Log.d("WebSocket", "✅ WebSocket connected successfully!")
                    retryDelay = 5000L // Reset retry delay on success

                    // Process incoming messages
                    for (frame in webSocketSession!!.incoming) {
                        when (frame) {
                            is Frame.Text -> {
                                val receivedText = frame.readText()
                                Log.d("WebSocket", "📥 Received RAW JSON: $receivedText")

                                try {
                                    val data = json.decodeFromString<WebSocketData>(receivedText)

                                    when (data) {
                                        is WebSocketData.ConnectionStatus -> {
                                            _isConnected.value = data.status
                                        }
                                        is WebSocketData.Message -> {
                                            onDataReceived?.invoke(data)
                                            Log.d("WebSocket", "💬 Message received: ${data.content}")
                                        }
                                        is WebSocketData.Acknowledgment -> {
                                            Log.d("WebSocket", "✔️ Acknowledgment: ${data.messageId} - ${data.status}")
                                        }
                                        is WebSocketData.Error -> {
                                            Log.e("WebSocket", "❌ Server Error: ${data.errorMessage}")
                                        }
                                        is WebSocketData.GetUsers -> {
                                            Log.e("WebSocket", "GetUsersr: ${data.user}")
                                        }
                                        is WebSocketData.UserList -> {
                                            Log.d("WebSocket", "User List: ${data.users}")
                                            onDataReceived?.invoke(data)
                                        }
                                        is WebSocketData.ChatList -> {
                                            Log.d("WebSocket", "Chat List ${data.chats}")
                                        }
                                        else -> Log.d("WebSocket", "ℹ️ Received unhandled WebSocketData type")
                                    }
                                } catch (e: SerializationException) {
                                    Log.e("WebSocket", "🚨 JSON Parsing Error: ${e.message}")
                                }
                            }
                            is Frame.Binary -> Log.d("WebSocket", "📦 Binary data received (size: ${frame.data.size} bytes)")

                            else -> Log.w("WebSocket", "⚠️ Unknown frame type received")
                        }
                    }

                    Log.d("WebSocket", "🔴 Connection closed gracefully, stopping reconnection attempts")
                    break // Stop retrying if the connection was closed properly

                } catch (e: Exception) {
                    Log.e("WebSocket", "❌ Connection error: $e")
                    _isConnected.value = false

                    if (isActive) {
                        Log.d("WebSocket", "⏳ Retrying in ${retryDelay / 1000} seconds...")
                        delay(retryDelay)
                        retryDelay = minOf(retryDelay * 2, 60000L) // Exponential backoff (max 60s)
                    }
                } finally {
                    webSocketSession?.let {
                        if (it.isActive) it.close()
                        _isConnected.value = false
                    }
                }
            }

            Log.d("WebSocket", "❌ Connection loop exited, WebSocket is disconnected")
            _isConnected.value = false
        }
    }

    fun sendData(data: WebSocketData) {
        scope.launch {
            webSocketSession?.let {
                val jsonMessage = json.encodeToString(data)
                if (it.isActive) {
                    it.send(Frame.Text(jsonMessage))
                    Log.d("Sent", jsonMessage)
                } else {
                    Log.e("WebSocket", "Cannot send message: WebSocket is closed")
                }
            }
        }
    }

    fun setOnDataReceivedListener(listener: (WebSocketData) -> Unit) {
        onDataReceived = listener
    }

    suspend fun sendFile(file: File, recipient: String) {
        val fileSize = file.length()
        val chunkSize = 1024 * 64 // 64 KB per chunk
        val totalChunks = (fileSize + chunkSize - 1) / chunkSize
        val fileMetadata = FileMetadata(
            fileName = file.name,
            fileType = file.extension,
            fileSize = fileSize,
            totalChunks = totalChunks
        )

        // Send metadata first as a JSON message
//        webSocketSession?.send(
//            Frame.Text(
//                Json.encodeToString(
//                    Message(
//                        senderId = username,
//                        receiversId = listOf(recipient).joinToString(","),
//                        content = Json.encodeToString(fileMetadata),
//                    )
//                )
//            )
//        )

        // Read the file in chunks and send via WebSocket
        file.inputStream().use { inputStream ->
            val buffer = ByteArray(chunkSize)
            var bytesRead: Int
            var chunkIndex = 0

            while ( inputStream.read(buffer).also { bytesRead = it } != -1) {
                chunkIndex++
                val chunkData = buffer.copyOf(bytesRead)
                webSocketSession?.send(Frame.Binary(true, chunkData))
                println("📤 Sent chunk $chunkIndex/$totalChunks")
            }
        }
    }

    suspend fun disconnect() {
//        connectionJob?.cancel()
        webSocketSession?.close(
            CloseReason(CloseReason.Codes.NORMAL, "Disconnecting the client")
        )
    }
}

