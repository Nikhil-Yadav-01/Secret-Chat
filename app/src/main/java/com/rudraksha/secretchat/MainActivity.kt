package com.rudraksha.secretchat

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.rudraksha.secretchat.data.model.FileMetadata
import com.rudraksha.secretchat.data.model.MessageEntity
import com.rudraksha.secretchat.navigation.NavigationManager
import com.rudraksha.secretchat.ui.theme.SecretChatTheme
import com.rudraksha.secretchat.viewmodels.AuthViewModel
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class ChatClient(
    private val username: String = "default",
    private val messageEntities: MutableList<MessageEntity> = mutableListOf()
) {
    private val client = HttpClient(OkHttp) {
        engine {
//            this.proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress(HOST, PORT))
        }
        install(WebSockets) {
        }
    }
    private var webSocketSession: WebSocketSession? = null
    private var onMessageReceived: ((MessageEntity) -> Unit)? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    var isConnected = MutableStateFlow(false)
    private var connectionJob: Job? = null

    fun connect(username: String = this.username, messageEntities: MutableList<MessageEntity> = this.messageEntities) {
        // Cancel any existing connection job
        connectionJob?.cancel()

        connectionJob = scope.launch {
            isConnected.update { true } // Update isConnected to true when the coroutine starts
            try {
                while (this.isActive) { // Use isActive to check if the coroutine is still active
                    try {
                        webSocketSession = client.webSocketSession(
                            urlString = "wss://chat-server-h5u5.onrender.com/chat/${username}"
                        )

                        // Only one coroutine to handle incoming messages
                        webSocketSession?.incoming?.consumeEach { frame ->
                            when (frame) {
                                is Frame.Text -> {
                                    val messageEntity = Json.decodeFromString<MessageEntity>(frame.readText())
                                    messageEntities.add(messageEntity)
                                    onMessageReceived?.invoke(messageEntity)
                                }
                                is Frame.Binary -> {
                                    // Handle binary frames if needed
                                }
                                else -> {
                                    // Handle other frame types if needed
                                }
                            }
                        }
                        // If we reach here, the connection was closed gracefully
                        Log.d("WebSocket", "Connection closed gracefully")
                        break // Exit the loop if the connection is closed gracefully
                    } catch (e: Exception) {
                        e.localizedMessage?.let { Log.e("Exception", it) }
                        isConnected.update { false }
                        // Check if the coroutine is still active before retrying
                        if (isActive) {
                            delay(5000) // Retry connection
                        }
                    } finally {
                        // Ensure the session is closed in any case
                        webSocketSession?.close()
                    }
                }
            } finally {
                isConnected.update { false } // Update isConnected to false when the coroutine ends
            }
        }
    }

    fun sendMessage(messageEntity: MessageEntity) {
        scope.launch {
            val jsonMessage = Json.encodeToString(messageEntity)
            webSocketSession?.send(Frame.Text(jsonMessage))
        }
    }

    fun setOnMessageReceivedListener(listener: (MessageEntity) -> Unit) {
        onMessageReceived = listener
    }

    private fun receiveMessage(messageEntities: MutableList<MessageEntity>) {
        scope.launch {
            webSocketSession?.incoming?.consumeEach { frame ->
                when (frame) {
                    is Frame.Text -> {
                        val messageEntity = Json.decodeFromString<MessageEntity>(frame.readText())
                        messageEntities.add(messageEntity)
                    }
                    else -> {
                    }
                }
            }
        }
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
                    MessageEntity(
                        senderId = userName,
                        receiversId = recipient,
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
        connectionJob?.cancel()
        webSocketSession?.close(
            CloseReason(CloseReason.Codes.NORMAL, "Disconnecting the client")
        )
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SecretChatTheme {
                val navHostController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()
                NavigationManager(
                    navController = navHostController,
                    authViewModel = authViewModel,
                    context = this,
                )
            }
        }
    }
}
var userName: String = ""

@Composable
fun ChatApp() {
    var username by remember { mutableStateOf("") }
    var recipient by remember { mutableStateOf("") }
    val messageEntities = remember { mutableStateListOf<MessageEntity>() }
    val chatClient = remember { ChatClient() }
    val isConnected by chatClient.isConnected.collectAsState()

    if (!isConnected) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            TextField(
                value = username,
                onValueChange = { username = it },
                placeholder = { Text("Enter your username") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        CoroutineScope(Dispatchers.Default).launch {
                            chatClient.connect(
                                username,
                                messageEntities
                            )
                        }
                    }
                )
            )

            Button(
                onClick = {
                    CoroutineScope(Dispatchers.Default).launch {
                        chatClient.connect(
                            username,
                            messageEntities
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("Connect")
            }
        }
    } else {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = recipient,
                onValueChange = { recipient = it },
                placeholder = { Text("Enter recipient username") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            )

            val message = remember { mutableStateOf("") }
            TextField(
                value = message.value,
                onValueChange = { message.value = it },
                placeholder = { Text("Enter message") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    chatClient.sendMessage(
                        MessageEntity(
                            content = message.value,
                            senderId = username,
                            receiversId = recipient
                        )
                    )
                    message.value = ""
                })
            )

            Button(
                onClick = {
                    userName = username
                    chatClient.sendMessage(
                        MessageEntity(
                            content = message.value,
                            senderId = username,
                            receiversId = recipient
                        )
                    )
                    message.value = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("Send")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Chat Messages:")
            messageEntities.forEach { msg ->
                Text("${msg.senderId}: ${msg.content}")
            }
        }
    }
}

@Preview
@Composable
fun ChatBubble(messageEntity: MessageEntity = MessageEntity(senderId = "se")) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .background(if (messageEntity.senderId == "default") Color.Blue else Color.Gray)
            .padding(8.dp),
        contentAlignment = if (messageEntity.senderId == "default") Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Text(text = messageEntity.content ?: "", color = Color.White)
    }
}
