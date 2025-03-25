package com.rudraksha.secretchat.ui.screens.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.rudraksha.secretchat.data.model.ChatType
import com.rudraksha.secretchat.ui.screens.common.SearchBar
import com.rudraksha.secretchat.ui.screens.home.JoinRequest
import com.rudraksha.secretchat.ui.screens.home.JoinResponse

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SelectMembersScreen(
    modifier: Modifier = Modifier,
    chatType: ChatType = ChatType.GROUP,
    navigateBack: () -> Unit = {},
    onUserItemClick: (String) -> Unit = {},
    userList: List<UserItem> = listOf(),
    create: (List<String>, String) -> Unit = { _, _ -> },
//    sendJoinRequest: (JoinRequest) -> Unit = {},
//    acceptJoinRequest: (JoinResponse) -> Unit = {},
//    rejectJoinRequest: (JoinResponse) -> Unit = {},
) {
    val selectedUsers: MutableList<UserItem> = remember { mutableStateListOf() }
    var name by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (showSearch) {
                        SearchBar(text = "Search members",)
                    } else {
                        Text(
                            when (chatType) {
                                ChatType.GROUP -> "Create Group"
                                ChatType.PRIVATE -> "Chat with"
                                else -> "Go secret with"
                            },
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                navigationIcon = {
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
        floatingActionButton = {
            // Floating Action Button
            FloatingActionButton(
                onClick = { create(selectedUsers.map { it.username }, name) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "New Chat",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            if (chatType == ChatType.GROUP) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Chat name") },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    )
                }
            }

            item {
                LazyRow {
                    items(selectedUsers) { user ->
                        SelectedItem(user)
                    }
                }
            }

            items(userList) { user ->
                UserRow(
                    user = user,
                    onClick = { item ->
                        if (selectedUsers.contains(item)) {
                            selectedUsers.remove(item)
                        } else {
                            selectedUsers.add(item)
                        }
                    },
                    selected = selectedUsers.contains(user)
                )
            }
        }
    }
}

@Composable
fun SelectedItem(
    item: UserItem
) {
    Card {
        Column(
            modifier = Modifier.size(80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = "Picture",
                modifier = Modifier.clip(shape = RoundedCornerShape(percent = 50))
            )
            Text(
                text = item.name
            )
        }
    }
}