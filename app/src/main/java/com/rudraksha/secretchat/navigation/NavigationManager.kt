package com.rudraksha.secretchat.navigation

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rudraksha.secretchat.data.model.ChatEntity
import com.rudraksha.secretchat.data.model.ChatItem
import com.rudraksha.secretchat.data.model.ChatType
import com.rudraksha.secretchat.data.model.UserEntity
import com.rudraksha.secretchat.data.model.toChatItem
import com.rudraksha.secretchat.data.remote.WebSocketManager
import com.rudraksha.secretchat.ui.screens.authentication.LoginScreen
import com.rudraksha.secretchat.ui.screens.authentication.RegisterScreen
import com.rudraksha.secretchat.ui.screens.chat.ChatScreen
import com.rudraksha.secretchat.ui.screens.chat.InvisibleChatScreen
import com.rudraksha.secretchat.ui.screens.create.SelectMembersScreen
import com.rudraksha.secretchat.ui.screens.home.HomeScreen
import com.rudraksha.secretchat.viewmodels.AuthViewModel
import com.rudraksha.secretchat.viewmodels.MessagesViewModel
import com.rudraksha.secretchat.viewmodels.MessagesViewModelFactory
import com.rudraksha.secretchat.viewmodels.ChatListViewModel
import com.rudraksha.secretchat.viewmodels.InvisibleChatViewModel
import com.rudraksha.secretchat.viewmodels.InvisibleChatViewModelFactory
import com.rudraksha.secretchat.viewmodels.MiscellaneousViewModel
import com.rudraksha.secretchat.viewmodels.MiscellaneousViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow

