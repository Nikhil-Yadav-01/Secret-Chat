package com.rudraksha.secretchat.navigation

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rudraksha.secretchat.data.model.ChatItem
import com.rudraksha.secretchat.data.model.ChatType
import com.rudraksha.secretchat.data.entity.toChatItem
import com.rudraksha.secretchat.network.WebSocketManager
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
import android.app.Application
import com.rudraksha.secretchat.ui.screens.profile.ProfileScreen
import com.rudraksha.secretchat.ui.screens.settings.SettingsScreen
import com.rudraksha.secretchat.ui.screens.splash.SplashScreen

@Composable
fun NavigationManager(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    context: Context,
) {
    val chatListViewModel: ChatListViewModel = viewModel()
    val currentUser by authViewModel.currentUserEntity.collectAsState()
    val chatList by chatListViewModel.chatEntityList.collectAsStateWithLifecycle()

    lateinit var webSocketManager: WebSocketManager

    NavHost(
        navController = navController,
        startDestination = if (currentUser == null) Routes.Registration else Routes.Home
    ) {
//        composable<Routes.Splash> {
//            SplashScreen(
//                navigateToRegister = {
//                    navController.navigate(Routes.Registration) {
//                        popUpTo(0) { inclusive = true } // Prevent going back to Splash
//                    }
//                },
//                navigateToHome = {
//                    navController.navigate(Routes.Home) {
//                        popUpTo(0) { inclusive = true }
//                    }
//                },
//                observeCurrentUserEntity = authViewModel.currentUserEntity,
//            )
//        }

        composable<Routes.Registration> {
            RegisterScreen(
                register = authViewModel::register,
                observeRegisterState = authViewModel.authState,
                observeLoadingState = authViewModel.isLoading,
                navigateToLogin = { navController.navigate(Routes.Login) },
                onRegisterSuccess = {
                    navController.navigate(Routes.Home) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<Routes.Login> {
            LoginScreen(
                login = authViewModel::login,
                observeLoginState = authViewModel.authState,
                observeLoadingState = authViewModel.isLoading,
                navigateToRegister = { navController.navigateUp() },
                onLoginSuccess = {
                    navController.navigate(Routes.Home) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<Routes.Home> {
            LaunchedEffect(Unit) {
                currentUser?.let {
                    webSocketManager = WebSocketManager(
                        username = it.username,
                        password = it.password
                    )
                    webSocketManager.connect()
                }
                chatListViewModel.getAllChats() // Ensure chat list is always updated
                Log.d("ChatList", chatList.toString())
            }
            Log.d("Time 4", System.currentTimeMillis().toString())

            HomeScreen(
                chatList = chatList.map { it.toChatItem(currentUser?.username ?: "default") },
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
                navigateToProfile = {
                    navController.navigate(Routes.Profile)
                },
                navigateToSettings = {
                    navController.navigate(Routes.Settings)
                },
                navigateToInvisibleChat = {
                    navController.navigate(Routes.InvisibleChat)
                },
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
                    application = navController.context.applicationContext as Application,
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
                    if (chat != null) { invisibleChatViewModel.sendMessage(message) }
                    Log.d("chat 1", chat.toString())
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

        composable<Routes.Profile> {
            ProfileScreen(
                navigateBack = { navController.navigateUp() }
            )
        }

        composable<Routes.Settings> {
            SettingsScreen(
                navigateBack = { navController.navigateUp() }
            )
        }
    }
}
