package com.rudraksha.secretchat.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.rudraksha.secretchat.data.converters.Converters
import kotlinx.serialization.Serializable

@TypeConverters(Converters::class)
@Serializable
@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: Int = 0,
    val email: String = "",
    val username: String = "",
    val fullName: String = "",
    val online: Boolean = false,
    val description: String = "",
    val profilePictureUrl: String = "",
    // Since Room does not directly support lists, you can store contacts as a JSON string.
    // Alternatively, consider a separate table for contacts.
    val contacts: String = "[]"
)
