package com.rudraksha.secretchat.ui.screens.chat

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rudraksha.secretchat.R
import com.rudraksha.secretchat.data.entity.MessageEntity
import com.rudraksha.secretchat.ui.components.MessageInput
import com.rudraksha.secretchat.ui.components.SearchBar
import com.rudraksha.secretchat.viewmodels.MessagesUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    username: String = "",
    chatName: String = "",
    sendMessage: (String) -> Unit,
    navigateBack: () -> Unit,
    observeMessagesUiState: StateFlow<MessagesUiState>,
    onMessageReaction: (MessageEntity, String) -> Unit = { _, _ -> },
    updateAllMessages: (List<MessageEntity>) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var showSearch by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var sheetHeight by remember { mutableStateOf(0.dp) }
    val animatedHeight by animateDpAsState(
        targetValue = 400.dp,
        animationSpec = tween(durationMillis = 700, easing = LinearOutSlowInEasing),
        label = "Bottom sheet height"
    )
    val chatDetailUiState by observeMessagesUiState.collectAsStateWithLifecycle()
    val messages by remember { derivedStateOf { chatDetailUiState.messageEntities } }
    val isConnected by remember { derivedStateOf { chatDetailUiState.isConnected } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (showSearch) {
                        SearchBar(
                            text = "Search chat",
                            leadingIcon = {
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
                            }
                        )
                    } else {
                        Text(
                            text = chatName,
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                },
                navigationIcon = {
                    if (!showSearch) {
                        IconButton(
                            onClick = {
                                navigateBack()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
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
                        sendMessage = sendMessage,
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
                            scope.launch { showBottomSheet = true }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
            }
        }

        if (showBottomSheet) {
            LaunchedEffect(Unit) {
                sheetHeight = animatedHeight
            }
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                    sheetHeight = 0.dp
                },
                sheetState = bottomSheetState,
                properties = ModalBottomSheetProperties(
                    shouldDismissOnBackPress = true,
                ),
                modifier = Modifier.height(sheetHeight)
            ) {
                BottomSheetContent {
                    scope.launch {
                        bottomSheetState.hide()
                        showBottomSheet = false
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        messages?.let { updateAllMessages(it) }
    }
}

@Preview
@Composable
fun ChatPreview() {
    val state = MutableStateFlow(listOf(
        MessageEntity(content = "a", senderId = "", timestamp = 1000000L),
        MessageEntity(content = "a", senderId = "", timestamp = 2000000L),
        MessageEntity(content = "a", senderId = "A", timestamp = 3000000L),
        MessageEntity(senderId = "", timestamp = 4000000L),
        MessageEntity(senderId = "A", timestamp = 8000000L),
        MessageEntity(senderId = "A", timestamp = 10000000L),
    ).sortedBy { it.timestamp }.reversed())

    var _messagesUiState = MutableStateFlow<MessagesUiState>(MessagesUiState())
    val messagesUiState: StateFlow<MessagesUiState> = _messagesUiState.asStateFlow()

    ChatScreen(
        username = "A",
        chatName = "Chat Name",
        sendMessage = {},
        navigateBack = {},
        observeMessagesUiState = messagesUiState,
        onMessageReaction = { _, _ ->}
    )
}