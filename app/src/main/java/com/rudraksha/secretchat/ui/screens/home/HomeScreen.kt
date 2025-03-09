package com.rudraksha.secretchat.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.EnhancedEncryption
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.rudraksha.secretchat.R
import com.rudraksha.secretchat.data.model.Chat
import com.rudraksha.secretchat.data.model.ChatItem
import com.rudraksha.secretchat.navigation.Routes
import com.rudraksha.secretchat.ui.screens.common.BottomNavigationBar
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onChatItemClick: (String) -> Unit = {},
    chatList: List<ChatItem> = getChatList(),
    navController: NavController = rememberNavController(),
    sendJoinRequest: (JoinRequest) -> Unit = {},
    acceptJoinRequest: (JoinResponse) -> Unit = {},
    rejectJoinRequest: (JoinResponse) -> Unit = {}
) {
    var longPressed by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun dismissDialog() {
        scope.launch { showDialog = false }
    }
    fun launchDialog() {
        scope.launch { showDialog = true }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Secret Chat", color = MaterialTheme.colorScheme.onBackground) },
                actions = {
                    if (longPressed) {
                        IconButton(onClick = { /* Pin action */ }) {
                            Icon(
                                Icons.Default.PushPin,
                                contentDescription = "Pin",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = { navController.navigate(Routes.InvisibleChat.route) }) {
                        Box(
                            modifier = Modifier.wrapContentSize(),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Icon(
                                Icons.Default.EnhancedEncryption,
                                contentDescription = "Invisible",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                Icons.Default.VisibilityOff,
                                contentDescription = "Invisible",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.alpha(0.5f)
                            )
                        }
                    }
                    IconButton(onClick = { /* Camera action */ }) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Camera",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { /* More options */ }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        bottomBar = { BottomNavigationBar() },
        floatingActionButton = {
            // Floating Action Button
            FloatingActionButton(
                onClick = { launchDialog() },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Icon(
                    Icons.Default.PersonAdd,
                    contentDescription = "New Chat",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        floatingActionButtonPosition = if (chatList.size == 1) FabPosition.Center else FabPosition.End
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            // Chat List
            if (showDialog) {
                SendChatRequest(
                    sendRequest = {
                        scope.launch {
                            sendJoinRequest(it)
                        }
                    },
                    dismissDialog = { dismissDialog() }
                )
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
                    .alpha(if (showDialog) 0.7f else 1f)
            ) {
                item {
                    SearchBar()// Search Bar
                }

                items(chatList) { chat ->
                    ChatRow(
                        chat = chat,
                        delete = { /* Handle delete action */ },
                        onClick = onChatItemClick,
                        onLongPress = { longPressed = !longPressed }
                    )
                }
                if (chatList.size == 1) {
                    item {
                        // Show message to join chat
                        Box(
                            modifier = Modifier
                                .heightIn(max = 400.dp)
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Join a chat to start messaging",
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getChatList() = listOf(
    ChatItem(
        id = UUID.randomUUID().toString(),
        "+91 94553 66424 (You)",
        "import androidx.compose.foundati...",
        "Yesterday",
        0,
        R.drawable.profile_pic
    ),
    ChatItem(
        id = UUID.randomUUID().toString(),
        "Coding Club India : Coder",
        "Job At Zomato Success St...",
        "09:46",
        6,
        R.drawable.profile_pic
    ),
    ChatItem(
        id = UUID.randomUUID().toString(),
        "Sandhya 215",
        "📷 Photo",
        "07:16",
        1,
        R.drawable.profile_pic
    ),
    ChatItem(
        id = UUID.randomUUID().toString(),
        "DAD",
        "📄 SUBHASH CHAND - CV.doc",
        "Yesterday",
        0,
        R.drawable.profile_pic
    ),
    ChatItem(
        id = UUID.randomUUID().toString(),
        "Amresh Thailand",
        "❌ Missed voice call",
        "Yesterday",
        0,
        R.drawable.profile_pic
    ),
    ChatItem(
        id = UUID.randomUUID().toString(),
        "LetsUpgrade Community",
        "📢 Help Us Build the...",
        "Yesterday",
        1,
        R.drawable.profile_pic
    ),
    ChatItem(
        id = UUID.randomUUID().toString(),
        "Ujjwal Jr",
        "👍 You reacted to \"Audio\"",
        "Yesterday",
        0,
        R.drawable.profile_pic
    ),
    ChatItem(
        id = UUID.randomUUID().toString(),
        "Access Denied Official",
        "Priyanka: Yes",
        "Yesterday",
        3,
        R.drawable.profile_pic
    )
)