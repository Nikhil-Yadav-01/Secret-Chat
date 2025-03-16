package com.rudraksha.secretchat.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rudraksha.secretchat.data.converters.Converters
import com.rudraksha.secretchat.data.model.Chat
import com.rudraksha.secretchat.data.model.User
import com.rudraksha.secretchat.data.model.Message
import com.rudraksha.secretchat.data.model.Contact

@TypeConverters(Converters::class)
@Database(entities = [Message::class, Chat::class, User::class, Contact::class], version = 1, exportSchema = false)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun chatDao(): ChatDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: ChatDatabase? = null

        fun getDatabase(context: Context): ChatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChatDatabase::class.java,
                    "chat_database"
                    )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
