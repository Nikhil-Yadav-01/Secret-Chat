package com.rudraksha.secretchat.data.remote

import android.util.Log
import com.rudraksha.secretchat.HOST
import com.rudraksha.secretchat.PORT
import com.rudraksha.secretchat.data.model.FileMetadata
import com.rudraksha.secretchat.data.model.Message
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
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class WebSocketManager(
    val username: String = "default",
) {
    private val client = HttpClient(OkHttp) {
        engine {
//            this.proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress(HOST, PORT))
        }
        install(WebSockets) {
        }
    }
    private var webSocketSession: WebSocketSession? = null
    private var onMessageReceived: ((Message) -> Unit)? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

//    private val _isConnected = MutableStateFlow(false)
//    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
//    private var connectionJob: Job? = null

//    private val _serverStatus = MutableStateFlow("Checking server...")
//    val serverStatus = _serverStatus.asStateFlow()

    fun connect() {
//        connectionJob?.cancel() // Cancel existing connection

        scope.launch {
            var retryDelay = 5000L // Initial retry delay

            while (isActive) { // Keep retrying while coroutine is active
                try {
                    webSocketSession = client.webSocketSession(
                        urlString = "wss://chat-server-h5u5.onrender.com/chat/${username}"
                    )

                    webSocketSession?.incoming?.consumeEach { frame ->
                        when (frame) {
                            is Frame.Text -> {
                                val message = Json.decodeFromString<Message>(frame.readText())
                                _messages.value += message // Correct StateFlow update
                                onMessageReceived?.let { it(message) }
                                Log.d("ReceIncoming", message.toString())
                            }
                            is Frame.Binary -> {
                                // Handle binary messages if needed
                            }
                            else -> {
                                // Handle other frame types
                            }
                        }
                    }

                    Log.d("WebSocket", "Connection closed gracefully")
                    break // Stop retrying if the connection was closed properly

                } catch (e: Exception) {
                    Log.e("WebSocket", "Connection failed: ${e.localizedMessage}")
//                    _isConnected.value = false

                    if (isActive) {
                        delay(retryDelay)
                        retryDelay = minOf(retryDelay * 2, 60000L) // Exponential backoff (max 60s)
                    }
                } finally {
                    webSocketSession?.let {
                        if (it.isActive) it.close()
                    }
                }
            }

//            _isConnected.value = false
        }
    }


    fun sendMessage(message: Message) {
        scope.launch {
            val jsonMessage = Json.encodeToString(message)
            webSocketSession?.send(Frame.Text(jsonMessage))
            Log.d("Sent", jsonMessage)
        }
    }

    fun setOnMessageReceivedListener(listener: (Message) -> Unit) {
        onMessageReceived = listener
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
        webSocketSession?.send(
            Frame.Text(
                Json.encodeToString(
                    Message(
                        senderId = username,
                        receiversId = listOf(recipient).joinToString(","),
                        content = Json.encodeToString(fileMetadata),
                    )
                )
            )
        )

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

