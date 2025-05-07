package com.rudraksha.secretchat.ui.screens.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rudraksha.secretchat.R
import com.rudraksha.secretchat.ui.components.MessageInput
import com.rudraksha.secretchat.viewmodels.InvisibleChatUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/*
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun InvisibleChatScreen(
    chat: Chat = Chat(
        createdBy = "Default"
    ),
    username: String = "",
    onNavIconClick: () -> Unit = {},
    context: Context = LocalContext.current
) {
    var recipient by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val webSocketManager: WebSocketManager = remember { WebSocketManager(username) }
//    val isConnected by webSocketManager.isConnected.collectAsState()
    val messages by webSocketManager.messages.collectAsState() // Observing messages
    val scope = rememberCoroutineScope()

    val currentUsername by rememberUpdatedState(username)
    // Ensure WebSocket connects when screen open
    LaunchedEffect(Unit, currentUsername) {
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
                                    if (recipient.isBlank() || recipient == "@") {
                                        Toast.makeText(context, "Enter a valid recipient!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val sendMessage = WebSocketData.Message(
                                            content = message,
                                            sender = username,
                                            receivers = listOf( recipient),
                                            chatId = chat.chatId
                                        )
                                        webSocketManager.sendData(sendMessage)
                                        message = ""
                                    }

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
    }*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvisibleChatScreen(
    username: String = "",
    sendMessage: (String) -> Unit,
    navigateBack: () -> Unit,
    createChat: (List<String>) -> Unit,
    invisibleChatUiStateStateFlow: StateFlow<InvisibleChatUiState>,
) {
    val scope = rememberCoroutineScope()
    var recipient by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val chatDetailUiState by invisibleChatUiStateStateFlow.collectAsStateWithLifecycle()
    val messages by remember { derivedStateOf { chatDetailUiState.messageEntities } }
    val isConnected by remember { derivedStateOf { chatDetailUiState.isConnected } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = recipient,
                        onValueChange = { recipient = it },
                        placeholder = {
                            Text(
                                text = "Enter recipient username",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(percent = 50))
                            .background(MaterialTheme.colorScheme.surfaceContainer),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (showSearch) showSearch = false
                            else navigateBack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    if (!showSearch) {
                        IconButton(onClick = {
                            showSearch = true
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "search"
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.Transparent,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    MessageInput(
                        sendMessage = {
                            scope.launch {
                                if (chatDetailUiState.chatEntity == null)
                                    createChat(listOf(recipient))
                                sendMessage(it)
                            }
                        } ,
                        scope = scope
                    )
                    if (!isConnected) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(percent = 50))
                                .background(MaterialTheme.colorScheme.errorContainer),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.WifiOff,
                                modifier = Modifier.size(16.dp),
                                contentDescription = null
                            )
                            Text("Not connected to server")
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painterResource(R.drawable.profile_pic),
                contentDescription = "Chat background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                reverseLayout = true,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                messages?.let { it ->
                    items(it) { message ->
                        ChatBubble(
                            messageEntity = message,
                            byMe = message.senderId == username
                        )
                    }
                }

                item {
                    EncryptionNotice(
                        text = "Messages and calls are end-to-end encrypted.",
                        onLearnMoreClicked = {
                            scope.launch {
//                                showBottomSheet = true
                                bottomSheetState.show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
            }

            if (bottomSheetState.isVisible) {
                ModalBottomSheet(
                    onDismissRequest = {
//                        showBottomSheet = false
                    },
                    sheetState = bottomSheetState,
                    properties = ModalBottomSheetProperties(
                        shouldDismissOnBackPress = true,
                    ),
                    modifier = Modifier.wrapContentHeight()
                ) {
                    BottomSheetContent {
                        scope.launch {
                            bottomSheetState.hide()
//                            showBottomSheet = false
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun InvisibleChatPreview() {
    val _chatDetailUiState = MutableStateFlow(InvisibleChatUiState())
    val chatDetailUiState: StateFlow<InvisibleChatUiState> = _chatDetailUiState.asStateFlow()

    InvisibleChatScreen(
        username = "A",
        sendMessage = {},
        navigateBack = {},
        createChat = {},
        invisibleChatUiStateStateFlow = chatDetailUiState,
    )
}