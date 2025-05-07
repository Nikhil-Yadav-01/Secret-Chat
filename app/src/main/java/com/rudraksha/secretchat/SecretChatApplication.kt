package com.rudraksha.secretchat

import android.app.Application
import android.util.Log
import androidx.biometric.BiometricManager
import com.rudraksha.secretchat.utils.BiometricAuthManager
import com.rudraksha.secretchat.utils.KeyManager
import com.rudraksha.secretchat.utils.SecurePreferences

class SecretChatApplication : Application() {
    
    lateinit var securePreferences: SecurePreferences
        private set
    
    lateinit var keyManager: KeyManager
        private set
    
    lateinit var biometricManager: BiometricAuthManager
        private set
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize secure preferences
        securePreferences = SecurePreferences(applicationContext)
        
        // Initialize key manager
        keyManager = KeyManager(applicationContext)
        
        // Initialize biometric manager
        biometricManager = BiometricAuthManager(applicationContext)
        
        // Pre-generate RSA keys if possible
        initializeKeys()
        
        // Log available security features
        logSecurityCapabilities()
    }
    
    private fun initializeKeys() {
        try {
            // Generate RSA keys in background
            Thread {
                keyManager.getOrGenerateRSAKeyPair()
            }.start()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize keys: ${e.message}")
        }
    }
    
    private fun logSecurityCapabilities() {
        val biometricCapability = BiometricManager.from(this)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        
        val biometricAvailable = when (biometricCapability) {
            BiometricManager.BIOMETRIC_SUCCESS -> "available"
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "no hardware"
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "hardware unavailable"
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "not enrolled"
            else -> "unknown status: $biometricCapability"
        }
        
        Log.d(TAG, "Biometric authentication: $biometricAvailable")
    }
    
    companion object {
        private const val TAG = "SecretChatApplication"
    }
} 