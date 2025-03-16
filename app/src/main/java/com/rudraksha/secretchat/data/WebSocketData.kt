package com.rudraksha.secretchat.data

import com.rudraksha.secretchat.data.model.Message
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import java.util.UUID

@Serializable
sealed class WebSocketData {

    @Serializable
    @SerialName("Message")
    data class Message(
        val id: String = UUID.randomUUID().toString(),
        val sender: String,
        val receivers: List<String>,
        val chatId: String,
        val content: String? = null,
        val timestamp: Long = System.currentTimeMillis(),
        val isRead: Boolean = false
    ): WebSocketData()

    @Serializable
    @SerialName("JoinRequest")
    data class JoinRequest(
        val sender: String,
        val receiver: String,
        val joinMessage: String? = null,
    ): WebSocketData()

    @Serializable
    @SerialName("JoinResponse")
    data class JoinResponse(
        val senderUsername: String,
        val receiverUsername: String,
        val accepted: Boolean = false,
    ): WebSocketData()

    @Serializable
    @SerialName("GetUsers")
    data class GetUsers(
        val user: String
    ) : WebSocketData() // Request to fetch all users

    @Serializable
    @SerialName("UserList")
    data class UserList(
        val users: List<String>
    ) : WebSocketData()

    @Serializable
    @SerialName("UserStatus")
    data class UserStatus(
        val username: String,
        val isOnline: Boolean
    ) : WebSocketData()

    @Serializable
    @SerialName("TypingStatus")
    data class TypingStatus(
        val sender: String,
        val receivers: List<String>,
        val isTyping: Boolean
    ) : WebSocketData()

    @Serializable
    @SerialName("Acknowledgment")
    data class Acknowledgment(
        val messageId: String,
        val status: Status
    ) : WebSocketData() {
        @Serializable
        enum class Status {
            SENT, DELIVERED, READ
        }
    }

    @Serializable
    @SerialName("Error")
    data class Error(
        val errorCode: Int,
        val errorMessage: String
    ) : WebSocketData()
}

fun Message.toWebSocketMessage(): WebSocketData.Message {
    return WebSocketData.Message(
        id = this.messageId,
        sender = this.senderId,
        receivers = this.receiversId.split(","),
        chatId = this.chatId,
        content = this.content ?: "",
        timestamp = this.timestamp,
        isRead = this.isRead
    )
}

fun WebSocketData.Message.toMessage(): Message {
    return Message(
        messageId = this.id,
        senderId = this.sender,
        receiversId = this.receivers.joinToString(","),
        chatId = this.chatId,
        timestamp = this.timestamp,
        content = this.content,
        isRead = this.isRead
    )
}

// Create the SerializersModule
val webSocketDataModule = SerializersModule {
    polymorphic(WebSocketData::class) {
        subclass(WebSocketData.Message::class)
        subclass(WebSocketData.JoinRequest::class)
        subclass(WebSocketData.JoinResponse::class)
        subclass(WebSocketData.GetUsers::class)
        subclass(WebSocketData.UserList::class)
        subclass(WebSocketData.UserStatus::class)
        subclass(WebSocketData.TypingStatus::class)
        subclass(WebSocketData.Acknowledgment::class)
        subclass(WebSocketData.Error::class)
    }
}
