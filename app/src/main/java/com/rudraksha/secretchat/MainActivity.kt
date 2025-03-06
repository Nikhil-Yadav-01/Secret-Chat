package com.rudraksha.secretchat

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.rudraksha.secretchat.data.model.Chat
import com.rudraksha.secretchat.data.model.Message
import com.rudraksha.secretchat.navigation.NavigationManager
import com.rudraksha.secretchat.navigation.Routes
import com.rudraksha.secretchat.ui.theme.SecretChatTheme
import com.rudraksha.secretchat.utils.createChatId
import com.rudraksha.secretchat.viewmodels.ChatDetailViewModel
import com.rudraksha.secretchat.viewmodels.ChatDetailViewModelFactory
import com.rudraksha.secretchat.viewmodels.ChatListViewModel
import com.rudraksha.secretchat.viewmodels.RegistrationViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SecretChatTheme {
                val navHostController = rememberNavController()
                NavigationManager(
                    navController = navHostController,
                    context = this,
                )
            }
        }
    }
}

@Composable
fun RegistrationScreen(
    navController: NavController, viewModel: RegistrationViewModel,
) {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Register", modifier = Modifier.padding(bottom = 16.dp))

        BasicTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray)
                .padding(8.dp),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        BasicTextField(
            value = username,
            onValueChange = { username = it },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray)
                .padding(8.dp),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        BasicTextField(
            value = fullName,
            onValueChange = { fullName = it },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray)
                .padding(8.dp),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if(email.isNotBlank() && username.isNotBlank() && fullName.isNotBlank()){
                viewModel.registerUser(email, username, fullName)

                navController.navigate(Routes.Home.route) {
                    popUpTo("registration") { inclusive = true }
                }
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Register")
        }
    }
}

@Composable
fun ChatListScreen(
    navController: NavController, chats: List<Chat>) {

    Text("List")
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(chats) { chat ->
            ChatListItem(chat = chat) {
                navController.navigate("chatDetail/${chat.id}")
            }
        }
    }
}

@Composable
fun ChatListItem(chat: Chat, onChatClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChatClick() }
            .padding(16.dp)
    ) {
        Text(text = chat.name ?: "Private Chat")
        Text(text = "Created by: ${chat.createdBy}")
    }
}

/*
@Composable
fun ChatDetailScreen(navController: NavController, chatId: String) {
    val viewModel: ChatDetailViewModel = viewModel(
        factory = ChatDetailViewModelFactory(chatId, navController.context.applicationContext as android.app.Application)
    )
    val messages by viewModel.messages.collectAsState()
    var messageText by remember { mutableStateOf(TextFieldValue("")) }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            reverseLayout = true,
            contentPadding = PaddingValues(8.dp)
        ) {
            items(messages) { message ->
                ChatBubble(message)
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            BasicTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier
                    .weight(1f)
                    .background(Color.LightGray)
                    .padding(8.dp)
            )
            Button(
                onClick = {
                    if (messageText.text.isNotBlank()) {
                        viewModel.sendMessage(messageText.text)
                        messageText = TextFieldValue("")
                    }
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Send")
            }
        }
    }
}*/

@Preview
@Composable
fun ChatBubble(message: Message = Message(senderId = "se")) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .background(if (message.senderId == "default") Color.Blue else Color.Gray)
            .padding(8.dp),
        contentAlignment = if (message.senderId == "default") Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Text(text = message.content ?: "", color = Color.White)
    }
}
