package com.rudraksha.secretchat.data.model

import com.rudraksha.secretchat.data.entity.UserEntity
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserEntity,
    val message: String? = null
)

@Serializable
data class UserCredentials(
    val email: String,
    val password: String
) 