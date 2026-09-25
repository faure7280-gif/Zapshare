package com.example.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Acrylic (Glassmorphism) design system for ZapShare.
 * Simulates high-end frosted acrylic glass with dynamic luminous refraction,
 * specular edge borders, and ambient background lighting.
 */

// Modern Acrylic Glass Gradient Borders
val AcrylicBorderBrush = Brush.linearGradient(
    colors = listOf(
        Color(0x66FFFFFF),
        Color(0x22FFFFFF),
        Color(0x08FFFFFF),
        Color(0x3300E5FF)
    ),
    start = Offset(0f, 0f),
    end = Offset(400f, 400f)
)

val AcrylicSubtleBorderBrush = Brush.linearGradient(
    colors = listOf(
        Color(0x35FFFFFF),
        Color(0x10FFFFFF),
        Color(0x05FFFFFF),
        Color(0x18FFFFFF)
    )
)

val AcrylicChannelABorder = Brush.linearGradient(
    colors = listOf(
        ChannelAColor.copy(alpha = 0.7f),
        Color.White.copy(alpha = 0.3f),
        ChannelAColor.copy(alpha = 0.2f)
    )
)

val AcrylicChannelBBorder = Brush.linearGradient(
    colors = listOf(
        ChannelBColor.copy(alpha = 0.7f),
        Color.White.copy(alpha = 0.3f),
        ChannelBColor.copy(alpha = 0.2f)
    )
)

/**
 * Atmospheric background that renders ambient glow lights behind frosted acrylic layers.
 * This brings out the translucent glass refraction depth.
 */
@Composable
fun AcrylicBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    // Subtle pulsating glow animation for the ambient lights
    val infiniteTransition = rememberInfiniteTransition(label = "acrylic_ambient")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.32f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .drawBehind {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Ambient Orb 1: Cyan glow at top-right
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NeonCyan.copy(alpha = pulseAlpha),
                            Color(0x0000E5FF)
                        ),
                        center = Offset(canvasWidth * 0.85f, canvasHeight * 0.12f),
                        radius = canvasWidth * 0.75f
                    ),
                    center = Offset(canvasWidth * 0.85f, canvasHeight * 0.12f),
                    radius = canvasWidth * 0.75f
                )

                // Ambient Orb 2: Electric Violet glow at center-left
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ElectricViolet.copy(alpha = pulseAlpha * 0.85f),
                            Color(0x007C3AED)
                        ),
                        center = Offset(canvasWidth * 0.12f, canvasHeight * 0.55f),
                        radius = canvasWidth * 0.85f
                    ),
                    center = Offset(canvasWidth * 0.12f, canvasHeight * 0.55f),
                    radius = canvasWidth * 0.85f
                )

                // Ambient Orb 3: Speed Emerald glow at bottom-center
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SpeedEmerald.copy(alpha = pulseAlpha * 0.45f),
                            Color(0x0010B981)
                        ),
                        center = Offset(canvasWidth * 0.5f, canvasHeight * 0.95f),
                        radius = canvasWidth * 0.6f
                    ),
                    center = Offset(canvasWidth * 0.5f, canvasHeight * 0.95f),
                    radius = canvasWidth * 0.6f
                )
            },
        content = content
    )
}

/**
 * Acrylic Frosted Glass Card with specular refraction borders and frosted tint.
 */
@Composable
fun AcrylicCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = Color(0x3D1E293B),
    borderBrush: Brush = AcrylicBorderBrush,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(color = NeonCyan),
            onClick = onClick
        )
    } else Modifier

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(borderWidth, borderBrush, shape)
            .drawBehind {
                // Acrylic top specular highlight reflection line
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.28f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(24f, 0f),
                    end = Offset(size.width - 24f, 0f),
                    strokeWidth = 2f
                )
            }
            .then(clickModifier),
        content = content
    )
}

/**
 * Acrylic Pill Badge for status, channels, and modern tags.
 */
@Composable
fun AcrylicPill(
    text: String,
    modifier: Modifier = Modifier,
    tint: Color = NeonCyan,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(tint.copy(alpha = 0.16f))
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(tint.copy(alpha = 0.6f), tint.copy(alpha = 0.15f))
                ),
                RoundedCornerShape(32.dp)
            )
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            leadingIcon?.invoke()
            androidx.compose.material3.Text(
                text = text,
                color = Color.White,
                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
            )
        }
    }
}

/**
 * Acrylic Circular Icon Button with glass shimmer feedback.
 */
@Composable
fun AcrylicIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    tint: Color = Color.White,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0x331E293B))
            .border(1.dp, AcrylicBorderBrush, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = tint),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
