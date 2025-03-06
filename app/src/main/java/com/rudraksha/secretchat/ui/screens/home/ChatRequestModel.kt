package com.rudraksha.secretchat.ui.screens.home

import kotlinx.serialization.Serializable

@Serializable
data class JoinRequest(
    val senderUsername: String = "",
    val receiverUsername: String = "",
    val joinMessage: String = "",
)

@Serializable
data class JoinResponse(
    val senderUsername: String = "",
    val receiverUsername: String = "",
    val accepted: Boolean = false,
)