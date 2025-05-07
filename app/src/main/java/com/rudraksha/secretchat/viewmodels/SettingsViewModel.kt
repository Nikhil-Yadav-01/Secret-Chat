package com.rudraksha.secretchat.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudraksha.secretchat.utils.SecurePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AppSettings(
    val biometricEnabled: Boolean = false,
    val autoLogoutTimeMinutes: Int = 30,
    val cloudStorageEnabled: Boolean = false,
    val messageTTLHours: Int = 24,
    val screenshotBlockingEnabled: Boolean = true,
    val typingIndicatorsEnabled: Boolean = true,
    val themeMode: SecurePreferences.ThemeMode = SecurePreferences.ThemeMode.SYSTEM,
    val hasDecoyPassword: Boolean = false
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val securePreferences = SecurePreferences(application.applicationContext)
    
    private val _appSettings = MutableStateFlow(AppSettings())
    val appSettings: StateFlow<AppSettings> = _appSettings.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                _appSettings.value = AppSettings(
                    biometricEnabled = securePreferences.isBiometricEnabled,
                    autoLogoutTimeMinutes = securePreferences.autoLogoutTimeMinutes,
                    cloudStorageEnabled = securePreferences.isCloudStorageEnabled,
                    messageTTLHours = securePreferences.messageTTLHours,
                    screenshotBlockingEnabled = securePreferences.isScreenshotBlockingEnabled,
                    typingIndicatorsEnabled = securePreferences.isTypingIndicatorsEnabled,
                    themeMode = securePreferences.themeMode,
                    hasDecoyPassword = securePreferences.decoyModePassword.isNotEmpty()
                )
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Error loading settings: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateBiometricAuthentication(enabled: Boolean) {
        securePreferences.isBiometricEnabled = enabled
        updateSettings { it.copy(biometricEnabled = enabled) }
    }
    
    fun updateAutoLogoutTime(minutes: Int) {
        securePreferences.autoLogoutTimeMinutes = minutes
        updateSettings { it.copy(autoLogoutTimeMinutes = minutes) }
    }
    
    fun updateCloudStorage(enabled: Boolean) {
        securePreferences.isCloudStorageEnabled = enabled
        updateSettings { it.copy(cloudStorageEnabled = enabled) }
    }
    
    fun updateMessageTTL(hours: Int) {
        securePreferences.messageTTLHours = hours
        updateSettings { it.copy(messageTTLHours = hours) }
    }
    
    fun updateScreenshotBlocking(enabled: Boolean) {
        securePreferences.isScreenshotBlockingEnabled = enabled
        updateSettings { it.copy(screenshotBlockingEnabled = enabled) }
    }
    
    fun updateTypingIndicators(enabled: Boolean) {
        securePreferences.isTypingIndicatorsEnabled = enabled
        updateSettings { it.copy(typingIndicatorsEnabled = enabled) }
    }
    
    fun updateThemeMode(themeMode: SecurePreferences.ThemeMode) {
        securePreferences.themeMode = themeMode
        updateSettings { it.copy(themeMode = themeMode) }
    }
    
    fun updateDecoyPassword(password: String) {
        securePreferences.decoyModePassword = password
        updateSettings { it.copy(hasDecoyPassword = password.isNotEmpty()) }
    }
    
    private fun updateSettings(update: (AppSettings) -> AppSettings) {
        _appSettings.value = update(_appSettings.value)
    }
} 