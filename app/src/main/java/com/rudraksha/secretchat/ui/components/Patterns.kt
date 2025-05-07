package com.rudraksha.secretchat.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rudraksha.secretchat.R

/* Network Pattern Background for Normal Chat */
@Composable
fun NetworkPatternBackground(
    modifier: Modifier = Modifier
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val baseColor = MaterialTheme.colorScheme.onBackground
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        backgroundColor,
                        backgroundColor.copy(alpha = 0.95f),
                    )
                )
            )
            .drawBehind {
                drawNetworkPattern(baseColor)
            }
    )
}

private fun DrawScope.drawNetworkPattern(baseColor: Color) {
    // Main grid lines
    val linesColor = baseColor.copy(alpha = 0.2f)
    val thinner = baseColor.copy(alpha = 0.15f)
    val nodeColor = baseColor.copy(alpha = 0.3f)
    
    // Main grid
    drawLine(
        color = linesColor,
        start = Offset(size.width * 0.1f, size.height * 0.1f),
        end = Offset(size.width * 0.5f, size.height * 0.5f),
        strokeWidth = 0.5.dp.toPx()
    )
    
    drawLine(
        color = linesColor,
        start = Offset(size.width * 0.5f, size.height * 0.5f),
        end = Offset(size.width * 0.9f, size.height * 0.1f),
        strokeWidth = 0.5.dp.toPx()
    )
    
    drawLine(
        color = linesColor,
        start = Offset(size.width * 0.1f, size.height * 0.9f),
        end = Offset(size.width * 0.5f, size.height * 0.5f),
        strokeWidth = 0.5.dp.toPx()
    )
    
    drawLine(
        color = linesColor,
        start = Offset(size.width * 0.5f, size.height * 0.5f),
        end = Offset(size.width * 0.9f, size.height * 0.9f),
        strokeWidth = 0.5.dp.toPx()
    )
    
    // Horizontal and vertical lines through center
    drawLine(
        color = linesColor,
        start = Offset(size.width * 0.1f, size.height * 0.5f),
        end = Offset(size.width * 0.9f, size.height * 0.5f),
        strokeWidth = 0.5.dp.toPx()
    )
    
    drawLine(
        color = linesColor,
        start = Offset(size.width * 0.5f, size.height * 0.1f),
        end = Offset(size.width * 0.5f, size.height * 0.9f),
        strokeWidth = 0.5.dp.toPx()
    )
    
    // Minor grid lines
    drawLine(
        color = thinner,
        start = Offset(size.width * 0.1f, size.height * 0.3f),
        end = Offset(size.width * 0.3f, size.height * 0.1f),
        strokeWidth = 0.5.dp.toPx()
    )
    
    drawLine(
        color = thinner,
        start = Offset(size.width * 0.7f, size.height * 0.1f),
        end = Offset(size.width * 0.9f, size.height * 0.3f),
        strokeWidth = 0.5.dp.toPx()
    )
    
    drawLine(
        color = thinner,
        start = Offset(size.width * 0.1f, size.height * 0.7f),
        end = Offset(size.width * 0.3f, size.height * 0.9f),
        strokeWidth = 0.5.dp.toPx()
    )
    
    drawLine(
        color = thinner,
        start = Offset(size.width * 0.7f, size.height * 0.9f),
        end = Offset(size.width * 0.9f, size.height * 0.7f),
        strokeWidth = 0.5.dp.toPx()
    )
    
    // Connection nodes
    drawCircle(
        color = nodeColor,
        radius = 4.dp.toPx(),
        center = Offset(size.width * 0.5f, size.height * 0.5f)
    )
    
    drawCircle(
        color = baseColor.copy(alpha = 0.2f),
        radius = 2.dp.toPx(),
        center = Offset(size.width * 0.1f, size.height * 0.1f)
    )
    
    drawCircle(
        color = baseColor.copy(alpha = 0.2f),
        radius = 2.dp.toPx(),
        center = Offset(size.width * 0.9f, size.height * 0.1f)
    )
    
    drawCircle(
        color = baseColor.copy(alpha = 0.2f),
        radius = 2.dp.toPx(),
        center = Offset(size.width * 0.1f, size.height * 0.9f)
    )
    
    drawCircle(
        color = baseColor.copy(alpha = 0.2f),
        radius = 2.dp.toPx(),
        center = Offset(size.width * 0.9f, size.height * 0.9f)
    )
}

/* Sacred Geometry Pattern Background for Secret Chat */
@Preview
@Composable
fun SacredGeometryBackground(
    modifier: Modifier = Modifier
) {
    val backgroundColor = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
    val accentColor = MaterialTheme.colorScheme.tertiary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val baseColor = MaterialTheme.colorScheme.onBackground
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        backgroundColor, 
                        backgroundColor
                    )
                )
            )
            .drawBehind {
                drawSacredGeometry(baseColor)
                
                // Add mystic glow effects
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            secondaryColor.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.2f, size.height * 0.8f),
                        radius = 800.dp.toPx()
                    )
                )
                
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.8f, size.height * 0.2f),
                        radius = 600.dp.toPx()
                    )
                )
            }
    )
}

