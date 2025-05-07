package com.rudraksha.secretchat.utils

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.SecretKey

/**
 * Manages cryptographic keys for the application
 * Handles generation, storage, and retrieval of RSA and AES keys
 */
class KeyManager(private val context: Context) {
    
    companion object {
        private const val KEY_ALIAS_RSA = "secret_chat_rsa_key"
        private const val PREFS_NAME = "secret_chat_keys"
        private const val PREF_PUBLIC_KEY = "public_key"
        private const val PREF_CHAT_KEYS = "chat_keys_"
    }
    
    private val masterKey: MasterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    /**
     * Get or generate RSA key pair
     */
    fun getOrGenerateRSAKeyPair(): KeyPair {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        
        // Check if key already exists
        if (keyStore.containsAlias(KEY_ALIAS_RSA)) {
            val privateKey = keyStore.getKey(KEY_ALIAS_RSA, null) as PrivateKey
            val publicKey = keyStore.getCertificate(KEY_ALIAS_RSA).publicKey
            return KeyPair(publicKey, privateKey)
        }
        
        // Generate new key pair
        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore"
        )
        
        val parameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS_RSA,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).apply {
            setKeySize(2048)
            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
            setRandomizedEncryptionRequired(true)
        }.build()
        
        keyPairGenerator.initialize(parameterSpec)
        return keyPairGenerator.generateKeyPair()
    }
    
    /**
     * Get RSA private key
     */
    fun getRSAPrivateKey(): PrivateKey? {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        
        if (keyStore.containsAlias(KEY_ALIAS_RSA)) {
            return keyStore.getKey(KEY_ALIAS_RSA, null) as PrivateKey
        }
        return null
    }
    
    /**
     * Get RSA public key
     */
    fun getRSAPublicKey(): PublicKey? {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        
        if (keyStore.containsAlias(KEY_ALIAS_RSA)) {
            return keyStore.getCertificate(KEY_ALIAS_RSA).publicKey
        }
        return null
    }
    
    /**
     * Save public key of another user
     */
    fun saveUserPublicKey(userId: String, publicKeyString: String) {
        sharedPreferences.edit().putString("$PREF_PUBLIC_KEY-$userId", publicKeyString).apply()
    }
    
    /**
     * Get public key of another user
     */
    fun getUserPublicKey(userId: String): PublicKey? {
        val keyString = sharedPreferences.getString("$PREF_PUBLIC_KEY-$userId", null) ?: return null
        return EncryptionUtils.stringToPublicKey(keyString)
    }
    
    /**
     * Save AES key for a chat
     */
    fun saveChatKey(chatId: String, secretKeyString: String) {
        sharedPreferences.edit().putString("$PREF_CHAT_KEYS$chatId", secretKeyString).apply()
    }
    
    /**
     * Get AES key for a chat
     */
    fun getChatKey(chatId: String): SecretKey? {
        val keyString = sharedPreferences.getString("$PREF_CHAT_KEYS$chatId", null) ?: return null
        return EncryptionUtils.stringToSecretKey(keyString)
    }
    
    /**
     * Generate a new AES key for a chat and save it
     */
    fun generateAndSaveChatKey(chatId: String): SecretKey {
        val secretKey = EncryptionUtils.generateAESKey()
        saveChatKey(chatId, EncryptionUtils.secretKeyToString(secretKey))
        return secretKey
    }
    
    /**
     * Securely share an AES key with another user using their public key
     */
    fun encryptKeyForUser(secretKey: SecretKey, userPublicKey: PublicKey): String {
        val keyData = secretKey.encoded
        return EncryptionUtils.encryptWithRSA(keyData, userPublicKey)
    }
    
    /**
     * Decrypt an AES key shared by another user
     */
    fun decryptSharedKey(encryptedKeyData: String): SecretKey {
        val privateKey = getRSAPrivateKey() ?: throw IllegalStateException("RSA private key not found")
        val keyData = EncryptionUtils.decryptWithRSA(encryptedKeyData, privateKey)
        return javax.crypto.spec.SecretKeySpec(keyData, "AES")
    }
} 