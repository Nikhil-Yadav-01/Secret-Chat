package com.rudraksha.secretchat.utils

suspend fun validate(user: String): Boolean {
    return if (user.isEmpty()) {
        false
    } else {
        isExistingUser(user)
    }
}

suspend fun isExistingUser(data: String): Boolean {
    return false
}