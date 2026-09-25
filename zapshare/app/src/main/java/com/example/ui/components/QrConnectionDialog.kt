package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Composable
fun QrConnectionDialog(
    ipAddress: String?,
    deviceName: String,
    onDismiss: () -> Unit,
    onConnectManualIp: (String) -> Unit
) {
    var manualIpText by remember { mutableStateOf(ipAddress ?: "192.168.49.1") }

    Dialog(onDismissRequest = onDismiss) {
        AcrylicCard(
            shape = RoundedCornerShape(26.dp),
            backgroundColor = Color(0xEE0F172A),
            borderBrush = Brush.linearGradient(
                listOf(NeonCyan.copy(alpha = 0.8f), ElectricViolet.copy(alpha = 0.8f))
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("qr_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Conectar vía QR / IP",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // High-tech stylized QR Matrix representation
                Box(
                    modifier = Modifier
                        .size(176.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(2.dp, NeonCyan.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cellSize = size.width / 15f
                        val darkColor = Color(0xFF0F172A)

                        // Draw corner finder patterns
                        fun drawFinder(x: Float, y: Float) {
                            drawRect(
                                color = darkColor,
                                topLeft = Offset(x, y),
                                size = Size(cellSize * 4, cellSize * 4)
                            )
                            drawRect(
                                color = Color.White,
                                topLeft = Offset(x + cellSize, y + cellSize),
                                size = Size(cellSize * 2, cellSize * 2)
                            )
                            drawRect(
                                color = darkColor,
                                topLeft = Offset(x + cellSize * 1.5f, y + cellSize * 1.5f),
                                size = Size(cellSize, cellSize)
                            )
                        }

                        drawFinder(0f, 0f)
                        drawFinder(size.width - cellSize * 4, 0f)
                        drawFinder(0f, size.height - cellSize * 4)

                        // Data matrix dots
                        val seed = (ipAddress?.hashCode() ?: 12345).toLong()
                        val random = java.util.Random(seed)
                        for (r in 0..14) {
                            for (c in 0..14) {
                                val inFinder = (r < 5 && c < 5) || (r < 5 && c > 9) || (r > 9 && c < 5)
                                if (!inFinder && random.nextBoolean()) {
                                    drawRect(
                                        color = darkColor,
                                        topLeft = Offset(c * cellSize, r * cellSize),
                                        size = Size(cellSize * 0.9f, cellSize * 0.9f)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Dispositivo: $deviceName",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = NeonCyan
                )

                Text(
                    text = "IP Wi-Fi: ${ipAddress ?: "192.168.49.1"}",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Row(modifier = Modifier.padding(top = 4.dp)) {
                    AcrylicPill(text = "CANAL A: 8988", tint = ChannelAColor)
                    Spacer(modifier = Modifier.width(6.dp))
                    AcrylicPill(text = "CANAL B: 8989", tint = ChannelBColor)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Manual IP input
                OutlinedTextField(
                    value = manualIpText,
                    onValueChange = { manualIpText = it },
                    label = { Text("IP de conexión del otro equipo", fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedLabelColor = NeonCyan,
                        unfocusedLabelColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        onConnectManualIp(manualIpText)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("connect_manual_ip_button")
                ) {
                    Text("Conectar Directamente", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
