package com.rudraksha.secretchat.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudraksha.secretchat.data.WebSocketData
import com.rudraksha.secretchat.data.model.ChatEntity
import com.rudraksha.secretchat.data.model.ChatType
import com.rudraksha.secretchat.data.model.UserEntity
import com.rudraksha.secretchat.data.remote.WebSocketManager
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

    private val _authState = MutableStateFlow("")
    val authState: StateFlow<String> = _authState.asStateFlow()

    private val _currentUserEntity = MutableStateFlow<UserEntity?>(null)
    val currentUserEntity: StateFlow<UserEntity?> = _currentUserEntity.asStateFlow()

    init {
        getCurrentUser()
    }

    fun getCurrentUser() {
        viewModelScope.launch {
            _currentUserEntity.value = withContext(Dispatchers.IO) {
                userDao.getRegisteredUser()
            }
            Log.d("CUser", _currentUserEntity.value.toString())
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
            this._authState.value = errors.first() // Show first error message
            false
        } else {
            this._authState.value = ""
            true
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val emailError = validateEmail(email)
            val passwordError = validatePassword(password)

            if (emailError != null) {
                this@AuthViewModel._authState.value = emailError
                return@launch
            }
            if (passwordError != null) {
                this@AuthViewModel._authState.value = passwordError
                return@launch
            }

            withContext(Dispatchers.IO) {
                val user = userDao.getUserByEmail(email)
                if (user == null) {
                    updateAuthState("User not found")
                } else {
                    _currentUserEntity.value = user
                    insertSelfChat()
                    updateAuthState("Login successful")
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
                    updateAuthState("User already exists with same username")
                    return@withContext
                }
                if (existingUserByEmail != null) {
                    updateAuthState("User already exists with same email")
                    return@withContext
                }

                val newUserEntity = UserEntity(email = email, username = sanitizedUsername, fullName = fullName, online = true)

                try {
                    userDao.insertUser(newUserEntity)
                    _currentUserEntity.value = newUserEntity
                    Log.d("User 1", userDao.getRegisteredUser().toString())
                    Log.d("Users", userDao.getAllUsers().toString())
                    insertSelfChat()
                    saveUser(newUserEntity, password)
                    updateAuthState("Registration successful")
                } catch (e: Exception) {
                    updateAuthState("Error: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun insertSelfChat() {
        viewModelScope.launch {
            currentUserEntity.value?.let {
                chatDao.insertChat(
                    ChatEntity(
                        chatId = createChatId(listOf(it.username), ChatType.PRIVATE),
                        name = "You (${it.fullName})",
                        createdBy = it.username,
                        participants = it.username,
                    )
                )
            }
        }
    }

    private fun saveUser(userEntity: UserEntity, password: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val websocketManager = WebSocketManager(userEntity.username, password)
//                websocketManager.connect()
                websocketManager.sendData(WebSocketData.SaveUser(userEntity.username))
            }
        }
    }

    private suspend fun updateAuthState(state: String) {
        withContext(Dispatchers.Default) {
            _authState.value = state
        }
    }
}
