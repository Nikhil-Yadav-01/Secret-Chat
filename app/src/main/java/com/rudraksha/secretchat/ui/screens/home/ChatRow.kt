package com.rudraksha.secretchat.ui.screens.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rudraksha.secretchat.data.model.ChatItem
import com.rudraksha.secretchat.ui.theme.circleShape

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatRow(
    chat: ChatItem,
    delete: (ChatItem) -> Unit = {},
    onClick: (ChatItem) -> Unit = {},
    onLongPress: () -> Unit = {}
) {
    var longPressed by remember { mutableStateOf(false) }
    val elevation by animateDpAsState(
        targetValue = 4.dp,
        label = ""
    )
    val swipeState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.StartToEnd) {
                delete(chat)
                true
            } else {
                false
            }
        }
    )

    Card {
        SwipeToDismissBox(
            state = swipeState,
            backgroundContent = {
                val color by animateColorAsState(
                    when (swipeState.targetValue) {
                        SwipeToDismissBoxValue.Settled -> MaterialTheme.colorScheme.background
                        else -> MaterialTheme.colorScheme.tertiary
                    },
                    label = ""
                )
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(color)
                )
            },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .combinedClickable(
                        onClick = {
                            longPressed = false
                            onClick(chat)
                        },
                        onLongClick = {
                            onLongPress()
                            longPressed = true
                        }
                    )
                    .padding(if (longPressed) elevation else 0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = chat.profilePic),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(circleShape)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.tertiary,
                            circleShape
                        )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        chat.name,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        chat.lastMessage,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        chat.lastEventAt,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 12.sp
                    )
                    if (chat.unreadCount > 0) {
                        BadgeBox(chat.unreadCount)
                    }
                }
            }
        }
    }
}