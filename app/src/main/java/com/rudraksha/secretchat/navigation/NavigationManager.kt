package com.rudraksha.secretchat.navigation

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rudraksha.secretchat.data.model.Chat
import com.rudraksha.secretchat.data.model.ChatItem
import com.rudraksha.secretchat.data.model.Message
import com.rudraksha.secretchat.data.model.User
import com.rudraksha.secretchat.data.model.toChatItem
import com.rudraksha.secretchat.ui.screens.authentication.LoginScreen
import com.rudraksha.secretchat.ui.screens.authentication.RegisterScreen
import com.rudraksha.secretchat.ui.screens.chat.ChatScreen
import com.rudraksha.secretchat.ui.screens.chat.InvisibleChatScreen
import com.rudraksha.secretchat.ui.screens.home.HomeScreen
import com.rudraksha.secretchat.viewmodels.AuthViewModel
import com.rudraksha.secretchat.utils.createChatId
import com.rudraksha.secretchat.viewmodels.ChatDetailViewModel
import com.rudraksha.secretchat.viewmodels.ChatDetailViewModelFactory
import com.rudraksha.secretchat.viewmodels.ChatListViewModel
import com.rudraksha.secretchat.viewmodels.InvisibleChatViewModel
import com.rudraksha.secretchat.viewmodels.InvisibleChatViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

@Composable
fun NavigationManager(
    navController: NavHostController,
    context: Context,
) {
    val chatListViewModel: ChatListViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()

    val currentUser by authViewModel.currentUser.collectAsState()
    val chatList by chatListViewModel.chatList.collectAsStateWithLifecycle()

    // Ensure registered user is always fetched
    LaunchedEffect(Unit) {
        authViewModel.getCurrentUser()
    }

    NavHost(
        navController = navController,
        startDestination = Routes.Splash.route // Start with SplashScreen
    ) {
        composable(Routes.Splash.route) {
            SplashScreen(
                navigateToRegister = {
                    navController.navigate(Routes.Registration.route) {
                        popUpTo(Routes.Splash.route) { inclusive = true } // Prevent going back to Splash
                    }
                },
                navigateToHome = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                },
                observeCurrentUser = authViewModel.currentUser,
            )
        }

        composable(Routes.Registration.route) {
            RegisterScreen(
                register = authViewModel::register,
                observeRegisterState = authViewModel.registerState.collectAsStateWithLifecycle(),
                onNavigateToLogin = { navController.navigate(Routes.Login.route) },
                onRegisterSuccess = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Registration.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Login.route) {
            LoginScreen(
                login = authViewModel::login,
                observeLoginState = authViewModel.loginState.collectAsStateWithLifecycle(),
                navigateToRegister = { navController.navigateUp() },
                onLoginSuccess = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Home.route) {
            LaunchedEffect(Unit) {
                chatListViewModel.getAllChats() // Ensure chat list is always updated
                Log.d("ChatList", chatList.toString())
            }

            HomeScreen(
                navController = navController,
                onChatItemClick = { chatId ->
                    Log.d("ChatItemClick", chatId)
                    chatList.find { it.chatId == chatId }?.let {
                        chatListViewModel.addChat(
                            it.copy( unreadCount = 0 )
                        )
//                        navController.currentBackStackEntry?.savedStateHandle?.set("chat", it.toChatItem())
                    }
                    navController.navigate("${Routes.Chat.route}/$chatId")
                },
                chatList = chatList.map { it.toChatItem() }
            )
        }

        composable(
            route = "${Routes.Chat.route}/{chatId}"
        ) { backStackEntry ->
            val chatFound = backStackEntry.savedStateHandle.get<ChatItem>("chat")
            Log.d("chatFound", chatFound.toString())
            val chatId = backStackEntry.arguments?.getString("chatId") ?: "not found"
            val chatDetailViewModel: ChatDetailViewModel = viewModel(
                factory = ChatDetailViewModelFactory(
                    chatId = chatId, username = currentUser?.username ?: "default",
                    navController.context.applicationContext as android.app.Application
                )
            )
            Log.d("gdgdh", currentUser?.username ?: "default")
            val chat = chatList.find { it.chatId == chatId }
//            Log.d("chatId received", chat.toString())
            val chatDetailUiState = chatDetailViewModel.chatDetailUiState // Observing messages

            ChatScreen(
                username = currentUser?.username ?: "default",
                chatName = chat?.name ?: "Default Chat",
                sendMessage = { message: String ->
                    if (chat == null) Log.d("Chat is", chat.toString())
                    chat?.let { chatDetailViewModel.sendMessage(message, it) }
                },
                onNavIconClick = {
                    navController.navigateUp()
                },
                chatDetailUiState = chatDetailUiState.collectAsStateWithLifecycle(),
                onMessageReaction = { message, reaction ->
//                    chatDetailViewModel.reactToMessage(message, reaction)
                },
                updateAllMessages = chatDetailViewModel::updateAllMessages
            )
        }

        composable(
            route = Routes.InvisibleChat.route,
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
        ) {
            val invisibleChatViewModel: InvisibleChatViewModel = viewModel(
                factory = InvisibleChatViewModelFactory(
                    username = currentUser?.username ?: "default",
                    navController.context.applicationContext as android.app.Application
                )
            )

//            val webSocketManager: WebSocketManager = remember {
//                WebSocketManager(currentUser?.username ?: "default")
//            }
//            Log.d("WSMNav", webSocketManager.username)
//            val messages = webSocketManager.messages.collectAsState() // Observing messages
//            LaunchedEffect(Unit) {
//                webSocketManager.connect()
//            }

            InvisibleChatScreen(
                chat = Chat(createdBy = currentUser?.username ?: "default"),
//                messages = messages,
                username = currentUser?.username ?: "default",
//                sendMessage = { message: String, chatId: String ->
//                    invisibleChatViewModel.sendMessage(message, chatId)
//                },
                onNavIconClick = {
                    navController.navigateUp()
                },
                context = context
            )
        }
        /*
        chat = Chat(
            chatId = webSocketData.chatId,
            lastMessage = webSocketData.content ?: "",
            createdBy = webSocketData.sender,
            time = webSocketData.timestamp,
        )
        chatDao.insertChat(chat)
        */
    }
}

@Composable
fun SplashScreen(
    navigateToRegister: () -> Unit,
    navigateToHome: () -> Unit,
    observeCurrentUser: StateFlow<User?>,
) {
    val currentUser = observeCurrentUser.collectAsState().value
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
