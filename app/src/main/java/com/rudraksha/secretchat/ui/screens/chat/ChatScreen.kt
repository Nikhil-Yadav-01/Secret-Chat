package com.rudraksha.secretchat.ui.screens.chat

import android.widget.Toast
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rudraksha.secretchat.R
import com.rudraksha.secretchat.data.model.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    username: String = "",
    chatName: String = "",
    sendMessage: (String) -> Unit,
    onNavIconClick: () -> Unit,
    messages: State<List<Message>?>,
    onMessageReaction: (Message, String) -> Unit = { _, _ -> },
) {
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    var sheetHeight by remember { mutableStateOf(0.dp) }
    val animatedHeight by animateDpAsState(
        targetValue = 400.dp,
        animationSpec = tween(durationMillis = 700, easing = LinearOutSlowInEasing),
        label = "Bottom sheet height"
    )
    val context = LocalContext.current

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
        bottomBar = {
            NavigationBar(
                containerColor = Color.Transparent,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                MessageInput(
                    sendMessage = sendMessage,
                    scope = scope
                )
            }
        },
//        contentWindowInsets = WindowInsets.ime,
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
            ) {/*
                messages.value?.let { it ->
                    items(it) { message ->
                        if (message.senderId == "Server" && message.content != null) {
                            Toast.makeText(
                                context, message.content, Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            ChatBubble(
                                message = message,
                                byMe = message.senderId == username
                            )
                        }
                    }
                }*/
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
}

@Preview
@Composable
fun ChatPreview() {
    val state = MutableStateFlow(listOf(
        Message(content = "a", senderId = "", timestamp = 1000000L),
        Message(content = "a", senderId = "", timestamp = 2000000L),
        Message(content = "a", senderId = "A", timestamp = 3000000L),
        Message(senderId = "", timestamp = 4000000L),
        Message(senderId = "A", timestamp = 8000000L),
        Message(senderId = "A", timestamp = 10000000L),
    ).sortedBy { it.timestamp }.reversed())

    ChatScreen(
        username = "A",
        chatName = "Chat Name",
        sendMessage = {},
        onNavIconClick = {},
        messages = state.collectAsState(),
        onMessageReaction = { _, _ ->}
    )
}