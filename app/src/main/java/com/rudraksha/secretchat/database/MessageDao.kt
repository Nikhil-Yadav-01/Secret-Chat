package com.rudraksha.secretchat.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudraksha.secretchat.data.model.MessageEntity

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

//    @Delete(entity = Message::class)
//    suspend fun deleteMessage(id: String)
}

