package com.rudraksha.secretchat.data.entity

import android.text.format.DateFormat
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.rudraksha.secretchat.R
import com.rudraksha.secretchat.data.converters.Converters
import com.rudraksha.secretchat.data.model.ChatItem
import com.rudraksha.secretchat.data.model.ChatType
import com.rudraksha.secretchat.utils.getReceivers
import kotlinx.serialization.Serializable
import java.util.UUID

@TypeConverters(Converters::class)
@Serializable
@Entity(
    tableName = "chats",
)
data class ChatEntity(
    @PrimaryKey val chatId: String = UUID.randomUUID().toString(), // Chat id
    val name: String = "", // Chat name
    val type: ChatType = ChatType.PRIVATE,
    val participants: String = "",
    val createdBy: String, // Creator username
    val createdAt: Long = System.currentTimeMillis(),
    val lastMessage: String = "",
    val lastEventAt: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val profileImage: String = ""
)

fun ChatEntity.toChatItem(currentUser: String): ChatItem {
    return ChatItem(
        id = this.chatId,
        name = this.name,
        type = this.type,
        receivers = getReceivers(participants = this.participants, currentUser),
        lastMessage = this.lastMessage,
        lastEventAt = DateFormat.format("hh:mm a", this.lastEventAt).toString(),
        unreadCount = this.unreadCount,
        profilePic = R.drawable.profile_pic
    )
}
