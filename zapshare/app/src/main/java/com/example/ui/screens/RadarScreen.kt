package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.PeerDevice
import com.example.ui.components.RadarView
import com.example.ui.theme.*

@Composable
fun RadarScreen(
    isScanning: Boolean,
    isConnected: Boolean,
    isGroupOwner: Boolean,
    groupOwnerAddress: String?,
    localIpAddress: String?,
    connectedDeviceName: String,
    statusMessage: String,
    peers: List<PeerDevice>,
    onStartScan: () -> Unit,
    onStartReceiver: () -> Unit,
    onConnectPeer: (PeerDevice) -> Unit,
    onConnectManualIp: (String) -> Unit,
    onOpenQrDialog: () -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDirectIpInput by remember { mutableStateOf(false) }
    var targetIpInput by remember { mutableStateOf(groupOwnerAddress ?: "192.168.49.1") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
    ) {
        // Acrylic Hero Banner
        item {
            AcrylicCard(
                shape = RoundedCornerShape(22.dp),
                backgroundColor = Color(0x331E293B),
                borderBrush = AcrylicBorderBrush,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(136.dp)
                    .testTag("radar_hero_card")
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_transfer_hero),
                        contentDescription = "ZapShare Hero",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        DarkBackground.copy(alpha = 0.55f),
                                        DarkBackground.copy(alpha = 0.85f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AcrylicPill(
                                text = "WI-FI DIRECT REAL P2P",
                                tint = NeonCyan,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            AcrylicPill(
                                text = "DOBLE CANAL (2x2)",
                                tint = ChannelBColor
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Transferencia Ultra-Rápida",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Conexión directa local sin gastar datos ni usar internet.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Connection Status Glass Banner
        item {
            AcrylicCard(
                shape = RoundedCornerShape(18.dp),
                backgroundColor = if (isConnected) Color(0x2E10B981) else Color(0x281E293B),
                borderBrush = if (isConnected) Brush.linearGradient(listOf(SpeedEmerald, SpeedEmerald.copy(0.3f))) else AcrylicSubtleBorderBrush,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("connection_status_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(if (isConnected) SpeedEmerald else WarningAmber)
                                .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isConnected) "Conectado a $connectedDeviceName" else "Listo para conectar",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            val ipDisplay = groupOwnerAddress ?: localIpAddress ?: "Buscando IP..."
                            Text(
                                text = "$statusMessage • IP: $ipDisplay",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (isConnected) {
                        TextButton(
                            onClick = onDisconnect,
                            colors = ButtonDefaults.textButtonColors(contentColor = ErrorRose)
                        ) {
                            Text("Desconectar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        IconButton(onClick = onOpenQrDialog) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Ver código QR",
                                tint = NeonCyan
                            )
                        }
                    }
                }
            }
        }

        // Radar Scanner View
        item {
            RadarView(
                isScanning = isScanning,
                peers = peers,
                onPeerClick = onConnectPeer,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Real Main Action Buttons: Receive (Server on ports 8988 & 8989) & Send (Scan P2P)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Receive (Host / Server)
                Button(
                    onClick = onStartReceiver,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .testTag("create_group_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CallReceived,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Recibir",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }

                // Send (Discover Peers)
                Button(
                    onClick = onStartScan,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .testTag("search_peers_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.NearMe,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = DarkBackground
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isScanning) "Buscando..." else "Enviar / Buscar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = DarkBackground
                    )
                }
            }
        }

        // Direct IP / Local Wi-Fi Quick Connect Acrylic Card
        item {
            AcrylicCard(
                shape = RoundedCornerShape(16.dp),
                backgroundColor = Color(0x281E293B),
                borderBrush = AcrylicSubtleBorderBrush,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NeonCyan.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lan,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Conexión Directa por IP / LAN",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Tu IP local: ${localIpAddress ?: "Esperando red Wi-Fi"}",
                                    fontSize = 11.sp,
                                    color = NeonCyan
                                )
                            }
                        }

                        TextButton(onClick = { showDirectIpInput = !showDirectIpInput }) {
                            Text(
                                text = if (showDirectIpInput) "Ocultar" else "Conectar IP",
                                color = NeonCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    AnimatedVisibility(visible = showDirectIpInput) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            OutlinedTextField(
                                value = targetIpInput,
                                onValueChange = { targetIpInput = it },
                                label = { Text("IP del receptor (ej. 192.168.49.1)", fontSize = 12.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = DarkCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { onConnectManualIp(targetIpInput) },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Vincular vía IP", color = DarkBackground, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Discovered Devices Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DISPOSITIVOS WI-FI DIRECT (${peers.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
            }
        }

        // Real Discovered Peers List
        if (peers.isEmpty()) {
            item {
                AcrylicCard(
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = Color(0x221E293B),
                    borderBrush = AcrylicSubtleBorderBrush,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(26.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiFind,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isScanning) "Buscando dispositivos Wi-Fi Direct cercanos..." else "Ningún dispositivo detectado aún",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Asegúrate de que el otro dispositivo tenga ZapShare abierto y presione 'Recibir' o esté en la misma red.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(peers, key = { it.deviceAddress }) { peer ->
                AcrylicCard(
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = Color(0x351E293B),
                    borderBrush = AcrylicBorderBrush,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("peer_item_${peer.deviceAddress}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ElectricViolet.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhoneAndroid,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = peer.deviceName,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${peer.status} • Wi-Fi P2P",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Button(
                            onClick = { onConnectPeer(peer) },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("connect_peer_${peer.deviceAddress}")
                        ) {
                            Text("Conectar", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
