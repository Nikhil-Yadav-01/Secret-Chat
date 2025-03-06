package com.rudraksha.secretchat.ui.screens.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun BottomNavigationBar() {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        val items = listOf(
            "Chats",
//            "Updates", "Communities",
            "Calls"
        )
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.AutoMirrored.Filled.Chat,
                    contentDescription = items[0],
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            label = { Text(items[0]) },
            selected = items[0] == "Chats",
            onClick = { /* Handle click */ }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Filled.Call,
                    contentDescription = items[1],
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            label = { Text(items[1]) },
            selected = items[1] == "Chats",
            onClick = { /* Handle click */ }
        )

    }
}