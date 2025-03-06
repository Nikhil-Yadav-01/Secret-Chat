package com.rudraksha.secretchat.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudraksha.secretchat.data.model.Message
import com.rudraksha.secretchat.data.model.User

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)

    @Query("SELECT * FROM messages WHERE senderId = :userId OR receiversId = :userId ORDER BY timestamp ASC")
    suspend fun getMessagesForUser(userId: Int): List<Message>

    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    suspend fun getAllMessages(): List<Message>

    @Query("SELECT * FROM messages WHERE senderId = :chatId OR receiversId LIKE '%' || :chatId || '%' ORDER BY timestamp ASC")
    suspend fun getMessagesForChat(chatId: String): List<Message>
}

