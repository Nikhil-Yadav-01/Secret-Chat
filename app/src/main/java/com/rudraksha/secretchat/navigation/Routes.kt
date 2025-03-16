package com.rudraksha.secretchat.navigation

import kotlinx.serialization.Serializable

// Define routes as a sealed class
@Serializable
sealed class Routes(val route: String) {
    @Serializable
    data object Splash: Routes("splash")

    @Serializable
    data object Registration: Routes("registration")

    @Serializable
    data object Login: Routes("login")

    @Serializable
    data object Home: Routes("home")

    @Serializable
    data object Calls: Routes("calls")

    @Serializable
    data object Chat: Routes("chat")

    @Serializable
    data object Profile: Routes("profile")

    @Serializable
    data object InvisibleChat: Routes("invisible_chat")

    @Serializable
    data object Settings: Routes("settings")

    @Serializable
    data object Notifications: Routes("notifications")
}