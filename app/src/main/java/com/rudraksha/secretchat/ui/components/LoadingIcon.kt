package com.rudraksha.secretchat.ui.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun LoadingIcon() {
    val infiniteTransition = rememberInfiniteTransition(
        label = "Progress Animation"
    )
    val progress = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800)
        ),
        label = "progress value"
    )
    Box {
        CircularProgressIndicator(
            progress = {
                progress.value
            },
            modifier = Modifier.size(24.dp),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            strokeWidth = 2.dp,
            trackColor = Color.Transparent,
        )
    }
}