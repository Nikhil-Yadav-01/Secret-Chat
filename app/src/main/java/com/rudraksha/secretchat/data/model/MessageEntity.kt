package com.rudraksha.secretchat.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.rudraksha.secretchat.data.converters.Converters
import kotlinx.serialization.Serializable
import java.util.UUID

@TypeConverters(Converters::class)
@Serializable
@Entity(
    tableName = "messages",
)
data class MessageEntity(
    @PrimaryKey val messageId: String = UUID.randomUUID().toString(),
    val senderId: String,
    @ColumnInfo(name = "chatId") val chatId: String = "",
    // For Room, store receiversId as a comma-separated string; a TypeConverter can convert to List<String>
    val receiversId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val type: MessageType = MessageType.TEXT,  // TEXT, IMAGE, VIDEO, etc.
    val content: String? = null, // For text messages
    val isRead: Boolean = false,
//    val fileMetadata: String? = null // For files (if any)
)

@Serializable
data class FileMetadata(
    val fileName: String,
    val fileType: String,
    val fileSize: Long,
    val totalChunks: Long // Total number of chunks
)