package com.rudraksha.secretchat.viewmodels

import android.app.Application
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudraksha.secretchat.data.entity.UserEntity
import com.rudraksha.secretchat.data.model.ProfileScreenState
import com.rudraksha.secretchat.database.ChatDatabase
import com.rudraksha.secretchat.utils.SecurePreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    
    private val userDao = ChatDatabase.getDatabase(application.applicationContext).userDao()
    private val securePreferences = SecurePreferences(application.applicationContext)
    private val contentResolver: ContentResolver = application.contentResolver
    private val appContext = application.applicationContext
    
    private val _userProfile = MutableStateFlow<ProfileScreenState?>(null)
    val userProfile: StateFlow<ProfileScreenState?> = _userProfile.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _savedImageUri = MutableStateFlow<Uri?>(null)
    val savedImageUri: StateFlow<Uri?> = _savedImageUri.asStateFlow()
    
    init {
        loadUserProfile()
    }
    
    private fun loadUserProfile() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val userId = securePreferences.userId
                val user = withContext(Dispatchers.IO) {
                    userDao.getUserByUsername(userId)
                }
                
                user?.let { userEntity ->
                    // Check if profile picture exists
                    val profilePictureUri = if (userEntity.profilePictureUrl.isNotEmpty()) {
                        val file = File(userEntity.profilePictureUrl)
                        if (file.exists()) {
                            FileProvider.getUriForFile(
                                appContext,
                                "${appContext.packageName}.fileprovider",
                                file
                            )
                        } else null
                    } else null
                    
                    _userProfile.value = ProfileScreenState(
                        userId = userEntity.username,
                        name = userEntity.fullName,
                        status = userEntity.online,
                        desc = userEntity.description,
                        profile = profilePictureUri
                    )
                    
                    _savedImageUri.value = profilePictureUri
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error loading profile: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateUserProfile(
        fullName: String,
        bio: String
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val userId = securePreferences.userId
                val currentUser = withContext(Dispatchers.IO) {
                    userDao.getUserByUsername(userId)
                }
                
                currentUser?.let { user ->
                    val updatedUser = user.copy(
                        fullName = fullName,
                        description = bio,
                        profilePictureUrl = _savedImageUri.value?.path ?: user.profilePictureUrl
                    )
                    
                    withContext(Dispatchers.IO) {
                        userDao.updateUser(updatedUser)
                    }
                    
                    // Update the profile state
                    _userProfile.update { currentProfile ->
                        currentProfile?.copy(
                            name = fullName,
                            desc = bio
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error updating profile: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun saveImageToInternalStorage(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)
                    }
                }
                
                bitmap?.let {
                    // Save to internal storage
                    val username = securePreferences.userId
                    val fileName = "profile_${username}_${System.currentTimeMillis()}.jpg"
                    val file = File(appContext.filesDir, fileName)
                    
                    withContext(Dispatchers.IO) {
                        FileOutputStream(file).use { outputStream ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                            outputStream.flush()
                        }
                    }
                    
                    // Create content URI
                    val savedUri = FileProvider.getUriForFile(
                        appContext,
                        "${appContext.packageName}.fileprovider",
                        file
                    )
                    
                    _savedImageUri.value = savedUri
                    
                    // Update profile state with new image
                    _userProfile.update { currentProfile ->
                        currentProfile?.copy(profile = savedUri)
                    }
                    
                    // Update user entity with image path
                    val currentUser = withContext(Dispatchers.IO) {
                        userDao.getUserByUsername(username)
                    }
                    
                    currentUser?.let { user ->
                        val updatedUser = user.copy(profilePictureUrl = file.absolutePath)
                        withContext(Dispatchers.IO) {
                            userDao.updateUser(updatedUser)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error saving image: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
} 