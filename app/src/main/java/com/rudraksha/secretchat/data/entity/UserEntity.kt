package com.rudraksha.secretchat.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.rudraksha.secretchat.data.converters.Converters
import kotlinx.serialization.Serializable

@TypeConverters(Converters::class)
@Serializable
@Entity(
    tableName = "users",
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val userId: Int = 0,
    val email: String = "",
    val username: String = "",
    val password: String = "",
    val fullName: String = "",
    val online: Boolean = false,
    val description: String = "",
    val profilePictureUrl: String = "",
    val contacts: String = "" // Comma separated list of user ids
)

@Entity(
    tableName = "contacts",
)
data class Contact(
    @PrimaryKey val username: String,
    val contactUsernames: String
)
