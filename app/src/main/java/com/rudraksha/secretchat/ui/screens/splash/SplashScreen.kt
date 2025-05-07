package com.rudraksha.secretchat.ui.screens.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rudraksha.secretchat.data.entity.UserEntity
import com.rudraksha.secretchat.ui.components.AppBackground
import com.rudraksha.secretchat.ui.components.SecretChatLogo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Composable
fun SplashScreen(
    navigateToRegister: () -> Unit,
    navigateToHome: () -> Unit,
    observeCurrentUserEntity: StateFlow<UserEntity?>,
) {
    val currentUser by observeCurrentUserEntity.collectAsState()
    LaunchedEffect(currentUser) {
        delay(500) // Optional: Show splash for a bit
        if (currentUser == null) {
            navigateToRegister()
        } else {
            navigateToHome()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Background from login screen for consistency
        AppBackground()
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App logo and branding
            SecretChatLogo(modifier = Modifier.size(120.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "SECRET CHAT",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineLarge,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Secure. Private. Invisible.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium,
                letterSpacing = 1.sp
            )
        }

        Box(
            Modifier.fillMaxWidth().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "From Rudraksha",
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Preview
@Composable
fun SplashScreenPreview(){
    SplashScreen(
        {},
        {},
        MutableStateFlow(UserEntity()).asStateFlow()
    )
}