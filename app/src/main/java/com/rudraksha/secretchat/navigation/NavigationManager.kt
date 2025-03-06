package com.rudraksha.secretchat.navigation

import android.content.Context
import android.util.Log
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rudraksha.secretchat.RegistrationScreen
import com.rudraksha.secretchat.data.model.Chat
import com.rudraksha.secretchat.data.model.Message
import com.rudraksha.secretchat.data.model.User
import com.rudraksha.secretchat.data.model.toChatItem
import com.rudraksha.secretchat.ui.screens.chat.ChatScreen
import com.rudraksha.secretchat.ui.screens.chat.InvisibleChatScreen
import com.rudraksha.secretchat.ui.screens.home.HomeScreen
import com.rudraksha.secretchat.utils.createChatId
import com.rudraksha.secretchat.utils.getReceivers
import com.rudraksha.secretchat.utils.isUserInChat
import com.rudraksha.secretchat.viewmodels.ChatDetailViewModel
import com.rudraksha.secretchat.viewmodels.ChatDetailViewModelFactory
import com.rudraksha.secretchat.viewmodels.ChatListViewModel
import com.rudraksha.secretchat.viewmodels.RegistrationViewModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Composable
fun NavigationManager(
    navController: NavHostController,
    context: Context,
) {
    val chatListViewModel: ChatListViewModel = viewModel()

    var currentUser: User? = chatListViewModel.registeredUser.collectAsState().value
    val chatList = chatListViewModel.chatList.collectAsState().value
    var insertedSelfChat by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        chatListViewModel.getRegisteredUser()
    }
    LaunchedEffect(currentUser) {
        chatListViewModel.getRegisteredUser()
        if (!insertedSelfChat) {
            currentUser?.let { user ->
                user.username.let { uname ->
                    if (!chatList.any { isUserInChat(it.id, user.username) }) {
                        chatListViewModel.addChat(
                            Chat(
                                id = createChatId(
                                    usernames = listOf(uname),
                                ),
                                name = "You (${uname})",
                                createdBy = uname,
                                participants = uname
                            )
                        )
                        Log.d("Inserted", "Chat")
                        insertedSelfChat = true
                    }
                }
            }
        }
    }

    NavHost(navController = navController,
        startDestination = if (currentUser == null) Routes.Registration.route else Routes.Home.route
    ) {
        composable(Routes.Registration.route) {
            val registrationViewModel: RegistrationViewModel = viewModel<RegistrationViewModel>()
            RegistrationScreen(
                navController, registrationViewModel,
            )
            currentUser = registrationViewModel.registeredUser.collectAsState().value
        }

        composable(Routes.Home.route) {
            chatListViewModel.getRegisteredUser()
            if (!insertedSelfChat) {
                currentUser?.let { user ->
                    user.username.let { uname ->
                        if (!chatList.any { isUserInChat(it.id, user.username) }) {
                            chatListViewModel.addChat(
                                Chat(
                                    id = createChatId(
                                        usernames = listOf(uname),
                                    ),
                                    name = "You (${uname})",
                                    createdBy = uname,
                                    participants = uname
                                )
                            )
                            Log.d("Inserted", "Chat")
                            insertedSelfChat = true
                        }
                    }
                }
            }

            chatListViewModel.getAllChats()

            HomeScreen(
                navController = navController,
                onChatItemClick = { chatId ->
                    navController.navigate("${Routes.Chat.route}/$chatId")
                },
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

            val receivers = getReceivers(chatId, currentUser?.username ?: "default")
            ChatScreen(
                chatName = chatList.find { it.id == chatId }?.name ?: "Default Chat",
                username = currentUser?.username ?: "default",
                sendMessage = { message: String ->
                    chatDetailViewModel.sendMessage(message, receivers)
                },
                onNavIconClick = {
                    navController.navigateUp()
                },
                receivers = receivers
            )
        }

        composable(
            route = Routes.InvisibleChat.route,
            enterTransition = {
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
            }
        ) {
            InvisibleChatScreen(
                username = currentUser?.username ?: "default",
                onNavIconClick = {
                    navController.navigateUp()
                },
                context = context
            )
        }
    }
}
