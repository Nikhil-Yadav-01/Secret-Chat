package com.rudraksha.secretchat.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class ChatItem(
    val id: String = "",
    val name: String,
    val type: ChatType = ChatType.PRIVATE,
    val receivers: List<String>,
    val lastMessage: String,
    val lastEventAt: String,
    val unreadCount: Int,
    val profilePic: Int
) : Parcelable
