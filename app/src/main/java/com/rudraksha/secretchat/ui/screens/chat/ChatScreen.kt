package com.rudraksha.secretchat.ui.screens.chat

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rudraksha.secretchat.data.model.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ChatScreen(
    chatName: String = "",
    username: String = "",
    sendMessage: (String) -> Unit = {},
    onNavIconClick: () -> Unit = {},
    receivers: String = "",
) {
    val messages = remember { mutableStateListOf<Message>() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = chatName) },
                navigationIcon = {
                    IconButton(onClick = onNavIconClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    reverseLayout = true,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    items(messages) { message ->
                        ChatBubble(message = message)
                    }
                }
                MessageInput(
                    sendMessage = sendMessage,
                    scope = scope
                )
            }
        }
    )
}

@Composable
fun ChatBubble(message: Message) {
    // Determine if the message was sent by the current user.
    // Here, we assume that the current user's ID/username is "default".
    // You might want to compare against a currentUserId from your repository.
    val isSent = message.senderId == "default"
    val bubbleColor = if (isSent) Color(0xFFDCF8C6) else Color.White
    val textColor = if (isSent) Color.Black else Color.Black
    val bubbleShape = if (isSent) {
        RoundedCornerShape(topStart = 12.dp, topEnd = 0.dp, bottomEnd = 12.dp, bottomStart = 12.dp)
    } else {
        RoundedCornerShape(topStart = 0.dp, topEnd = 12.dp, bottomEnd = 12.dp, bottomStart = 12.dp)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isSent) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .background(bubbleColor, shape = bubbleShape)
                .padding(8.dp)
        ) {
            Text(text = message.content ?: "", color = textColor)
            Spacer(modifier = Modifier.height(4.dp))
            // Display timestamp in a smaller font.
            Text(
                text = DateFormat.format("hh:mm a", message.timestamp).toString(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun MessageInput(sendMessage: (String) -> Unit, scope: CoroutineScope) {
    var content by remember { mutableStateOf("") }

    OutlinedTextField(
        value = content,
        onValueChange = { content = it },
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = MaterialTheme.shapes.large),
        singleLine = true,
        placeholder = { Text("Enter message to send") },
        trailingIcon = {
            IconButton(
                onClick = {
                    scope.launch {
                        sendMessage(content)
                        content = ""
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

