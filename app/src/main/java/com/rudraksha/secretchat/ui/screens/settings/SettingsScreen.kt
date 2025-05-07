package com.rudraksha.secretchat.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudraksha.secretchat.ui.components.AppBackground
import com.rudraksha.secretchat.utils.SecurePreferences
import com.rudraksha.secretchat.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SettingsScreen(
    navigateBack: () -> Unit = {},
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val appSettings by settingsViewModel.appSettings.collectAsState()
    val isLoading by settingsViewModel.isLoading.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Background
            AppBackground()
            
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                appSettings?.let { settings ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // Security Settings Section
                        SectionHeader(title = "Security", icon = Icons.Default.Security)
                        
                        // Biometric Authentication
                        SettingsSwitchItem(
                            title = "Biometric Authentication",
                            subtitle = "Use fingerprint or face ID to unlock the app",
                            icon = Icons.Default.Fingerprint,
                            checked = settings.biometricEnabled,
                            onCheckedChange = { settingsViewModel.updateBiometricAuthentication(it) }
                        )
                        
                        // Auto Logout
                        Spacer(modifier = Modifier.height(8.dp))
                        SettingsSliderItem(
                            title = "Auto Logout",
                            subtitle = "Minutes of inactivity before logout: ${settings.autoLogoutTimeMinutes}",
                            icon = Icons.Default.Schedule,
                            value = settings.autoLogoutTimeMinutes.toFloat(),
                            valueRange = 1f..60f,
                            steps = 59,
                            onValueChange = { 
                                settingsViewModel.updateAutoLogoutTime(it.toInt())
                            }
                        )
                        
                        // Decoy Mode Password
                        Spacer(modifier = Modifier.height(8.dp))
                        SettingsActionItem(
                            title = "Decoy Mode Password",
                            subtitle = if (settings.hasDecoyPassword) "Password set" else "Not set",
                            icon = Icons.Default.Lock,
                            onClick = {
                                // Show decoy password dialog
                            }
                        )
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                        
                        // Storage Settings Section
                        SectionHeader(title = "Storage", icon = Icons.Default.Storage)
                        
                        // Cloud Storage
                        SettingsSwitchItem(
                            title = "Cloud Storage",
                            subtitle = "Store messages in the cloud",
                            icon = Icons.Default.Storage,
                            checked = settings.cloudStorageEnabled,
                            onCheckedChange = { settingsViewModel.updateCloudStorage(it) }
                        )
                        
                        // Message Time-to-Live
                        Spacer(modifier = Modifier.height(8.dp))
                        SettingsSliderItem(
                            title = "Message Time-to-Live",
                            subtitle = "Hours until messages are deleted: ${settings.messageTTLHours}",
                            icon = Icons.Default.Schedule,
                            value = settings.messageTTLHours.toFloat(),
                            valueRange = 1f..168f, // 1 hour to 7 days
                            steps = 167,
                            onValueChange = { 
                                settingsViewModel.updateMessageTTL(it.toInt())
                            }
                        )
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                        
                        // Privacy Settings Section
                        SectionHeader(title = "Privacy", icon = Icons.Default.VisibilityOff)
                        
                        // Screenshot Blocking
                        SettingsSwitchItem(
                            title = "Screenshot Blocking",
                            subtitle = "Prevent taking screenshots of the app",
                            icon = Icons.Default.VisibilityOff,
                            checked = settings.screenshotBlockingEnabled,
                            onCheckedChange = { settingsViewModel.updateScreenshotBlocking(it) }
                        )
                        
                        // Typing Indicators
                        Spacer(modifier = Modifier.height(8.dp))
                        SettingsSwitchItem(
                            title = "Typing Indicators",
                            subtitle = "Show when others are typing",
                            icon = Icons.Default.Visibility,
                            checked = settings.typingIndicatorsEnabled,
                            onCheckedChange = { settingsViewModel.updateTypingIndicators(it) }
                        )
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                        
                        // Appearance Settings Section
                        SectionHeader(title = "Appearance", icon = Icons.Default.WbSunny)
                        
                        // Theme Mode
                        var showThemeDropdown by remember { mutableStateOf(false) }
                        
                        SettingsActionItem(
                            title = "Theme",
                            subtitle = when (settings.themeMode) {
                                SecurePreferences.ThemeMode.LIGHT -> "Light"
                                SecurePreferences.ThemeMode.DARK -> "Dark"
                                SecurePreferences.ThemeMode.SYSTEM -> "System Default"
                            },
                            icon = Icons.Default.WbSunny,
                            onClick = { showThemeDropdown = true }
                        )
                        
                        Box {
                            DropdownMenu(
                                expanded = showThemeDropdown,
                                onDismissRequest = { showThemeDropdown = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Light") },
                                    onClick = {
                                        settingsViewModel.updateThemeMode(SecurePreferences.ThemeMode.LIGHT)
                                        showThemeDropdown = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Dark") },
                                    onClick = {
                                        settingsViewModel.updateThemeMode(SecurePreferences.ThemeMode.DARK)
                                        showThemeDropdown = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("System Default") },
                                    onClick = {
                                        settingsViewModel.updateThemeMode(SecurePreferences.ThemeMode.SYSTEM)
                                        showThemeDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingsSliderItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit
) {
    var sliderValue by remember { mutableFloatStateOf(value) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = sliderValue,
            onValueChange = { 
                sliderValue = it
            },
            onValueChangeFinished = {
                onValueChange(sliderValue)
            },
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

@Composable
fun SettingsActionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 