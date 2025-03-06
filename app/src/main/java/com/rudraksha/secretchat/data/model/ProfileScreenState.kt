package com.rudraksha.secretchat.data.model

import android.net.Uri
import androidx.compose.runtime.Immutable

@Immutable
data class ProfileScreenState(
    val userId: String,
    val profile: Uri? = null,
    val name: String,
    val status: Boolean,
    val desc: String,
)