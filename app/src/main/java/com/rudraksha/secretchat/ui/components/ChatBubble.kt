package com.rudraksha.secretchat.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rudraksha.secretchat.data.entity.MessageEntity
import com.rudraksha.secretchat.data.model.MessageType
import com.rudraksha.secretchat.ui.theme.ElectricBlue
import com.rudraksha.secretchat.ui.theme.ErrorRed
import com.rudraksha.secretchat.ui.theme.GoldAccent
import com.rudraksha.secretchat.ui.theme.TextPrimary
import com.rudraksha.secretchat.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatBubble(
    message: MessageEntity,
    isFromCurrentUser: Boolean,
    modifier: Modifier = Modifier
) {
    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { 
        timeFormatter.format(Date(message.timestamp)) 
    }
    
    val alignment = if (isFromCurrentUser) Alignment.End else Alignment.Start
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        horizontalAlignment = alignment
    ) {
        // Sender name if not current user
        if (!isFromCurrentUser) {
            Text(
                text = message.senderId,
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
            )
        }
        
        // Message bubble with appropriate style
        when {
            message.type == MessageType.SELF_DESTRUCT -> {
                SelfDestructMessageBubble(
                    modifier = Modifier
                        .widthIn(max = 300.dp)
                ) {
                    MessageContent(message, isFromCurrentUser, formattedTime)
                }
            }
            isFromCurrentUser -> {
                SentMessageBubble(
                    modifier = Modifier
                        .widthIn(max = 300.dp)
                ) {
                    MessageContent(message, isFromCurrentUser, formattedTime)
                }
            }
            else -> {
                ReceivedMessageBubble(
                    modifier = Modifier
                        .widthIn(max = 300.dp)
                ) {
                    MessageContent(message, isFromCurrentUser, formattedTime)
                }
            }
        }
    }
}

@Composable
private fun MessageContent(
    message: MessageEntity,
    isFromCurrentUser: Boolean,
    formattedTime: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(12.dp)
    ) {
        // Message content
        Text(
            text = message.content ?: "",
            color = TextPrimary,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        // Time and status indicators
        Row(
            modifier = Modifier.align(Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Self-destruct icon if applicable
            if (message.type == MessageType.SELF_DESTRUCT) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Self-destructing message",
                    tint = ErrorRed,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            
            // Encrypted icon
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Encrypted",
                tint = GoldAccent,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            
            // Time
            Text(
                text = formattedTime,
                color = TextSecondary,
                fontSize = 11.sp
            )
            
            // Read status for current user's messages
            if (isFromCurrentUser) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (message.isRead) "✓✓" else "✓",
                    color = if (message.isRead) ElectricBlue else TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
} 