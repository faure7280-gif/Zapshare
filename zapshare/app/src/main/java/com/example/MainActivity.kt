package com.example

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.TransferStatus
import com.example.ui.NavTab
import com.example.ui.ZapShareViewModel
import com.example.ui.ZapShareViewModelFactory
import com.example.ui.components.QrConnectionDialog
import com.example.ui.screens.FileSelectorScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.RadarScreen
import com.example.ui.screens.TransferScreen
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {

    private val viewModel: ZapShareViewModel by viewModels {
        val app = application as ZapShareApplication
        ZapShareViewModelFactory(
            app.wifiDirectManager,
            app.transferEngine,
            app.filePickerManager,
            app.repository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle external incoming share intent (e.g. from gallery or file manager)
        handleShareIntent(intent)

        setContent {
            ZapShareTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShareIntent(intent)
    }

    private fun handleShareIntent(intent: Intent?) {
        if (intent == null) return
        when (intent.action) {
            Intent.ACTION_SEND -> {
                (intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM))?.let { uri ->
                    viewModel.addPickedFiles(listOf(uri))
                    viewModel.setTab(NavTab.FILES)
                }
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)?.let { uris ->
                    viewModel.addPickedFiles(uris)
                    viewModel.setTab(NavTab.FILES)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: ZapShareViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val isP2pEnabled by viewModel.isP2pEnabled.collectAsStateWithLifecycle()
    val isDiscovering by viewModel.isDiscovering.collectAsStateWithLifecycle()
    val peers by viewModel.peersList.collectAsStateWithLifecycle()
    val isConnected by viewModel.isConnected.collectAsStateWithLifecycle()
    val isGroupOwner by viewModel.isGroupOwner.collectAsStateWithLifecycle()
    val groupOwnerAddress by viewModel.groupOwnerAddress.collectAsStateWithLifecycle()
    val localIpAddress by viewModel.localIpAddress.collectAsStateWithLifecycle()
    val connectedDeviceName by viewModel.connectedDeviceName.collectAsStateWithLifecycle()
    val localDeviceName by viewModel.localDeviceName.collectAsStateWithLifecycle()
    val statusMessage by viewModel.p2pStatusMessage.collectAsStateWithLifecycle()

    val activeTransfers by viewModel.activeTransfers.collectAsStateWithLifecycle()
    val slot0Active by viewModel.slot0Active.collectAsStateWithLifecycle()
    val slot1Active by viewModel.slot1Active.collectAsStateWithLifecycle()
    val totalSpeed by viewModel.totalSpeedBytesPerSec.collectAsStateWithLifecycle()
    val isTransferring by viewModel.isTransferring.collectAsStateWithLifecycle()

    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val availableFiles by viewModel.availableFiles.collectAsStateWithLifecycle()
    val isLoadingFiles by viewModel.isLoadingFiles.collectAsStateWithLifecycle()
    val selectedFiles by viewModel.selectedFiles.collectAsStateWithLifecycle()

    val historyList by viewModel.transferHistory.collectAsStateWithLifecycle()
    val showQrDialog by viewModel.showQrDialog.collectAsStateWithLifecycle()

    // Runtime permissions for Wi-Fi Direct
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Permissions handled
    }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        } else {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        permissionsLauncher.launch(permissions.toTypedArray())
    }

    AcrylicBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NeonCyan),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = DarkBackground,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "ZapShare",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    AcrylicPill(
                                        text = "DUAL 2x2",
                                        tint = NeonCyan
                                    )
                                }
                                Text(
                                    text = if (isConnected) "Conectado a $connectedDeviceName" else "P2P Wi-Fi Direct Ultra-Rápido",
                                    fontSize = 11.sp,
                                    color = if (isConnected) SpeedEmerald else TextSecondary
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.setShowQrDialog(true) },
                            modifier = Modifier.testTag("top_bar_qr_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Conectar vía QR",
                                tint = NeonCyan
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0x350F172A),
                        titleContentColor = TextPrimary
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xD90F172A),
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .border(1.dp, AcrylicSubtleBorderBrush, RoundedCornerShape(0.dp))
                        .testTag("main_bottom_nav")
                ) {
                    val activeCount = activeTransfers.count { it.status == TransferStatus.TRANSFERRING || it.status == TransferStatus.QUEUED }

                    BottomNavItem(
                        selected = currentTab == NavTab.RADAR,
                        onClick = { viewModel.setTab(NavTab.RADAR) },
                        icon = Icons.Default.WifiTethering,
                        label = "Radar",
                        testTag = "nav_radar"
                    )

                    BottomNavItem(
                        selected = currentTab == NavTab.FILES,
                        onClick = { viewModel.setTab(NavTab.FILES) },
                        icon = Icons.Default.FolderOpen,
                        label = "Archivos",
                        badgeCount = selectedFiles.size,
                        testTag = "nav_files"
                    )

                    BottomNavItem(
                        selected = currentTab == NavTab.TRANSFER,
                        onClick = { viewModel.setTab(NavTab.TRANSFER) },
                        icon = Icons.Default.Bolt,
                        label = "Transferir",
                        badgeCount = activeCount,
                        testTag = "nav_transfer"
                    )

                    BottomNavItem(
                        selected = currentTab == NavTab.HISTORY,
                        onClick = { viewModel.setTab(NavTab.HISTORY) },
                        icon = Icons.Default.History,
                        label = "Historial",
                        testTag = "nav_history"
                    )
                }
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    NavTab.RADAR -> {
                        RadarScreen(
                            isScanning = isDiscovering,
                            isConnected = isConnected,
                            isGroupOwner = isGroupOwner,
                            groupOwnerAddress = groupOwnerAddress,
                            localIpAddress = localIpAddress,
                            connectedDeviceName = connectedDeviceName,
                            statusMessage = statusMessage,
                            peers = peers,
                            onStartScan = { viewModel.startDiscovery() },
                            onStartReceiver = { viewModel.startReceiverMode() },
                            onConnectPeer = { viewModel.connectToPeer(it) },
                            onConnectManualIp = { viewModel.connectDirectIp(it) },
                            onOpenQrDialog = { viewModel.setShowQrDialog(true) },
                            onDisconnect = { viewModel.disconnect() }
                        )
                    }

                    NavTab.FILES -> {
                        FileSelectorScreen(
                            selectedCategory = selectedCategory,
                            availableFiles = availableFiles,
                            isLoadingFiles = isLoadingFiles,
                            selectedFiles = selectedFiles,
                            onCategorySelected = { viewModel.selectCategory(it) },
                            onToggleSelectFile = { viewModel.toggleFileSelection(it) },
                            onFilesPicked = { viewModel.addPickedFiles(it) },
                            onSendFiles = { viewModel.sendSelectedFiles() }
                        )
                    }

                    NavTab.TRANSFER -> {
                        TransferScreen(
                            activeTransfers = activeTransfers,
                            slot0Item = slot0Active,
                            slot1Item = slot1Active,
                            totalSpeedBytesPerSec = totalSpeed,
                            isTransferring = isTransferring,
                            onCancelItem = { viewModel.cancelTransfer(it) },
                            onClearCompleted = { viewModel.clearCompletedTransfers() },
                            onSelectFiles = { viewModel.setTab(NavTab.FILES) }
                        )
                    }

                    NavTab.HISTORY -> {
                        HistoryScreen(
                            historyList = historyList,
                            onClearHistory = { viewModel.clearHistory() },
                            onDeleteItem = { viewModel.deleteHistoryItem(it) }
                        )
                    }
                }

                // QR Code Connection Dialog
                if (showQrDialog) {
                    QrConnectionDialog(
                        ipAddress = groupOwnerAddress ?: localIpAddress,
                        deviceName = localDeviceName,
                        onDismiss = { viewModel.setShowQrDialog(false) },
                        onConnectManualIp = { ip ->
                            viewModel.connectDirectIp(ip)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RowScope.BottomNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    badgeCount: Int = 0,
    testTag: String
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            BadgedBox(
                badge = {
                    if (badgeCount > 0) {
                        Badge(
                            containerColor = NeonCyan,
                            contentColor = DarkBackground
                        ) {
                            Text(
                                text = if (badgeCount > 99) "99+" else "$badgeCount",
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (selected) NeonCyan else TextSecondary
                )
            }
        },
        label = {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) NeonCyan else TextSecondary
            )
        },
        colors = NavigationBarItemDefaults.colors(
            indicatorColor = NeonCyan.copy(alpha = 0.15f)
        ),
        modifier = Modifier.testTag(testTag)
    )
}

// Backward-compatible Greeting composable for tests
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
