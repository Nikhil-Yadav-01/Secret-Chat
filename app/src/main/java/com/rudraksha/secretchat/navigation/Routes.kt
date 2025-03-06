package com.rudraksha.secretchat.navigation

// Define routes as a sealed class
sealed class Routes(val route: String) {
    data object Registration: Routes("registration")
    data object Login: Routes("login")
    data object Home: Routes("home")
    data object Calls: Routes("calls")
    data object Chat: Routes("chat")
    data object Profile: Routes("profile")
    data object InvisibleChat: Routes("invisible_chat")
}