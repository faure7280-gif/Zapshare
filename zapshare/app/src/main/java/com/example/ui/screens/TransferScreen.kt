package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TransferDirection
import com.example.model.TransferItem
import com.example.model.TransferStatus
import com.example.ui.components.DualChannelCards
import com.example.ui.components.TransferSpeedometer
import com.example.ui.components.getFileIcon
import com.example.ui.theme.*

@Composable
fun TransferScreen(
    activeTransfers: List<TransferItem>,
    slot0Item: TransferItem?,
    slot1Item: TransferItem?,
    totalSpeedBytesPerSec: Long,
    isTransferring: Boolean,
    onCancelItem: (String) -> Unit,
    onClearCompleted: () -> Unit,
    onSelectFiles: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val completedTransfers = activeTransfers.filter { it.status == TransferStatus.COMPLETED }
    val queuedTransfers = activeTransfers.filter { it.status == TransferStatus.QUEUED }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
    ) {
        // Speedometer Header
        item {
            TransferSpeedometer(
                totalSpeedBytesPerSec = totalSpeedBytesPerSec,
                isTransferring = isTransferring,
                activeCount = if (slot0Item != null || slot1Item != null) (if (slot0Item != null) 1 else 0) + (if (slot1Item != null) 1 else 0) else 0,
                completedCount = completedTransfers.size
            )
        }

        // Dual Channel Cards (Enviando de dos en dos en paralelo)
        item {
            DualChannelCards(
                slot0Item = slot0Item,
                slot1Item = slot1Item,
                onCancelItem = onCancelItem
            )
        }

        // Queued Items Section
        if (queuedTransfers.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EN COLA DE ESPERA (${queuedTransfers.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• Se activan automáticamente de 2 en 2",
                        fontSize = 11.sp,
                        color = NeonCyan
                    )
                }
            }

            items(queuedTransfers, key = { it.id }) { item ->
                AcrylicCard(
                    shape = RoundedCornerShape(14.dp),
                    backgroundColor = Color(0x281E293B),
                    borderBrush = AcrylicSubtleBorderBrush,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("queued_item_${item.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DarkBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getFileIcon(item.name, item.mimeType),
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = item.name,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = item.formattedSize,
                                    fontSize = 11.sp,
                                    color = TextTertiary
                                )
                            }
                        }

                        IconButton(
                            onClick = { onCancelItem(item.id) },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancelar",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Completed Items Section
        if (completedTransfers.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "COMPLETADOS RECIENTEMENTE (${completedTransfers.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )

                    TextButton(
                        onClick = onClearCompleted,
                        colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
                    ) {
                        Text("Limpiar", fontSize = 11.sp)
                    }
                }
            }

            items(completedTransfers, key = { it.id }) { item ->
                AcrylicCard(
                    shape = RoundedCornerShape(14.dp),
                    backgroundColor = Color(0x301E293B),
                    borderBrush = AcrylicSubtleBorderBrush,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("completed_item_${item.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(SpeedEmerald.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = SpeedEmerald,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = item.name,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${item.formattedSize} • ${if (item.direction == TransferDirection.SENDING) "Enviado a " else "Recibido de "} ${item.peerDeviceName}",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Open or share button
                        IconButton(
                            onClick = {
                                openOrShareFile(context, item)
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Compartir",
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Empty state when nothing in progress
        if (!isTransferring && activeTransfers.isEmpty()) {
            item {
                AcrylicCard(
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = Color(0x281E293B),
                    borderBrush = AcrylicBorderBrush,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(NeonCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Sin transferencias activas",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Selecciona archivos para enviarlos en paralelo de 2 en 2 a máxima velocidad Wi-Fi Direct.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = onSelectFiles,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = DarkBackground,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Seleccionar Archivos", color = DarkBackground, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private fun openOrShareFile(context: Context, item: TransferItem) {
    try {
        val uri = if (item.uriString != null) {
            Uri.parse(item.uriString)
        } else if (item.filePath != null) {
            Uri.parse("file://${item.filePath}")
        } else null

        if (uri != null) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = item.mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Compartir ${item.name}"))
        }
    } catch (_: Exception) {}
}
