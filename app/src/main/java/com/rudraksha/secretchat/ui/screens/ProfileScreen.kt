package com.rudraksha.secretchat.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.core.net.toFile
import com.rudraksha.secretchat.R
import com.rudraksha.secretchat.data.model.ProfileScreenState
import com.rudraksha.secretchat.ui.components.FunctionalityNotAvailablePopup
import com.rudraksha.secretchat.ui.theme.SecretChatTheme
import kotlin.math.roundToInt

@Composable
fun ProfileScreen(
    userData: ProfileScreenState,
    nestedScrollInteropConnection: NestedScrollConnection = rememberNestedScrollInteropConnection()
) {
    var functionalityNotAvailablePopupShown by remember { mutableStateOf(false) }
    if (functionalityNotAvailablePopupShown) {
        FunctionalityNotAvailablePopup { functionalityNotAvailablePopupShown = false }
    }

    val scrollState = rememberScrollState()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .nestedScroll(nestedScrollInteropConnection)
            .systemBarsPadding()
    ) {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
            ) {
                ProfileHeader(
                    scrollState,
                    userData,
                    this@BoxWithConstraints.maxHeight
                )
                UserInfoFields(userData, this@BoxWithConstraints.maxHeight)
            }
        }

        val fabExtended by remember { derivedStateOf { scrollState.value == 0 } }
        ProfileFab(
            extended = fabExtended,
            userIsMe = true,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                // Offsets the FAB to compensate for CoordinatorLayout collapsing behaviour
                .offset(y = ((-100).dp)),
            onFabClicked = { functionalityNotAvailablePopupShown = true }
        )
    }
}

@Composable
private fun UserInfoFields(userData: ProfileScreenState, containerHeight: Dp) {
    Column {
        Spacer(modifier = Modifier.height(8.dp))

        NameAndPosition(userData)

        ProfileProperty("Name", userData.name)

        if (userData.status) {
            Text(
                text = "Online",
                color = Color.Green
            )
        } else {
            Text(
                text = "Offline",
                color = Color.LightGray
            )
        }

//        userData.timeZone?.let {
//            ProfileProperty(stringResource(R.string.timezone), userData.timeZone)
//        }

        // Add a spacer that always shows part (320.dp) of the fields list regardless of the device,
        // in order to always leave some content at the top.
        Spacer(Modifier.height((containerHeight - 320.dp).coerceAtLeast(0.dp)))
    }
}

@Composable
private fun NameAndPosition(
    userData: ProfileScreenState
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Name(
            userData,
            modifier = Modifier.paddingFromBaseline(top = 32.dp)
        )
//        Position(
//            userData,
//            modifier = Modifier
//                .padding(bottom = 20.dp)
//                .paddingFromBaseline(top = 24.dp)
//        )
    }
}

@Composable
private fun Name(userData: ProfileScreenState, modifier: Modifier = Modifier) {
    Text(
        text = userData.name,
        modifier = modifier,
        style = MaterialTheme.typography.headlineLarge
    )
}

//@Composable
//private fun Position(userData: ProfileScreenState, modifier: Modifier = Modifier) {
//    Text(
//        text = userData.,
//        modifier = modifier,
//        style = MaterialTheme.typography.bodyLarge,
//        color = MaterialTheme.colorScheme.onSurfaceVariant
//    )
//}

@Composable
private fun ProfileHeader(
    scrollState: ScrollState,
    data: ProfileScreenState,
    containerHeight: Dp
) {
    val offset = (scrollState.value / 2)
    val offsetDp = with(LocalDensity.current) { offset.toDp() }

    data.profile?.let { uri ->
        Image(
            bitmap = BitmapFactory.decodeStream(uri.toFile().inputStream()).asImageBitmap(),
            modifier = Modifier
                .heightIn(max = containerHeight / 2)
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    top = offsetDp,
                    end = 16.dp
                )
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            contentDescription = null
        )
    }
}

