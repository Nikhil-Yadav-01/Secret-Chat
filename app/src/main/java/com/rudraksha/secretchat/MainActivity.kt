package com.rudraksha.secretchat

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.rudraksha.secretchat.data.entity.MessageEntity
import com.rudraksha.secretchat.navigation.NavigationManager
import com.rudraksha.secretchat.ui.theme.SecretChatTheme
import com.rudraksha.secretchat.utils.ScreenshotBlocker
import com.rudraksha.secretchat.utils.SecurePreferences
import com.rudraksha.secretchat.utils.SessionManager
import com.rudraksha.secretchat.viewmodels.AuthViewModel

class MainActivity : ComponentActivity() {
    private lateinit var securePreferences: SecurePreferences
    private lateinit var sessionManager: SessionManager
    private lateinit var authViewModel: AuthViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize security components
        securePreferences = SecurePreferences(applicationContext)
        
        // Setup auth view model
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        
        // Setup session manager for auto-logout
        sessionManager = SessionManager(
            context = application,
            securePreferences = securePreferences,
            logoutCallback = { 
                // Auto logout handler
                authViewModel.logout()
            }
        )
        
        // Apply screenshot blocking based on settings
        if (securePreferences.isScreenshotBlockingEnabled) {
            ScreenshotBlocker.enable(this)
        }

        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition {
            authViewModel.isLoading.value
        }
        
        setContent {
            SecretChatTheme {
                val navHostController = rememberNavController()
                Log.d("Time 2 ", System.currentTimeMillis().toString())
                NavigationManager(
                    navController = navHostController,
                    authViewModel = authViewModel,
                    context = this,
                )
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        
        // Apply screenshot blocking whenever activity resumes
        ScreenshotBlocker.applyFromSettings(this, securePreferences.isScreenshotBlockingEnabled)
        
        // Update session activity
        sessionManager.updateLastActive()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Clean up session manager
        sessionManager.cleanup()
    }
}

@Preview
@Composable
fun ChatBubble(messageEntity: MessageEntity = MessageEntity(senderId = "se")) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .background(
                if (messageEntity.senderId == "default") 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            )
            .padding(8.dp),
        contentAlignment = if (messageEntity.senderId == "default") Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Text(
            text = messageEntity.content ?: "", 
            color = if (messageEntity.senderId == "default")
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
