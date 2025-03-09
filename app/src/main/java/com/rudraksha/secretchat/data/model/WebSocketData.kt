package com.rudraksha.secretchat.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.rudraksha.secretchat.data.converters.Converters
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
sealed class WebSocketData {

    @TypeConverters(Converters::class)
    @Serializable
    @Entity(tableName = "messages")
    data class Message(
        @PrimaryKey val id: String = UUID.randomUUID().toString(),
        val senderId: String,
        // For Room, store receiversId as a comma-separated string; a TypeConverter can convert to List<String>
        val receiversId: String = "",
        val timestamp: Long = System.currentTimeMillis(),
        val type: MessageType = MessageType.TEXT,  // TEXT, IMAGE, VIDEO, etc.
        val content: String? = null, // For text messages
        val fileMetadata: String? = null // For files (if any)
    ): WebSocketData()

    @Serializable
    data class JoinRequest(
        val senderUsername: String = "",
        val receiverUsername: String = "",
        val joinMessage: String = "",
    ): WebSocketData()

    @Serializable
    data class JoinResponse(
        val senderUsername: String = "",
        val receiverUsername: String = "",
        val accepted: Boolean = false,
    ): WebSocketData()
}

/*
@Serializable
data class FileMetadata(
    val fileName: String,
    val fileType: String,
    val fileSize: Long,
    val totalChunks: Long // Total number of chunks
)*/
