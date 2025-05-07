package com.rudraksha.secretchat.ui.screens.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.rudraksha.secretchat.data.WebSocketData
import com.rudraksha.secretchat.ui.components.SearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun NotificationsScreen(
    modifier: Modifier = Modifier,
    onNavIconClick: () -> Unit = {},
    navigateBack: () -> Unit = {},
    notificationsList: List<NotificationsItem> = listOf(),
    acceptJoinRequest: (WebSocketData.JoinResponse) -> Unit = {},
    rejectJoinRequest: (WebSocketData.JoinResponse) -> Unit = {},
) {
    var showSearch by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (showSearch) {
                        SearchBar(
                            text = "Search notifications",
                            leadingIcon = {
                                IconButton(
                                    onClick = {
                                        if (showSearch) showSearch = false
                                        else navigateBack()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        )
                    } else {
                        Text(
                            "Notifications",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                },
                navigationIcon = {
                    if (!showSearch) {
                        IconButton(
                            onClick = {
                                navigateBack()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                },
                actions = {
                    if (!showSearch) {
                        IconButton(onClick = {
                            showSearch = true
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "search"
                            )
                        }
                    }
                }
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            items(notificationsList) { item ->
                NotificationRow(
                    notificationsItem = item
                )
            }
        }
    }
}

data class NotificationsItem(
    val name: String = "",
    val imageUrl: String? = null,
)