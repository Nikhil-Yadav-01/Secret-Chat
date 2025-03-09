package com.rudraksha.secretchat.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun MessageInput(sendMessage: (String) -> Unit, scope: CoroutineScope) {
    var content by remember { mutableStateOf("") }

    Row(
        modifier = Modifier.background(Color.Transparent),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            modifier = Modifier
                .weight(0.8f)
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large),
            maxLines = 5,
            placeholder = { Text("Enter message to send") },
            shape = MaterialTheme.shapes.large
        )
        IconButton(
            onClick = {
                scope.launch {
                    sendMessage(content)
                    content = ""
                }
            },
            modifier = Modifier.padding(start = 2.dp).clip(RoundedCornerShape(percent = 50)),

            ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "send",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
        }
    }
}
