package com.rudraksha.secretchat.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudraksha.secretchat.data.model.ChatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chatEntity: ChatEntity)

    @Query("SELECT * FROM chats ORDER BY createdAt DESC")
    fun getAllChats(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE chatId = :chatId LIMIT 1")
    suspend fun getChatById(chatId: String): ChatEntity?

//    @Delete(entity = Chat::class)
//    suspend fun deleteChat(id: String)
}
