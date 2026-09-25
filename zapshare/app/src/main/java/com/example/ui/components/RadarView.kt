package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PeerDevice
import com.example.ui.theme.*

@Composable
fun RadarView(
    isScanning: Boolean,
    peers: List<PeerDevice>,
    onPeerClick: (PeerDevice) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar_waves")

    val wave1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave1"
    )

    val wave2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, delayMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave2"
    )

    val wave3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, delayMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave3"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        contentAlignment = Alignment.Center
    ) {
        // Canvas drawing expanding radar ripples
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = Math.min(size.width, size.height) * 0.45f

            // Static background rings
            for (i in 1..4) {
                drawCircle(
                    color = DarkCardBorder.copy(alpha = 0.35f),
                    radius = maxRadius * (i / 4f),
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // Expanding animated pulse waves
            if (isScanning) {
                val waves = listOf(wave1, wave2, wave3)
                for (progress in waves) {
                    val currentRadius = maxRadius * progress
                    val alpha = (1f - progress).coerceIn(0f, 1f) * 0.7f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(NeonCyan.copy(alpha = alpha), Color.Transparent),
                            center = center,
                            radius = currentRadius + 10f
                        ),
                        radius = currentRadius,
                        center = center
                    )
                    drawCircle(
                        color = NeonCyan.copy(alpha = alpha),
                        radius = currentRadius,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }

        // Center device avatar (Self)
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(ElectricViolet, DarkSurface)
                    )
                )
                .testTag("radar_center_device"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = "Zap Central",
                tint = NeonCyan,
                modifier = Modifier.size(42.dp)
            )
        }

        // Surrounding peer devices positioned dynamically
        if (peers.isNotEmpty()) {
            val angles = listOf(-40.0, 45.0, 190.0, 130.0)
            peers.take(4).forEachIndexed { index, peer ->
                val angleRad = Math.toRadians(angles[index % angles.size])
                val distance = 95.dp

                // Small peer node
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkSurface.copy(alpha = 0.92f)
                    ),
                    modifier = Modifier
                        .offset(
                            x = (distance.value * Math.cos(angleRad)).dp,
                            y = (distance.value * Math.sin(angleRad)).dp
                        )
                        .clickable { onPeerClick(peer) }
                        .testTag("peer_node_${peer.deviceAddress}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = peer.deviceName,
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = peer.deviceName.take(12),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}
