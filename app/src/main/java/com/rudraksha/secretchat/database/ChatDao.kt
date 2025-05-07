package com.rudraksha.secretchat.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudraksha.secretchat.data.entity.ChatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chatEntity: ChatEntity)

    @Query("SELECT * FROM chats ORDER BY createdAt DESC")
    fun getAllChats(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE chatId = :chatId LIMIT 1")
    suspend fun getChatById(chatId: String): ChatEntity?

    @Query("UPDATE chats SET lastMessage = :lastMessage, unreadCount = :unreadCount, lastEventAt = :lastEventAt WHERE chatId = :chatId")
    suspend fun updateChat(chatId: String, lastMessage: String, lastEventAt: Long, unreadCount: Int)

    @Query("UPDATE chats SET profileImage = :profile WHERE chatId = :chatId")
    suspend fun updateProfile(chatId: String, profile: String)

    @Query("UPDATE chats SET lastMessage = :lastMessage WHERE chatId = :chatId")
    suspend fun updateLastMessage(chatId: String, lastMessage: String)

    @Query("UPDATE chats SET lastEventAt = :lastEventAt WHERE chatId = :chatId")
    suspend fun updateLastEventAt(chatId: String, lastEventAt: Long)

    @Query("UPDATE chats SET unreadCount = :unreadCount WHERE chatId = :chatId")
    suspend fun updateUnreadCount(chatId: String, unreadCount: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateChat(chat: ChatEntity)

    @Query("DELETE FROM chats WHERE chatId = :chatId")
    suspend fun deleteChat(chatId: String)
}
