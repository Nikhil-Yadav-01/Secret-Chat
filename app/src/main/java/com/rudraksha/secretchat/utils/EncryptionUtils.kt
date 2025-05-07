package com.rudraksha.secretchat.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Utility class for encryption/decryption operations
 * Provides methods for AES-256 and RSA-2048 encryption
 */
object EncryptionUtils {
    private const val RSA_KEY_SIZE = 2048
    private const val AES_KEY_SIZE = 256
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128
    private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
    private const val RSA_TRANSFORMATION = "RSA/ECB/PKCS1Padding"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    
    /**
     * Generate RSA Key Pair
     */
    fun generateRSAKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(RSA_KEY_SIZE)
        return keyPairGenerator.generateKeyPair()
    }
    
    /**
     * Generate AES Secret Key
     */
    fun generateAESKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(AES_KEY_SIZE)
        return keyGenerator.generateKey()
    }
    
    /**
     * Encrypt data using AES-256 GCM
     * @param data The data to encrypt
     * @param secretKey The AES secret key
     * @return A Base64 encoded string containing IV + ciphertext
     */
    fun encryptWithAES(data: String, secretKey: SecretKey): String {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        val iv = ByteArray(GCM_IV_LENGTH)
        // In a real app, use a secure random to generate IV
        java.security.SecureRandom().nextBytes(iv)
        
        val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec)
        
        val ciphertext = cipher.doFinal(data.toByteArray())
        
        // Concatenate IV and ciphertext
        val encryptedData = ByteArray(iv.size + ciphertext.size)
        System.arraycopy(iv, 0, encryptedData, 0, iv.size)
        System.arraycopy(ciphertext, 0, encryptedData, iv.size, ciphertext.size)
        
        return Base64.encodeToString(encryptedData, Base64.NO_WRAP)
    }
    
    /**
     * Decrypt data using AES-256 GCM
     * @param encryptedData Base64 encoded string containing IV + ciphertext
     * @param secretKey The AES secret key
     * @return The decrypted data
     */
    fun decryptWithAES(encryptedData: String, secretKey: SecretKey): String {
        val decodedData = Base64.decode(encryptedData, Base64.NO_WRAP)
        
        // Extract IV from the beginning of the data
        val iv = ByteArray(GCM_IV_LENGTH)
        System.arraycopy(decodedData, 0, iv, 0, iv.size)
        
        // Extract ciphertext
        val ciphertext = ByteArray(decodedData.size - iv.size)
        System.arraycopy(decodedData, iv.size, ciphertext, 0, ciphertext.size)
        
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec)
        
        return String(cipher.doFinal(ciphertext))
    }
    
    /**
     * Encrypt data using RSA public key
     * @param data The data to encrypt
     * @param publicKey The RSA public key
     * @return Base64 encoded encrypted data
     */
    fun encryptWithRSA(data: ByteArray, publicKey: PublicKey): String {
        val cipher = Cipher.getInstance(RSA_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encryptedBytes = cipher.doFinal(data)
        return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
    }
    
    /**
     * Decrypt data using RSA private key
     * @param encryptedData Base64 encoded encrypted data
     * @param privateKey The RSA private key
     * @return The decrypted data
     */
    fun decryptWithRSA(encryptedData: String, privateKey: PrivateKey): ByteArray {
        val cipher = Cipher.getInstance(RSA_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val encryptedBytes = Base64.decode(encryptedData, Base64.NO_WRAP)
        return cipher.doFinal(encryptedBytes)
    }
    
    /**
     * Convert a secret key to a Base64 string for transmission
     */
    fun secretKeyToString(secretKey: SecretKey): String {
        return Base64.encodeToString(secretKey.encoded, Base64.NO_WRAP)
    }
    
    /**
     * Recreate a secret key from a Base64 string
     */
    fun stringToSecretKey(encodedKey: String): SecretKey {
        val decodedKey = Base64.decode(encodedKey, Base64.NO_WRAP)
        return SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")
    }
    
    /**
     * Convert public key to Base64 string
     */
    fun publicKeyToString(publicKey: PublicKey): String {
        return Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP)
    }
    
    /**
     * Convert Base64 string to public key
     */
    fun stringToPublicKey(encodedPublicKey: String): PublicKey {
        val keyFactory = KeyFactory.getInstance("RSA")
        val decodedKey = Base64.decode(encodedPublicKey, Base64.NO_WRAP)
        val keySpec = X509EncodedKeySpec(decodedKey)
        return keyFactory.generatePublic(keySpec)
    }
    
    /**
     * Convert private key to Base64 string
     */
    fun privateKeyToString(privateKey: PrivateKey): String {
        return Base64.encodeToString(privateKey.encoded, Base64.NO_WRAP)
    }
    
    /**
     * Convert Base64 string to private key
     */
    fun stringToPrivateKey(encodedPrivateKey: String): PrivateKey {
        val keyFactory = KeyFactory.getInstance("RSA")
        val decodedKey = Base64.decode(encodedPrivateKey, Base64.NO_WRAP)
        val keySpec = PKCS8EncodedKeySpec(decodedKey)
        return keyFactory.generatePrivate(keySpec)
    }
} 