package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun TransferSpeedometer(
    totalSpeedBytesPerSec: Long,
    isTransferring: Boolean,
    activeCount: Int,
    completedCount: Int,
    modifier: Modifier = Modifier
) {
    val speedMbs = totalSpeedBytesPerSec / (1024.0 * 1024.0)

    val infiniteTransition = rememberInfiniteTransition(label = "speedometer_glow")
    val boltAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "boltAlpha"
    )

    AcrylicCard(
        shape = RoundedCornerShape(22.dp),
        backgroundColor = Color(0x351E293B),
        borderBrush = Brush.linearGradient(
            listOf(NeonCyan.copy(alpha = 0.7f), Color.White.copy(alpha = 0.25f), ElectricViolet.copy(alpha = 0.7f))
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("transfer_speedometer")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Speed indicator with lightning bolt
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(NeonCyan.copy(alpha = 0.25f), ElectricViolet.copy(alpha = 0.25f))
                            )
                        )
                        .border(1.5.dp, Brush.horizontalGradient(listOf(NeonCyan, ElectricViolet)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isTransferring) Icons.Default.Bolt else Icons.Default.Speed,
                        contentDescription = "Velocidad",
                        tint = if (isTransferring) NeonCyan.copy(alpha = boltAlpha) else TextSecondary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "VELOCIDAD AGREGADA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = if (isTransferring) String.format("%.1f", speedMbs) else "0.0",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isTransferring) NeonCyan else TextPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "MB/s",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SpeedEmerald,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                }
            }

            // Stats pill
            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x351E293B))
                        .border(1.dp, AcrylicSubtleBorderBrush, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isTransferring) "Transferencia Dual ⚡" else "Listo para enviar",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isTransferring) SpeedEmerald else TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "$completedCount completados",
                    fontSize = 11.sp,
                    color = TextTertiary
                )
            }
        }
    }
}
