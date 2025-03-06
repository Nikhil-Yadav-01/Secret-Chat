package com.rudraksha.secretchat.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudraksha.secretchat.database.ChatDatabase
import com.rudraksha.secretchat.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegistrationViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = ChatDatabase.getDatabase(application).userDao()

    private val _registeredUser = MutableStateFlow<User?>(null)
    val registeredUser = _registeredUser.asStateFlow()

    fun registerUser(email: String, username: String, fullName: String) {
        viewModelScope.launch {
            val user = User(
                email = email,
                username = username,
                fullName = fullName,
                online = true,
                description = "",
                profilePictureUrl = "",
                contacts = "[]"
            )
            userDao.insertUser(user)
            _registeredUser.value = user
        }
    }
}
