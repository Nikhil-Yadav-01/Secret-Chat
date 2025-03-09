package com.rudraksha.secretchat.ui.screens.chat

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rudraksha.secretchat.data.model.Message
import com.rudraksha.secretchat.data.remote.WebSocketManager
import com.rudraksha.secretchat.utils.createChatId
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun InvisibleChatScreen(
    username: String = "",
    onNavIconClick: () -> Unit = {},
    context: Context = LocalContext.current
) {
    var recipient by remember { mutableStateOf("@") }
    var message by remember { mutableStateOf("") }
    val webSocketManager: WebSocketManager = remember { WebSocketManager(username) }
//    val isConnected by webSocketManager.isConnected.collectAsState()
    val messages by webSocketManager.messages.collectAsState() // Observing messages
    val scope = rememberCoroutineScope()

    // Ensure WebSocket connects when screen opens
    LaunchedEffect(Unit) {
        webSocketManager.connect()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavIconClick) {
                        Icon(
                            Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                },
                title = {
                    OutlinedTextField(
                        value = recipient,
                        onValueChange = { recipient = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Recipient username:") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                    )
                }
            )
        },
        bottomBar = {
            NavigationBar {
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape = MaterialTheme.shapes.large),
                    singleLine = true,
                    placeholder = { Text("Enter message to send") },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
//                                    if (isConnected) {
                                        val sendMessage = Message(
                                            content = message,
                                            senderId = username,
                                            chatId = createChatId(listOf(username, recipient,)),
                                            receiversId = recipient
                                        )
                                        webSocketManager.sendMessage(sendMessage)
                                        message = ""
//                                    } else {
//                                        Toast.makeText(context, "Disconnected", Toast.LENGTH_SHORT)
//                                            .show()
//                                    }
                                }
                            },
                            modifier = Modifier.padding(start = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "send",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    },
                    shape = MaterialTheme.shapes.large
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(8.dp)
        ) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(messages) { msg ->
                    msg.content?.let {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                        ) {
                            Text(it, modifier = Modifier.padding(8.dp))
                        }
                    }
                }
            }
        }
    }
}
