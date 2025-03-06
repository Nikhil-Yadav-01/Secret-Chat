package com.rudraksha.secretchat.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatItem(
    val id: String = "",
    val name: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int,
    val profilePic: Int
)