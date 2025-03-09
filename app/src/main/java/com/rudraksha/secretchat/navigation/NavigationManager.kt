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
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rudraksha.secretchat.data.model.Chat
import com.rudraksha.secretchat.data.model.User
import com.rudraksha.secretchat.data.model.toChatItem
import com.rudraksha.secretchat.data.remote.WebSocketManager
import com.rudraksha.secretchat.ui.screens.authentication.LoginScreen
import com.rudraksha.secretchat.ui.screens.authentication.RegisterScreen
import com.rudraksha.secretchat.ui.screens.chat.ChatScreen
import com.rudraksha.secretchat.ui.screens.chat.InvisibleChatScreen
import com.rudraksha.secretchat.ui.screens.home.HomeScreen
import com.rudraksha.secretchat.viewmodels.AuthViewModel
import com.rudraksha.secretchat.utils.createChatId
import com.rudraksha.secretchat.utils.getReceivers
import com.rudraksha.secretchat.utils.isUserInChat
import com.rudraksha.secretchat.viewmodels.ChatDetailViewModel
import com.rudraksha.secretchat.viewmodels.ChatDetailViewModelFactory
import com.rudraksha.secretchat.viewmodels.ChatListViewModel
import com.rudraksha.secretchat.viewmodels.InvisibleChatViewModel
import com.rudraksha.secretchat.viewmodels.InvisibleChatViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun NavigationManager(
    navController: NavHostController,
    context: Context,
) {
    val chatListViewModel: ChatListViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()

//    val currentUser by chatListViewModel.registeredUser.collectAsStateWithLifecycle()
    var currentUser by remember { mutableStateOf<User?>(null) }
    val chatList by chatListViewModel.chatList.collectAsStateWithLifecycle()

    var insertedSelfChat by remember { mutableStateOf(false) }

    // Ensure registered user is always fetched
    LaunchedEffect(Unit) {
        chatListViewModel.getRegisteredUser()
    }

    fun insertSelfChat(user: User) {
        val uname = user.username
        if (!chatList.any { isUserInChat(it.chatId, uname) }) {
            chatListViewModel.addChat(
                Chat(
                    chatId = createChatId(listOf(uname)),
                    name = "You ($uname)",
                    createdBy = uname,
                    participants = uname
                )
            )
            Log.d("Inserted", "Self Chat")
            insertedSelfChat = true
        }
    }

    // Ensure self-chat is inserted once
    LaunchedEffect(currentUser, chatList) {
        Log.d("Recomposed", "Launched Effect")
        currentUser?.let { user ->
            if (!insertedSelfChat) {
                withContext(Dispatchers.Unconfined) {
                    insertSelfChat(user)
                }
            }
        }
    }

    suspend fun updateCurrentUser(user: User) {
        withContext(Dispatchers.Unconfined) {
            currentUser = user
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.Splash.route // Start with SplashScreen
    ) {
        composable(Routes.Splash.route) {
            SplashScreen(navController, chatListViewModel, ::updateCurrentUser)
        }
        composable(Routes.Registration.route) {
            RegisterScreen(
                register = authViewModel::register,
                observeRegisterState = authViewModel.registerState.collectAsStateWithLifecycle(),
                observeRegisteredUser = authViewModel.currentUser.collectAsStateWithLifecycle(),
                onNavigateToLogin = { navController.navigate(Routes.Login.route) },
                onRegisterSuccess = { user ->
                    currentUser = user
//                    insertSelfChat(user)
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
                onLoginSuccess = { email ->
                    authViewModel.getUserByEmail(email)
                    currentUser = authViewModel.currentUser.value
//                    if (user != null) {
//                        insertSelfChat(user)
//                    }
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Home.route) {
            val user = chatListViewModel.registeredUser.collectAsState().value
            LaunchedEffect(Unit) {
                user?.let {
                    updateCurrentUser(it)
                }
                chatListViewModel.getAllChats() // Ensure chat list is always updated
            }

            HomeScreen(
                navController = navController,
                onChatItemClick = { chatId -> navController.navigate("${Routes.Chat.route}/$chatId") },
                chatList = chatList.map { it.toChatItem(lastMessage = "LAST", time = "TIME", unreadCount = 0) }
            )
        }

        composable("${Routes.Chat.route}/{chatId}") { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            val chatDetailViewModel: ChatDetailViewModel = viewModel(
                factory = ChatDetailViewModelFactory(
                    chatId = chatId, username = currentUser?.username ?: "default",
                    navController.context.applicationContext as android.app.Application
                )
            )

            val webSocketManager: WebSocketManager = remember { WebSocketManager(currentUser?.username ?: "default") }
            val messages = webSocketManager.messages.collectAsState() // Observing messages
            LaunchedEffect(Unit) {
                webSocketManager.connect()
            }

            val receivers = getReceivers(chatId, currentUser?.username ?: "default")
            ChatScreen(
                username = currentUser?.username ?: "default",
                chatName = chatList.find { it.chatId == chatId }?.name ?: "Default Chat",
                sendMessage = { message: String ->
                    chatDetailViewModel.sendMessage(message, chatId)
                },
                onNavIconClick = {
                    navController.navigateUp()
                },
                messages = messages,
                onMessageReaction = { message, reaction ->
//                    chatDetailViewModel.reactToMessage(message, reaction)
                },
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

            val webSocketManager: WebSocketManager = remember { WebSocketManager(currentUser?.username ?: "default") }
            val messages = webSocketManager.messages.collectAsState() // Observing messages
            LaunchedEffect(Unit) {
                webSocketManager.connect()
            }

            InvisibleChatScreen(
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
    }
}

@Composable
fun SplashScreen(
    navController: NavController, userViewModel: ChatListViewModel,
    updateCurrentUser: suspend (User) -> Unit = {}
) {
    val currentUser by userViewModel.registeredUser.collectAsState()

    LaunchedEffect(currentUser) {
        delay(500) // Optional: Show splash for a bit
        if (currentUser == null) {
            navController.navigate(Routes.Registration.route) {
                popUpTo(Routes.Splash.route) { inclusive = true } // Prevent going back to Splash
            }
        } else {
            currentUser?.let {
                updateCurrentUser(it)
                navController.navigate(Routes.Home.route) {
                    popUpTo(Routes.Splash.route) { inclusive = true }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Loading...", fontSize = 24.sp)
    }
}
