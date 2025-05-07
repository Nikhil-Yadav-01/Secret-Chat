package com.rudraksha.secretchat.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudraksha.secretchat.data.entity.UserEntity

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(userEntity: UserEntity)

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getRegisteredUser(userId: Int = 1): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>?

    @Query("UPDATE users SET online = :online WHERE username = :username")
    suspend fun updateOnlineStatus(username: String, online: Boolean)

    @Query("UPDATE users SET profilePictureUrl = :profilePictureUrl WHERE username = :username")
    suspend fun updateProfilePicture(username: String, profilePictureUrl: String)

    @Query("UPDATE users SET fullName = :fullName WHERE username = :username")
    suspend fun updateFullName(username: String, fullName: String)

    @Query("UPDATE users SET description = :description WHERE username = :username")
    suspend fun updateDescription(username: String, description: String)

    @Query("UPDATE users SET contacts = :contacts WHERE username = :username")
    suspend fun updateContacts(username: String, contacts: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateUser(userEntity: UserEntity)

    @Query("DELETE FROM users WHERE username = :username")
    suspend fun deleteUserByUsername(username: String)

}