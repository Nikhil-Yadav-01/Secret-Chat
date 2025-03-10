package com.rudraksha.secretchat.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.rudraksha.secretchat.R
import com.rudraksha.secretchat.data.converters.Converters
import kotlinx.serialization.Serializable
import java.util.UUID

@TypeConverters(Converters::class)
@Serializable
@Entity(
    tableName = "chats",
)
data class Chat(
    @PrimaryKey val chatId: String = UUID.randomUUID().toString(), // Chat id
    val name: String? = null, // Chat name
    val type: ChatType = ChatType.PRIVATE,
    val participants: String = "",
    val createdBy: String, // Creator username
    val createdAt: Long = System.currentTimeMillis()
)

fun Chat.toChatItem(lastMessage: String, time: String, unreadCount: Int): ChatItem {
    return ChatItem(
        id = this.chatId,
        name = this.name ?: "",
        lastMessage = lastMessage,
        time = time,
        unreadCount = unreadCount,
        profilePic = R.drawable.profile_pic
    )
}
