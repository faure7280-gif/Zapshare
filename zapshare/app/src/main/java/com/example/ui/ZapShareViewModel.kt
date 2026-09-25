package com.example.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.TransferEntity
import com.example.data.local.TransferRepository
import com.example.engine.DualChannelTransferEngine
import com.example.model.*
import com.example.p2p.WifiDirectManager
import com.example.util.FilePickerManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class NavTab {
    RADAR,
    FILES,
    TRANSFER,
    HISTORY
}

class ZapShareViewModel(
    private val wifiDirectManager: WifiDirectManager,
    private val transferEngine: DualChannelTransferEngine,
    private val filePickerManager: FilePickerManager,
    private val repository: TransferRepository
) : ViewModel() {

    // Current navigation tab
    private val _currentTab = MutableStateFlow(NavTab.RADAR)
    val currentTab: StateFlow<NavTab> = _currentTab.asStateFlow()

    // Wi-Fi Direct state
    val isP2pEnabled = wifiDirectManager.isP2pEnabled
    val isDiscovering = wifiDirectManager.isDiscovering
    val peersList = wifiDirectManager.peersList
    val isConnected = wifiDirectManager.isConnected
    val isGroupOwner = wifiDirectManager.isGroupOwner
    val groupOwnerAddress = wifiDirectManager.groupOwnerAddress
    val connectedDeviceName = wifiDirectManager.connectedDeviceName
    val localDeviceName = wifiDirectManager.localDeviceName
    val p2pStatusMessage = wifiDirectManager.statusMessage

    // Transfer Engine state (Dual Pipeline)
    val activeTransfers = transferEngine.activeTransfers
    val slot0Active = transferEngine.slot0Active
    val slot1Active = transferEngine.slot1Active
    val totalSpeedBytesPerSec = transferEngine.totalSpeedBytesPerSec
    val isTransferring = transferEngine.isTransferring

    // History from Room
    val transferHistory: StateFlow<List<TransferEntity>> = repository.allTransfers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // File Browser state
    private val _selectedCategory = MutableStateFlow(FileCategory.APPS)
    val selectedCategory: StateFlow<FileCategory> = _selectedCategory.asStateFlow()

    private val _availableFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val availableFiles: StateFlow<List<FileItem>> = _availableFiles.asStateFlow()

    private val _isLoadingFiles = MutableStateFlow(false)
    val isLoadingFiles: StateFlow<Boolean> = _isLoadingFiles.asStateFlow()

    private val _selectedFiles = MutableStateFlow<Map<Uri, FileItem>>(emptyMap())
    val selectedFiles: StateFlow<Map<Uri, FileItem>> = _selectedFiles.asStateFlow()

    // Show QR code dialog
    private val _showQrDialog = MutableStateFlow(false)
    val showQrDialog: StateFlow<Boolean> = _showQrDialog.asStateFlow()

    val localIpAddress = wifiDirectManager.localIpAddress

    init {
        loadFilesForCategory(FileCategory.APPS)
        wifiDirectManager.refreshLocalIp()
    }

    fun setTab(tab: NavTab) {
        _currentTab.value = tab
    }

    fun setShowQrDialog(show: Boolean) {
        _showQrDialog.value = show
    }

    fun refreshNetworkState() {
        wifiDirectManager.refreshLocalIp()
    }

    fun connectDirectIp(ip: String, name: String = "Receptor Wi-Fi") {
        wifiDirectManager.setDirectIpConnection(ip, name)
    }

    fun startReceiverMode() {
        // Starts the dual-socket server listening on port 8988 & 8989
        transferEngine.startReceiverServer()
        wifiDirectManager.refreshLocalIp()
        createGroup()
        _showQrDialog.value = true
    }

    fun selectCategory(category: FileCategory) {
        _selectedCategory.value = category
        loadFilesForCategory(category)
    }

    fun loadFilesForCategory(category: FileCategory) {
        viewModelScope.launch {
            _isLoadingFiles.value = true
            val files = when (category) {
                FileCategory.APPS -> filePickerManager.getInstalledApps()
                FileCategory.PHOTOS -> filePickerManager.getMediaImages()
                FileCategory.VIDEOS -> filePickerManager.getMediaVideos()
                FileCategory.MUSIC -> filePickerManager.getMediaAudio()
                FileCategory.DOCUMENTS, FileCategory.ARCHIVES, FileCategory.ALL -> filePickerManager.getDocuments()
            }
            _availableFiles.value = files
            _isLoadingFiles.value = false
        }
    }

    fun toggleFileSelection(item: FileItem) {
        _selectedFiles.update { current ->
            val mutable = current.toMutableMap()
            if (mutable.containsKey(item.uri)) {
                mutable.remove(item.uri)
            } else {
                mutable[item.uri] = item
            }
            mutable
        }
    }

    fun addPickedFiles(uris: List<Uri>) {
        viewModelScope.launch {
            uris.forEach { uri ->
                val item = filePickerManager.parsePickedFileUri(uri)
                _selectedFiles.update { it + (uri to item) }
            }
        }
    }

    fun clearSelection() {
        _selectedFiles.value = emptyMap()
    }

    /**
     * Start Dual-Stream File Transfer (Enviando de 2 en 2)
     */
    fun sendSelectedFiles() {
        val filesToSend = _selectedFiles.value.values.toList()
        if (filesToSend.isEmpty()) return

        val transferItems = filesToSend.map { file ->
            TransferItem(
                name = file.name,
                size = file.size,
                mimeType = file.mimeType,
                uriString = file.uri.toString(),
                direction = TransferDirection.SENDING,
                peerDeviceName = connectedDeviceName.value
            )
        }

        val targetIp = groupOwnerAddress.value ?: "192.168.49.1"
        transferEngine.enqueueFilesToSend(
            items = transferItems,
            targetIp = targetIp,
            peerDeviceName = connectedDeviceName.value
        )

        clearSelection()
        _currentTab.value = NavTab.TRANSFER
    }

    fun startDiscovery() {
        wifiDirectManager.startDiscovery()
    }

    fun createGroup() {
        wifiDirectManager.createGroup { success ->
            if (success) {
                transferEngine.startReceiverServer()
            }
        }
    }

    fun connectToPeer(peer: PeerDevice) {
        wifiDirectManager.connectToPeer(peer)
    }

    fun disconnect() {
        wifiDirectManager.disconnect()
        transferEngine.stopReceiverServer()
    }

    fun cancelTransfer(id: String) {
        transferEngine.cancelTransfer(id)
    }

    fun clearCompletedTransfers() {
        transferEngine.clearCompleted()
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteTransfer(id)
        }
    }
}

class ZapShareViewModelFactory(
    private val wifiDirectManager: WifiDirectManager,
    private val transferEngine: DualChannelTransferEngine,
    private val filePickerManager: FilePickerManager,
    private val repository: TransferRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ZapShareViewModel::class.java)) {
            return ZapShareViewModel(
                wifiDirectManager,
                transferEngine,
                filePickerManager,
                repository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