@Composable
fun NavigationManager(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    context: Context,
) {
    val chatListViewModel: ChatListViewModel = viewModel()
    val currentUser by authViewModel.currentUserEntity.collectAsState()
    val chatList by chatListViewModel.chatEntityList.collectAsStateWithLifecycle()

    val pwd = "pwd"
    lateinit var webSocketManager: WebSocketManager

    // Ensure registered user is always fetched
    LaunchedEffect(Unit) {
        authViewModel.getCurrentUser()
        webSocketManager = WebSocketManager(username = currentUser?.username ?: "default", password = pwd)
        webSocketManager.connect()
    }

    NavHost(
        navController = navController,
        startDestination = Routes.Splash // Start with SplashScreen
    ) {
        composable<Routes.Splash> {
            SplashScreen(
                navigateToRegister = {
                    navController.navigate(Routes.Registration) {
                        popUpTo(Routes.Splash) { inclusive = true } // Prevent going back to Splash
                    }
                },
                navigateToHome = {
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                },
                observeCurrentUserEntity = authViewModel.currentUserEntity,
            )
        }

        composable<Routes.Registration> {
            RegisterScreen(
                register = authViewModel::register,
                observeRegisterState = authViewModel.authState,
                navigateToLogin = { navController.navigate(Routes.Login) },
                onRegisterSuccess = {
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Registration) { inclusive = true }
                    }
                }
            )
        }

        composable<Routes.Login> {
            LoginScreen(
                login = authViewModel::login,
                observeLoginState = authViewModel.authState,
                navigateToRegister = { navController.navigateUp() },
                onLoginSuccess = {
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Login) { inclusive = true }
                    }
                }
            )
        }

        composable<Routes.Home> {
            LaunchedEffect(Unit) {
                chatListViewModel.getAllChats() // Ensure chat list is always updated
                Log.d("ChatList", chatList.toString())
            }

            HomeScreen(
                navController = navController,
                onChatItemClick = { chatItem ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("chat", chatItem)
                    chatList.find { it.chatId == chatItem.id }?.let {
                        chatListViewModel.addChat(
                            it.copy(unreadCount = 0)
                        )
                    }
                    navController.navigate(Routes.Chat)
                },
                selectMembers = { chatType ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("chatType", chatType)
                    navController.navigate(Routes.SelectMembers)
                },
                chatList = chatList.map { it.toChatItem(currentUser?.username ?: "default") }
            )
        }

        composable<Routes.Chat> {
            val chat = navController.previousBackStackEntry?.savedStateHandle?.get<ChatItem>("chat")
            chat?.let {
                val messagesViewModel: MessagesViewModel = viewModel(
                    factory = MessagesViewModelFactory(
                        chatId = it.id, username = currentUser?.username ?: "default", webSocketManager = webSocketManager,
                        navController.context.applicationContext as android.app.Application
                    )
                )
                val chatDetailUiState = messagesViewModel.messagesUiState // Observing messages

                ChatScreen(
                    username = currentUser?.username ?: "default",
                    chatName = it.name,
                    sendMessage = { message: String ->
                        messagesViewModel.sendMessage(message, it)
                    },
                    navigateBack = { navController.navigateUp() },
                    observeMessagesUiState = chatDetailUiState,
                    onMessageReaction = { message, reaction ->
//                    chatDetailViewModel.reactToMessage(message, reaction)
                    },
                    updateAllMessages = messagesViewModel::updateAllMessages
                )
            }
        }

        composable<Routes.InvisibleChat> {
/*            enterTransition = {
                slideIn (
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioHighBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    initialOffset = { size ->
                        IntOffset(x = -size.height, y = -size.width)
                    }
                )
            },
            exitTransition = {
                slideOut(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioHighBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    targetOffset = { size ->
                        IntOffset(x = size.height, y = size.width)
                    }
                )
            }*/
            val invisibleChatViewModel: InvisibleChatViewModel = viewModel(
                factory = InvisibleChatViewModelFactory(
                    application = navController.context.applicationContext as android.app.Application,
                    webSocketManager = webSocketManager
                )
            )
            val invisibleChatUiState = invisibleChatViewModel.invisibleChatUiState
            val chat = invisibleChatUiState.collectAsStateWithLifecycle().value.chatEntity

            InvisibleChatScreen(
                username = currentUser?.username ?: "default",
                createChat = invisibleChatViewModel::createChat,
                sendMessage = { message: String, ->
                    Log.d("chat 0", chat.toString())
                    if (chat != null) {
                        invisibleChatViewModel.sendMessage(message)
                    }
                },
                navigateBack = { navController.navigateUp() },
                invisibleChatUiStateStateFlow = invisibleChatUiState,
            )
        }

        composable<Routes.SelectMembers> {
            val chatType = navController.previousBackStackEntry?.savedStateHandle
                ?.get<ChatType>("chatType") ?: ChatType.PRIVATE

            val miscellaneousViewModel: MiscellaneousViewModel = viewModel(
                factory = MiscellaneousViewModelFactory(
                    application = navController.context.applicationContext as android.app.Application,
                    webSocketManager = webSocketManager
                )
            )
            val miscellaneousUiState = miscellaneousViewModel.miscellaneousUiState.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) {
                miscellaneousViewModel.fetchUsers(currentUser?.username ?: "def")
                Log.d("NM LE", "${miscellaneousUiState.value.users}")
            }
            SelectMembersScreen(
                chatType = chatType,
                create = { users, name ->
                    Toast.makeText(context, "Creating chat $name $users", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun SplashScreen(
    navigateToRegister: () -> Unit,
    navigateToHome: () -> Unit,
    observeCurrentUserEntity: StateFlow<UserEntity?>,
) {
    val currentUser = observeCurrentUserEntity.collectAsState().value
    LaunchedEffect(currentUser) {
        delay(500) // Optional: Show splash for a bit
        if (currentUser == null) {
            navigateToRegister()
        } else {
            navigateToHome()
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Loading...", fontSize = 24.sp)
    }
}

//fun createChat(users: List<String>): Chat {
//    val chatId = createChatId(users)
//    // Check for the server
//    val chat = getChat(chatId)
//    if (chat != null) {
//        return chat
//    } else {
//        // Create a new chat
//
////        saveChat(newChat)
//    }
//}
//
//@Composable
//fun createChatDialog(
//    users: List<String>,
//    create(String) -> Unit
////    create(String, String, ChatType, String, String)-> Unit
//) {
//
//}
//
//fun getChat(chatId: String): Chat? {
//    return null
//}