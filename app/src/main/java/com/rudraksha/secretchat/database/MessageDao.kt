package com.rudraksha.secretchat.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudraksha.secretchat.data.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(messageEntity: MessageEntity)

    @Query("SELECT * FROM messages WHERE senderId = :userId OR receiversId = :userId ORDER BY timestamp ASC")
    suspend fun getMessagesForUser(userId: Int): List<MessageEntity>

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    suspend fun getAllMessages(): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp DESC")
    suspend fun getMessagesForChat(chatId: String): List<MessageEntity>?

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesByChatId(chatId: String): Flow<List<MessageEntity>>

    @Query("UPDATE messages SET isRead = :isRead WHERE messageId = :messageId")
    suspend fun updateMessageReadStatus(messageId: String, isRead: Boolean)

    @Query("UPDATE messages SET content = :content WHERE messageId = :messageId")
    suspend fun updateMessage(content: String, messageId: String)

    @Query("DELETE FROM messages WHERE messageId = :messageId")
    suspend fun deleteMessage(messageId: String)
}

