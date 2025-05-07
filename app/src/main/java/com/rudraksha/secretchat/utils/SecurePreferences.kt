package com.rudraksha.secretchat.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Manages secure storage of application preferences using EncryptedSharedPreferences
 */
class SecurePreferences(private val context: Context) {
    
    companion object {
        private const val PREFS_FILE_NAME = "secret_chat_prefs"
        
        // Preference keys
        private const val KEY_USER_ID = "user_id"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_AUTO_LOGOUT_TIME = "auto_logout_time"
        private const val KEY_CLOUD_STORAGE_ENABLED = "cloud_storage_enabled"
        private const val KEY_MESSAGE_TTL = "message_ttl" // Time-to-live for messages
        private const val KEY_DECOY_MODE_PASSWORD = "decoy_mode_password"
        private const val KEY_SCREENSHOT_BLOCKING = "screenshot_blocking"
        private const val KEY_TYPING_INDICATORS = "typing_indicators"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_LAST_ACTIVE = "last_active"
    }
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val preferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    // User credentials
    var userId: String
        get() = preferences.getString(KEY_USER_ID, "") ?: ""
        set(value) = preferences.edit().putString(KEY_USER_ID, value).apply()
    
    var authToken: String
        get() = preferences.getString(KEY_AUTH_TOKEN, "") ?: ""
        set(value) = preferences.edit().putString(KEY_AUTH_TOKEN, value).apply()
    
    // Security settings
    var isBiometricEnabled: Boolean
        get() = preferences.getBoolean(KEY_BIOMETRIC_ENABLED, false)
        set(value) = preferences.edit().putBoolean(KEY_BIOMETRIC_ENABLED, value).apply()
    
    var autoLogoutTimeMinutes: Int
        get() = preferences.getInt(KEY_AUTO_LOGOUT_TIME, 30) // Default 30 minutes
        set(value) = preferences.edit().putInt(KEY_AUTO_LOGOUT_TIME, value).apply()
    
    var lastActiveTimestamp: Long
        get() = preferences.getLong(KEY_LAST_ACTIVE, 0)
        set(value) = preferences.edit().putLong(KEY_LAST_ACTIVE, value).apply()
    
    // Storage settings
    var isCloudStorageEnabled: Boolean
        get() = preferences.getBoolean(KEY_CLOUD_STORAGE_ENABLED, false)
        set(value) = preferences.edit().putBoolean(KEY_CLOUD_STORAGE_ENABLED, value).apply()
    
    var messageTTLHours: Int
        get() = preferences.getInt(KEY_MESSAGE_TTL, 24) // Default 24 hours
        set(value) = preferences.edit().putInt(KEY_MESSAGE_TTL, value).apply()
    
    // Decoy mode
    var decoyModePassword: String
        get() = preferences.getString(KEY_DECOY_MODE_PASSWORD, "") ?: ""
        set(value) = preferences.edit().putString(KEY_DECOY_MODE_PASSWORD, value).apply()
    
    fun isDecoyPassword(password: String): Boolean {
        val savedDecoyPassword = decoyModePassword
        return savedDecoyPassword.isNotEmpty() && savedDecoyPassword == password
    }
    
    // Privacy settings
    var isScreenshotBlockingEnabled: Boolean
        get() = preferences.getBoolean(KEY_SCREENSHOT_BLOCKING, true)
        set(value) = preferences.edit().putBoolean(KEY_SCREENSHOT_BLOCKING, value).apply()
    
    var isTypingIndicatorsEnabled: Boolean
        get() = preferences.getBoolean(KEY_TYPING_INDICATORS, true)
        set(value) = preferences.edit().putBoolean(KEY_TYPING_INDICATORS, value).apply()
    
    // Appearance
    var themeMode: ThemeMode
        get() = ThemeMode.valueOf(preferences.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
        set(value) = preferences.edit().putString(KEY_THEME_MODE, value.name).apply()
    
    // Clear all preferences (logout)
    fun clearAll() {
        preferences.edit().clear().apply()
    }
    
    // Only clear session data but keep settings
    fun clearSession() {
        preferences.edit()
            .remove(KEY_USER_ID)
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_LAST_ACTIVE)
            .apply()
    }
    
    // Update last active timestamp
    fun updateLastActive() {
        lastActiveTimestamp = System.currentTimeMillis()
    }
    
    // Check if session has timed out
    fun hasSessionTimedOut(): Boolean {
        val lastActive = lastActiveTimestamp
        if (lastActive == 0L) return false // Never logged in
        
        val timeoutMillis = autoLogoutTimeMinutes * 60 * 1000L
        val currentTime = System.currentTimeMillis()
        
        return (currentTime - lastActive) > timeoutMillis
    }
    
    // Theme mode enum
    enum class ThemeMode {
        LIGHT, DARK, SYSTEM
    }
} 