@Composable
fun ProfileProperty(
    label: String, value: String, isLink: Boolean = false
) {
    Column(
        modifier = Modifier
            .padding(bottom = 16.dp)
    ) {
        HorizontalDivider()
        Text(
            text = label,
            modifier = Modifier.paddingFromBaseline(24.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        val style = if (isLink) {
            MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary)
        } else {
            MaterialTheme.typography.bodyLarge
        }
        Text(
            text = value,
            modifier = Modifier.paddingFromBaseline(24.dp),
            style = style
        )
    }
}

@Composable
fun ProfileFab(
    extended: Boolean,
    userIsMe: Boolean,
    modifier: Modifier = Modifier,
    onFabClicked: () -> Unit = { }
) {
    key(userIsMe) { // Prevent multiple invocations to execute during composition
        FloatingActionButton(
            onClick = onFabClicked,
            modifier = modifier
                .padding(16.dp)
                .navigationBarsPadding()
                .height(48.dp)
                .widthIn(min = 48.dp),
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ) {
            AnimatingFabContent(
                icon = {
                    Icon(
                        imageVector = if (userIsMe) Icons.Outlined.Create else Icons.Outlined.AccountCircle,
                        contentDescription = if (userIsMe) "Edit" else "Status"
                    )
                },
                text = {
                    Text(
                        text = if (userIsMe) "Edit message" else "Name"
                    )
                },
                extended = extended
            )
        }
    }
}

@Composable
fun AnimatingFabContent(
    icon: @Composable () -> Unit,
    text: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    extended: Boolean = true
) {
    val currentState = if (extended) ExpandableFabStates.Extended else ExpandableFabStates.Collapsed
    val transition = updateTransition(currentState, "fab_transition")

    val textOpacity by transition.animateFloat(
        transitionSpec = {
            if (targetState == ExpandableFabStates.Collapsed) {
                tween(
                    easing = LinearEasing,
                    durationMillis = (transitionDuration / 12f * 5).roundToInt() // 5 / 12 frames
                )
            } else {
                tween(
                    easing = LinearEasing,
                    delayMillis = (transitionDuration / 3f).roundToInt(), // 4 / 12 frames
                    durationMillis = (transitionDuration / 12f * 5).roundToInt() // 5 / 12 frames
                )
            }
        },
        label = "fab_text_opacity"
    ) { state ->
        if (state == ExpandableFabStates.Collapsed) {
            0f
        } else {
            1f
        }
    }
    val fabWidthFactor by transition.animateFloat(
        transitionSpec = {
            if (targetState == ExpandableFabStates.Collapsed) {
                tween(
                    easing = FastOutSlowInEasing,
                    durationMillis = transitionDuration
                )
            } else {
                tween(
                    easing = FastOutSlowInEasing,
                    durationMillis = transitionDuration
                )
            }
        },
        label = "fab_width_factor"
    ) { state ->
        if (state == ExpandableFabStates.Collapsed) {
            0f
        } else {
            1f
        }
    }
    // Deferring reads using lambdas instead of Floats here can improve performance,
    // preventing recompositions.
    IconAndTextRow(
        icon,
        text,
        { textOpacity },
        { fabWidthFactor },
        modifier = modifier
    )
}

@Composable
private fun IconAndTextRow(
    icon: @Composable () -> Unit,
    text: @Composable () -> Unit,
    opacityProgress: () -> Float, // Lambdas instead of Floats, to defer read
    widthProgress: () -> Float,
    modifier: Modifier
) {
    Layout(
        modifier = modifier,
        content = {
            icon()
            Box(modifier = Modifier.graphicsLayer { alpha = opacityProgress() }) {
                text()
            }
        }
    ) { measurables, constraints ->

        val iconPlaceable = measurables[0].measure(constraints)
        val textPlaceable = measurables[1].measure(constraints)

        val height = constraints.maxHeight

        // FAB has an aspect ratio of 1 so the initial width is the height
        val initialWidth = height.toFloat()

        // Use it to get the padding
        val iconPadding = (initialWidth - iconPlaceable.width) / 2f

        // The full width will be : padding + icon + padding + text + padding
        val expandedWidth = iconPlaceable.width + textPlaceable.width + iconPadding * 3

        // Apply the animation factor to go from initialWidth to fullWidth
        val width = lerp(initialWidth, expandedWidth, widthProgress())

        layout(width.roundToInt(), height) {
            iconPlaceable.place(
                iconPadding.roundToInt(),
                constraints.maxHeight / 2 - iconPlaceable.height / 2
            )
            textPlaceable.place(
                (iconPlaceable.width + iconPadding * 2).roundToInt(),
                constraints.maxHeight / 2 - textPlaceable.height / 2
            )
        }
    }
}

private enum class ExpandableFabStates { Collapsed, Extended }

private const val transitionDuration = 200