package com.rudraksha.secretchat.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudraksha.secretchat.data.model.Chat
import com.rudraksha.secretchat.data.model.User
import com.rudraksha.secretchat.database.ChatDatabase
import com.rudraksha.secretchat.utils.createChatId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val userDao = ChatDatabase.getDatabase(application.applicationContext).userDao()
    private val chatDao = ChatDatabase.getDatabase(application.applicationContext).chatDao()

    private val _loginState = MutableStateFlow("")
    val loginState: StateFlow<String> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow("")
    val registerState: StateFlow<String> = _registerState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        getCurrentUser()
    }

    fun getCurrentUser() {
        viewModelScope.launch {
            _currentUser.value = withContext(Dispatchers.IO) {
                userDao.getRegisteredUser()
            }
            Log.d("CUser", _currentUser.value.toString())
        }
    }

    // ✅ Full Name Validation
    private fun validateFullName(fullName: String): String? {
        return when {
            fullName.isEmpty() -> "Full name cannot be empty"
            fullName.length < 3 -> "Full name must be at least 3 characters long"
            else -> null
        }
    }

    // ✅ Username Validation (Auto-fixes and checks for invalid characters)
    private fun validateUserName(username: String): String? {
        if (username.isEmpty()) return "Username cannot be empty"
        if (username.contains(Regex("[,\$^]"))) return "Username cannot contain ',', '$', or '^'"
        if (username.count { it == '@' } > 1) return "Username cannot contain @ more than once"

        return null
    }

    // ✅ Email Validation
    private fun validateEmail(email: String): String? {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return when {
            email.isEmpty() -> "Email cannot be empty"
            !email.matches(emailRegex) -> "Invalid email format"
            else -> null
        }
    }

    // ✅ Password Validation
    private fun validatePassword(password: String, confirmPassword: String? = null): String? {
        val passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$".toRegex()
        return when {
            password.isEmpty() -> "Password cannot be empty"
            !password.matches(passwordRegex) -> "Password must be at least 8 characters long, include 1 uppercase letter, 1 lowercase letter, and 1 number"
            confirmPassword != null && password != confirmPassword -> "Passwords do not match"
            else -> null
        }
    }

    // ✅ General Validation
    private fun validate(fullName: String, username: String, email: String, password: String, confirmPassword: String): Boolean {
        val errors = listOfNotNull(
            validateFullName(fullName),
            validateUserName(username),
            validateEmail(email),
            validatePassword(password, confirmPassword)
        )

        return if (errors.isNotEmpty()) {
            _registerState.value = errors.first() // Show first error message
            false
        } else {
            _registerState.value = ""
            true
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val emailError = validateEmail(email)
            val passwordError = validatePassword(password)

            if (emailError != null) {
                _loginState.value = emailError
                return@launch
            }
            if (passwordError != null) {
                _loginState.value = passwordError
                return@launch
            }

            withContext(Dispatchers.IO) {
                val user = userDao.getUserByEmail(email)
                if (user == null) {
                    updateLoginState("User not found")
                } else {
                    _currentUser.value = user
                    insertSelfChat()
                    updateLoginState("Login successful")
                }
            }
        }
    }

    fun register(fullName: String, username: String, email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            val sanitizedUsername = if (!username.startsWith("@")) "@$username" else username

            val isValid = validate(fullName, sanitizedUsername, email, password, confirmPassword)
            if (!isValid) return@launch

            withContext(Dispatchers.IO) {
                val existingUserByEmail = userDao.getUserByEmail(email)
                val existingUserByUsername = userDao.getUserByUsername(username)

                if (existingUserByUsername != null) {
                    updateRegisterState("User already exists with same username")
                    return@withContext
                }
                if (existingUserByEmail != null) {
                    updateRegisterState("User already exists with same email")
                    return@withContext
                }

                val newUser = User(email = email, username = sanitizedUsername, fullName = fullName, online = true)

                try {
                    userDao.insertUser(newUser)
                    _currentUser.value = newUser
                    Log.d("User 1", userDao.getRegisteredUser().toString())
                    Log.d("Users", userDao.getAllUsers().toString())
                    insertSelfChat()
                    updateRegisterState("Registration successful")
                } catch (e: Exception) {
                    updateRegisterState("Error: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun insertSelfChat() {
        viewModelScope.launch {
            Log.d("ISC", "0")
            currentUser.value?.let { it ->
                Log.d("ISC", "1")
                chatDao.insertChat(
                    Chat(
                        name = "You (${it.fullName})",
                        createdBy = it.username,
                        participants = it.username,
                    )
                )
                Log.d("ISC", "2")
            }
            Log.d("ISC", "3")
        }
    }

    private suspend fun updateLoginState(state: String) {
        withContext(Dispatchers.Default) {
            _loginState.value = state
        }
    }

    private suspend fun updateRegisterState(state: String) {
        withContext(Dispatchers.Default) {
            _registerState.value = state
        }
    }
}
