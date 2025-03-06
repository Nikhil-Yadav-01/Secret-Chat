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
@Entity(tableName = "chats")
data class Chat(
    @PrimaryKey val id: String = UUID.randomUUID().toString(), // Chat id
    val name: String? = null, // Chat name
    val type: ChatType = ChatType.PRIVATE,
    // Store participants as a comma-separated string (use a TypeConverter if you prefer a List)
    val participants: String = "",
    val createdBy: String, // Creator id (or username)
    val createdAt: Long = System.currentTimeMillis()
    // The messages list is excluded from the entity since messages are stored separately.
)

fun Chat.toChatItem(lastMessage: String, time: String, unreadCount: Int): ChatItem {
    return ChatItem(
        id = this.id,
        name = this.name ?: "",
        lastMessage = lastMessage,
        time = time,
        unreadCount = unreadCount,
        profilePic = R.drawable.profile_pic
    )
}