private fun DrawScope.drawSacredGeometry(baseColor: Color) {
    val center = Offset(size.width / 2, size.height / 2)
    val outerRadius = minOf(size.width, size.height) * 0.4f
    val middleRadius = outerRadius * 0.6f
    val innerRadius = outerRadius * 0.3f
    
    val strokeColor = baseColor.copy(alpha = 0.15f)
    val thinnerStroke = baseColor.copy(alpha = 0.1f)
    val thinnestStroke = baseColor.copy(alpha = 0.08f)
    val nodeColor = baseColor.copy(alpha = 0.2f)
    val smallNodeColor = baseColor.copy(alpha = 0.15f)
    
    // Outer circle
    drawCircle(
        color = strokeColor,
        radius = outerRadius,
        center = center,
        style = Stroke(width = 0.5.dp.toPx())
    )
    
    // Middle circle
    drawCircle(
        color = strokeColor,
        radius = middleRadius,
        center = center,
        style = Stroke(width = 0.5.dp.toPx())
    )
    
    // Inner circle
    drawCircle(
        color = strokeColor,
        radius = innerRadius,
        center = center,
        style = Stroke(width = 0.5.dp.toPx())
    )
    
    // Hexagon path
    val hexPath = Path().apply {
        val hexPoints = (0..5).map {
            val angle = Math.PI / 3 * it - Math.PI / 6
            val x = center.x + outerRadius * Math.cos(angle).toFloat()
            val y = center.y + outerRadius * Math.sin(angle).toFloat()
            Offset(x, y)
        }
        
        moveTo(hexPoints[0].x, hexPoints[0].y)
        hexPoints.forEach { lineTo(it.x, it.y) }
        close()
    }
    
    drawPath(
        path = hexPath,
        color = thinnerStroke,
        style = Stroke(width = 0.5.dp.toPx())
    )
    
    // Star pattern (connecting lines)
    for (i in 0..5) {
        val angle1 = Math.PI / 3 * i - Math.PI / 6
        val x1 = center.x + outerRadius * Math.cos(angle1).toFloat()
        val y1 = center.y + outerRadius * Math.sin(angle1).toFloat()
        
        // Connect to center
        drawLine(
            color = thinnestStroke,
            start = Offset(x1, y1),
            end = center,
            strokeWidth = 0.3.dp.toPx()
        )
        
        // Connect to opposite point
        val angle2 = Math.PI / 3 * (i + 3) - Math.PI / 6
        val x2 = center.x + outerRadius * Math.cos(angle2).toFloat()
        val y2 = center.y + outerRadius * Math.sin(angle2).toFloat()
        
        drawLine(
            color = thinnestStroke,
            start = Offset(x1, y1),
            end = Offset(x2, y2),
            strokeWidth = 0.3.dp.toPx()
        )
        
        // Add small node at each hex corner
        drawCircle(
            color = smallNodeColor,
            radius = 1.5.dp.toPx(),
            center = Offset(x1, y1)
        )
    }
    
    // Center point
    drawCircle(
        color = nodeColor,
        radius = 3.dp.toPx(),
        center = center
    )
}

/* Login Background */
@Preview
@Composable
fun AppBackground(
    primary: Color = MaterialTheme.colorScheme.primary,
    accent: Color = MaterialTheme.colorScheme.tertiary,
    modifier: Modifier = Modifier,
) {
    val primaryDark = primary.copy(alpha = 0.8f)
    val accentColor = accent
    val baseColor = MaterialTheme.colorScheme.onBackground
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        primaryDark
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            .drawBehind {
                // Draw security pattern dots
                drawSecurityDots(baseColor)
                
                // Subtle glow effect
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.3f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.65f, size.height * 0.3f),
                        radius = 600.dp.toPx()
                    )
                )
            }
    )
}

private fun DrawScope.drawSecurityDots(baseColor: Color) {
    val dotColor = baseColor.copy(alpha = 0.05f)
    val dotSize = 1.dp.toPx()
    
    // Create a grid of security dots (using a simplified pattern)
    val gridSpacing = size.width / 12
    
    // Random but consistent pattern of dots
    val dotPositions = listOf(
        Offset(1f, 1f), Offset(3f, 1.5f), Offset(5f, 1f), Offset(7f, 2f), Offset(9f, 1.5f), 
        Offset(1.5f, 3f), Offset(3.5f, 4f), Offset(5.5f, 3f), Offset(7.5f, 3.5f), Offset(9.5f, 2.5f),
        Offset(2f, 5f), Offset(4f, 5.5f), Offset(6f, 4.5f), Offset(8f, 6f), Offset(10f, 5f),
        Offset(1f, 7f), Offset(3f, 7.5f), Offset(5f, 6.5f), Offset(7f, 8f), Offset(9f, 7f),
        Offset(1.5f, 9f), Offset(3.5f, 8.5f), Offset(5.5f, 9.5f), Offset(7.5f, 9f), Offset(9.5f, 10f)
    )
    
    dotPositions.forEach { pos ->
        drawCircle(
            color = dotColor,
            radius = dotSize,
            center = Offset(pos.x * gridSpacing, pos.y * gridSpacing)
        )
    }
}

/* App Logo */
@Preview
@Composable
fun SecretChatLogo(
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.logo_secret_chat),
        contentDescription = "Secret Chat Logo",
        modifier = modifier.size(120.dp)
    )
}