package com.rudraksha.secretchat.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun BottomNavigationBar(
    onProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    var selectedItem by remember { mutableIntStateOf(0) }

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.AutoMirrored.Filled.Chat,
                    contentDescription = "Chats",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            label = { Text("Chats") },
            selected = selectedItem == 0,
            onClick = { selectedItem = 0 }
        )

        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Filled.Call,
                    contentDescription = "Calls",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            label = { Text("Calls") },
            selected = selectedItem == 1,
            onClick = { selectedItem = 1 }
        )

        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            label = { Text("Profile") },
            selected = selectedItem == 2,
            onClick = {
                selectedItem = 2
                onProfileClick()
            }
        )

        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            label = { Text("Settings") },
            selected = selectedItem == 3,
            onClick = {
                selectedItem = 3
                onSettingsClick()
            }
        )
    }
}