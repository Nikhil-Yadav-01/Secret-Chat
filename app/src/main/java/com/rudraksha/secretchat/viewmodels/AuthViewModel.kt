package com.rudraksha.secretchat.viewmodels

import android.app.Application
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudraksha.secretchat.data.WebSocketData
import com.rudraksha.secretchat.data.entity.ChatEntity
import com.rudraksha.secretchat.data.model.ChatType
import com.rudraksha.secretchat.data.entity.UserEntity
import com.rudraksha.secretchat.network.AuthApiService
import com.rudraksha.secretchat.network.WebSocketManager
import com.rudraksha.secretchat.database.ChatDatabase
import com.rudraksha.secretchat.utils.BiometricAuthManager
import com.rudraksha.secretchat.utils.NetworkUtils
import com.rudraksha.secretchat.utils.SecurePreferences
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
    private val securePreferences = SecurePreferences(application.applicationContext)
    private val biometricManager = BiometricAuthManager(application.applicationContext)
    private val authApiService = AuthApiService()
    private val appContext = application.applicationContext

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authState = MutableStateFlow("")
    val authState: StateFlow<String> = _authState.asStateFlow()

    private val _currentUserEntity = MutableStateFlow<UserEntity?>(null)
    val currentUserEntity: StateFlow<UserEntity?> = _currentUserEntity.asStateFlow()
    
    private val _isDecoyMode = MutableStateFlow(false)
    val isDecoyMode: StateFlow<Boolean> = _isDecoyMode.asStateFlow()
    
    private val _isBiometricRequired = MutableStateFlow(false)
    val isBiometricRequired: StateFlow<Boolean> = _isBiometricRequired.asStateFlow()

    private var wsManager: WebSocketManager? = null

    init {
        _isLoading.value = true
        getCurrentUser()
        checkSession()
        _isLoading.value = false
    }

    private fun checkSession() {
        // Check if biometric auth is needed
        if (securePreferences.isBiometricEnabled && biometricManager.canAuthenticate()) {
            _isBiometricRequired.value = true
        } else {
            // Check for session timeout
            if (securePreferences.hasSessionTimedOut()) {
                securePreferences.clearSession()
            } else {
                // Get current user and token
                val token = securePreferences.authToken
                
                // First get user, then restore session if we have both user and token
                viewModelScope.launch {
                    val user = withContext(Dispatchers.IO) {
                        userDao.getRegisteredUser()
                    }
                    
                    _currentUserEntity.value = user
                    
                    // Update last active timestamp
                    securePreferences.updateLastActive()
                    
                    // If we have both user and token, restore session
                    if (user != null && token.isNotEmpty()) {
                        Log.d("AuthViewModel", "Restoring session with saved token")
                        // Create WebSocketManager with token and connect
                        wsManager = WebSocketManager(token = token)
                        wsManager?.setOnDataReceivedListener { data ->
                            handleWebSocketData(data)
                        }
                        wsManager?.connect()
                    }
                }
            }
        }
    }
    
    fun getCurrentUser() {
        viewModelScope.launch {
            _currentUserEntity.value = withContext(Dispatchers.IO) {
                userDao.getRegisteredUser()
            }
            
            // Update last active timestamp
            securePreferences.updateLastActive()
            
            Log.d("CUser", _currentUserEntity.value.toString())
        }
    }
    
    /**
     * Authenticate using biometrics
     */
    fun authenticateWithBiometrics(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!biometricManager.canAuthenticate()) {
            onError("Biometric authentication not available")
            _isBiometricRequired.value = false
            return
        }
        
        biometricManager.authenticate(
            activity = activity,
            title = "Authenticate to SecretChat",
            subtitle = "Confirm your identity to access your messages",
            description = "Use your fingerprint or face to continue",
            onSuccess = { 
                _isBiometricRequired.value = false
                getCurrentUser()
                onSuccess()
            },
            onError = { errorMsg ->
                onError(errorMsg)
            },
            onCancel = {
                // Keep biometric required true
            }
        )
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
            _isLoading.value = true
            val emailError = validateEmail(email)
            val passwordError = validatePassword(password)

            if (emailError != null) {
                this@AuthViewModel._authState.value = emailError
                _isLoading.value = false
                return@launch
            }
            if (passwordError != null) {
                this@AuthViewModel._authState.value = passwordError
                _isLoading.value = false
                return@launch
            }

            withContext(Dispatchers.IO) {
                // Check if this is a decoy password
                if (securePreferences.isDecoyPassword(password)) {
                    _isDecoyMode.value = true
                    updateAuthState("Login successful (Decoy Mode)")
                    _isLoading.value = false
                    return@withContext
                }
                
                // Check for network connectivity
                if (NetworkUtils.isNetworkAvailable(appContext)) {
                    try {
                        // First try to log in to the server
                        val result = authApiService.login(email, password)
                        
                        result.onSuccess { authResponse ->
                            // Save token
                            securePreferences.authToken = authResponse.token
                            
                            val userEntity = UserEntity(
                                email = email,
                                username = authResponse.user.username,
                                fullName = authResponse.user.fullName,
                                online = true
                            )
                            
                            userDao.insertUser(userEntity)
                            _currentUserEntity.value = userEntity
                            insertSelfChat()
                            
                            // Save user ID to secure preferences
                            securePreferences.userId = userEntity.username
                            securePreferences.updateLastActive()
                            
                            updateAuthState("Login successful")
                        }.onFailure { error ->
                            Log.e("AuthViewModel", "Server login failed: ${error.message}, falling back to local login")
                            localLogin(email)
                        }
                    } catch (e: Exception) {
                        Log.e("AuthViewModel", "Login error: ${e.message}")
                        localLogin(email)
                    } finally {
                        _isLoading.value = false
                    }
                } else {
                    Log.d("AuthViewModel", "No network connection, using local login")
                    localLogin(email)
                }
                _isLoading.value = false
            }
        }
    }
    
    private suspend fun localLogin(email: String) {
        val user = userDao.getUserByEmail(email)
        if (user == null) {
            updateAuthState("User not found")
        } else {
            _currentUserEntity.value = user
            insertSelfChat()
            
            // Save user ID to secure preferences
            securePreferences.userId = user.username
            securePreferences.updateLastActive()
            
            updateAuthState("Login successful (Offline Mode)")
        }
    }

    fun register(
        fullName: String, 
        username: String, 
        email: String, 
        password: String, 
        confirmPassword: String,
        decoyPassword: String = ""
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val sanitizedUsername = if (!username.startsWith("@")) "@$username" else username

            val isValid = validate(fullName, sanitizedUsername, email, password, confirmPassword)
            if (!isValid) {
                _isLoading.value = false
                return@launch
            }

            withContext(Dispatchers.IO) {
                val existingUserByEmail = userDao.getUserByEmail(email)
                val existingUserByUsername = userDao.getUserByUsername(username)

                if (existingUserByUsername != null) {
                    updateAuthState("User already exists with same username")
                    _isLoading.value = false
                    return@withContext
                }
                if (existingUserByEmail != null) {
                    updateAuthState("Email is already registered")
                    _isLoading.value = false
                    return@withContext
                }

                // Check for network connectivity
                if (NetworkUtils.isNetworkAvailable(appContext)) {
                    try {
                        // Try to register with server first
                        val result = authApiService.register(fullName, email, password)
                        
                        result.onSuccess { authResponse ->
                            // Save token
                            securePreferences.authToken = authResponse.token
                            
                            // Create user entity from response
                            val newUserEntity = UserEntity(
                                email = email, 
                                username = sanitizedUsername, 
                                fullName = fullName, 
                                online = true
                            )
                            
                            // Save to Room database
                            userDao.insertUser(newUserEntity)
                            _currentUserEntity.value = newUserEntity
                            insertSelfChat()
                            saveUser(newUserEntity, password)
                            
                            // Save preferences
                            securePreferences.userId = newUserEntity.username
                            if (decoyPassword.isNotEmpty()) {
                                securePreferences.decoyModePassword = decoyPassword
                            }
                            securePreferences.updateLastActive()
                            
                            updateAuthState("Registration successful")
                        }.onFailure { error ->
                            Log.e("AuthViewModel", "Server registration failed: ${error.message}, proceeding with local registration")
                            localRegister(fullName, sanitizedUsername, email, password, decoyPassword)
                        }
                    } catch (e: Exception) {
                        Log.e("AuthViewModel", "Registration error: ${e.message}")
                        localRegister(fullName, sanitizedUsername, email, password, decoyPassword)
                    } finally {
                        _isLoading.value = false
                    }
                } else {
                    Log.d("AuthViewModel", "No network connection, using local registration")
                    localRegister(fullName, sanitizedUsername, email, password, decoyPassword)
                }
                _isLoading.value = false
            }
        }
    }
    
    private suspend fun localRegister(
        fullName: String,
        username: String,
        email: String,
        password: String,
        decoyPassword: String
    ) {
        val newUserEntity = UserEntity(email = email, username = username, fullName = fullName, online = true)

        try {
            // Save to Room database
            userDao.insertUser(newUserEntity)
            _currentUserEntity.value = newUserEntity
            insertSelfChat()
            saveUser(newUserEntity, password)
            
            // Save preferences
            securePreferences.userId = newUserEntity.username
            if (decoyPassword.isNotEmpty()) {
                securePreferences.decoyModePassword = decoyPassword
            }
            securePreferences.updateLastActive()
            
            updateAuthState("Registration successful (Offline Mode)")
        } catch (e: Exception) {
            updateAuthState("Error: ${e.localizedMessage}")
        }
    }

    /**
     * Set up biometric authentication for the app
     */
    fun enableBiometricAuth(enable: Boolean) {
        securePreferences.isBiometricEnabled = enable
    }
    
    /**
     * Set up automatic logout timer
     */
    fun setAutoLogoutTime(minutes: Int) {
        securePreferences.autoLogoutTimeMinutes = minutes
    }
    
    /**
     * Set up decoy mode password
     */
    fun setDecoyPassword(password: String) {
        securePreferences.decoyModePassword = password
    }
    
    /**
     * Toggle screenshot blocking
     */
    fun setScreenshotBlocking(enabled: Boolean) {
        securePreferences.isScreenshotBlockingEnabled = enabled
    }
    
    /**
     * Get screenshot blocking setting
     */
    fun isScreenshotBlockingEnabled(): Boolean {
        return securePreferences.isScreenshotBlockingEnabled
    }
    
    /**
     * Log the user out
     */
    fun logout() {
        viewModelScope.launch {
            try {
                // Disconnect WebSocket
                wsManager?.disconnect()
                wsManager = null
                
                // Clear authentication state
                securePreferences.clearSession()
                securePreferences.authToken = ""
                
                // Reset state
                _currentUserEntity.value = null
                _isDecoyMode.value = false
                
                updateAuthState("")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error during logout: ${e.message}")
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

    private fun saveUser(user: UserEntity, password: String) {
        // Get token from preferences if available
        val token = securePreferences.authToken
        
        // Create WebSocketManager instance with appropriate authentication
        wsManager = if (token.isNotEmpty()) {
            Log.d("AuthViewModel", "Initializing WebSocketManager with token")
            WebSocketManager(token = token)
        } else {
            Log.d("AuthViewModel", "Initializing WebSocketManager with username/password")
            WebSocketManager(user.username, password)
        }
        
        // Connect to WebSocket
        wsManager?.setOnDataReceivedListener { data ->
            handleWebSocketData(data)
        }
        wsManager?.connect()
    }

    private suspend fun updateAuthState(state: String) {
        withContext(Dispatchers.Default) {
            _authState.value = state
        }
    }

    private fun handleWebSocketData(data: WebSocketData) {
        when (data) {
            is WebSocketData.Message -> {
                // Message handling can be done in the ChatViewModel
                Log.d("AuthViewModel", "Received message: ${data.content}")
            }
            is WebSocketData.UserList -> {
                // Cache user list if needed
                Log.d("AuthViewModel", "Received user list: ${data.users.size} users")
            }
            is WebSocketData.ConnectionStatus -> {
                // Update connection status
                Log.d("AuthViewModel", "Connection status: ${data.status}")
            }
            is WebSocketData.Error -> {
                // Handle errors
                Log.e("AuthViewModel", "WebSocket error: ${data.errorMessage}")
                // Launch a coroutine to call the suspend function
                viewModelScope.launch {
                    updateAuthState("Error: ${data.errorMessage}")
                }
            }
            // Add other relevant cases as needed
            else -> {
                // Unknown data type
                Log.d("AuthViewModel", "Received unhandled data type: ${data::class.simpleName}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        authApiService.cleanup()
    }
}
