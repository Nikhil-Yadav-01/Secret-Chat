package com.rudraksha.secretchat.ui.screens.create

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.rudraksha.secretchat.ui.theme.circleShape

@Preview
@Composable
fun UserRow(
    user: UserItem = UserItem(
        name = "Full Name",
    ),
    onClick: (UserItem) -> Unit = {},
    selected: Boolean = false
) {
    Card(
        onClick = {
            onClick(user)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = user.imageUrl,
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
            Icon(
                imageVector = if (selected) Icons.Filled.Close else Icons.Filled.Check,
                contentDescription = "selected"
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                user.name,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

        }
    }
}

data class UserItem(
    val username: String = "",
    val name: String = "",
    val imageUrl: String = ""
)