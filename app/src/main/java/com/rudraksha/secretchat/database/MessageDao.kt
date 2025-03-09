package com.rudraksha.secretchat.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudraksha.secretchat.data.model.Message

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)

    @Query("SELECT * FROM messages WHERE senderId = :userId OR receiversId = :userId ORDER BY timestamp ASC")
    suspend fun getMessagesForUser(userId: Int): List<Message>

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    suspend fun getAllMessages(): List<Message>

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp DESC")
    suspend fun getMessagesForChat(chatId: String): List<Message>

//    @Delete(entity = Message::class)
//    suspend fun deleteMessage(id: String)
}

