package com.rudraksha.secretchat.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.rudraksha.secretchat.data.converters.Converters
import kotlinx.serialization.Serializable

@TypeConverters(Converters::class)
@Serializable
@Entity(
    tableName = "users",
    primaryKeys = ["id", "username"],
)
data class User(
    @PrimaryKey(autoGenerate = true) val userId: Int = 0,
    @PrimaryKey val email: String = "",
    @PrimaryKey val username: String = "",
    val fullName: String = "",
    val online: Boolean = false,
    val description: String = "",
    val profilePictureUrl: String = "",
    val contacts: String = ""// Comma separated list of user ids
)

@Entity(tableName = "contacts",
    primaryKeys = ["userId", "contactId"],
    foreignKeys = [ForeignKey(entity = User::class, parentColumns = ["username"], childColumns = ["username"])])
data class Contact(
    @PrimaryKey val username: String,
    val contactUsernames: String
)
