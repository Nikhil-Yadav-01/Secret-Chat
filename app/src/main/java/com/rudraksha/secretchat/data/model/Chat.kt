package com.rudraksha.secretchat.data.model

import android.text.format.DateFormat
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
    val name: String = "", // Chat name
    val type: ChatType = ChatType.PRIVATE,
    val participants: String = "",
    val createdBy: String, // Creator username
    val createdAt: Long = System.currentTimeMillis(),
    val lastMessage: String = "",
    val time: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val profileImage: String = ""
)

fun Chat.toChatItem(): ChatItem {
    return ChatItem(
        id = this.chatId,
        name = this.name,
        lastMessage = this.lastMessage,
        time = DateFormat.format("hh:mm a", this.time).toString(),
        unreadCount = this.unreadCount,
        profilePic = R.drawable.profile_pic
    )
}
