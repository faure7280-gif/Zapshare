package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TransferItem
import com.example.ui.theme.*

@Composable
fun DualChannelCards(
    slot0Item: TransferItem?,
    slot1Item: TransferItem?,
    onCancelItem: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Section Header with Dual Channel Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(SpeedEmerald)
                        .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CANALES ACTIVOS (ENVIANDO DE 2 EN 2)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = NeonCyan
                )
            }

            Text(
                text = "Tubería Doble Simultánea",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }

        // Slot 0 (Channel A - Port 8988)
        AcrylicChannelSlotCard(
            slotLabel = "CANAL A • PUERTO 8988",
            badgeColor = ChannelAColor,
            borderBrush = AcrylicChannelABorder,
            item = slot0Item,
            onCancel = { slot0Item?.let { onCancelItem(it.id) } },
            testTag = "channel_slot_a"
        )

        // Slot 1 (Channel B - Port 8989)
        AcrylicChannelSlotCard(
            slotLabel = "CANAL B • PUERTO 8989",
            badgeColor = ChannelBColor,
            borderBrush = AcrylicChannelBBorder,
            item = slot1Item,
            onCancel = { slot1Item?.let { onCancelItem(it.id) } },
            testTag = "channel_slot_b"
        )
    }
}

@Composable
fun AcrylicChannelSlotCard(
    slotLabel: String,
    badgeColor: Color,
    borderBrush: Brush,
    item: TransferItem?,
    onCancel: () -> Unit,
    testTag: String
) {
    AcrylicCard(
        shape = RoundedCornerShape(18.dp),
        backgroundColor = if (item != null) Color(0x351E293B) else Color(0x1F1E293B),
        borderBrush = borderBrush,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Channel Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badge
                AcrylicPill(
                    text = slotLabel,
                    tint = badgeColor,
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(badgeColor)
                        )
                    }
                )

                // Speed indicator
                if (item != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SpeedEmerald.copy(alpha = 0.2f))
                            .border(1.dp, SpeedEmerald.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = item.speedFormatted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SpeedEmerald
                        )
                    }
                } else {
                    Text(
                        text = "En espera de archivo...",
                        fontSize = 11.sp,
                        color = TextTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (item != null) {
                // Active file row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceVariant)
                            .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getFileIcon(item.name, item.mimeType),
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${item.formattedTransferred} / ${item.formattedSize}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "${item.progressPercent}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("${testTag}_cancel")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancelar transferencia",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Bar with Acrylic Glow
                LinearProgressIndicator(
                    progress = { item.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = badgeColor,
                    trackColor = DarkSurfaceVariant
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Canal libre listo para procesar en paralelo",
                        fontSize = 12.sp,
                        color = TextTertiary
                    )
                }
            }
        }
    }
}

fun getFileIcon(name: String, mimeType: String): ImageVector {
    val lower = name.lowercase()
    return when {
        lower.endsWith(".apk") || mimeType.contains("android.package-archive") -> Icons.Default.Android
        lower.endsWith(".jpg") || lower.endsWith(".png") || lower.endsWith(".webp") || mimeType.startsWith("image/") -> Icons.Default.Image
        lower.endsWith(".mp4") || lower.endsWith(".mkv") || mimeType.startsWith("video/") -> Icons.Default.Movie
        lower.endsWith(".mp3") || lower.endsWith(".flac") || mimeType.startsWith("audio/") -> Icons.Default.MusicNote
        lower.endsWith(".zip") || lower.endsWith(".rar") || lower.endsWith(".tar") || lower.endsWith(".gz") -> Icons.Default.FolderZip
        lower.endsWith(".pdf") || lower.endsWith(".doc") || lower.endsWith(".docx") || lower.endsWith(".txt") -> Icons.Default.Description
        else -> Icons.Default.InsertDriveFile
    }
}
