package com.example.engine

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.example.data.local.TransferEntity
import com.example.data.local.TransferRepository
import com.example.model.TransferDirection
import com.example.model.TransferItem
import com.example.model.TransferStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.*
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

class DualChannelTransferEngine(
    private val context: Context,
    private val repository: TransferRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Active transfers list shown in UI
    private val _activeTransfers = MutableStateFlow<List<TransferItem>>(emptyList())
    val activeTransfers: StateFlow<List<TransferItem>> = _activeTransfers.asStateFlow()

    // Dual slot status (Slot 0: Channel A, Slot 1: Channel B)
    private val _slot0Active = MutableStateFlow<TransferItem?>(null)
    val slot0Active: StateFlow<TransferItem?> = _slot0Active.asStateFlow()

    private val _slot1Active = MutableStateFlow<TransferItem?>(null)
    val slot1Active: StateFlow<TransferItem?> = _slot1Active.asStateFlow()

    // Aggregate real-time speed in bytes/sec
    private val _totalSpeedBytesPerSec = MutableStateFlow(0L)
    val totalSpeedBytesPerSec: StateFlow<Long> = _totalSpeedBytesPerSec.asStateFlow()

    // Whether transmission engine is actively transferring
    private val _isTransferring = MutableStateFlow(false)
    val isTransferring: StateFlow<Boolean> = _isTransferring.asStateFlow()

    // Receiver ServerSockets
    private var serverSocketA: ServerSocket? = null
    private var serverSocketB: ServerSocket? = null
    private var isServerRunning = false

    // Concurrent queue of items pending transmission
    private val pendingSendQueue = ConcurrentLinkedQueue<TransferItem>()
    private val activeItemMap = ConcurrentHashMap<String, TransferItem>()

    // Cancellation signals
    private val cancelledItemIds = ConcurrentHashMap.newKeySet<String>()

    init {
        // Speed calculation ticker
        scope.launch {
            while (isActive) {
                delay(500)
                val s0 = _slot0Active.value?.speedBytesPerSec ?: 0L
                val s1 = _slot1Active.value?.speedBytesPerSec ?: 0L
                _totalSpeedBytesPerSec.value = s0 + s1
            }
        }
    }

    /**
     * Start the Receiver Server listening on both Channel A (8988) and Channel B (8989)
     */
    fun startReceiverServer() {
        if (isServerRunning) return
        isServerRunning = true

        scope.launch {
            try {
                serverSocketA = ServerSocket().apply {
                    reuseAddress = true
                    bind(InetSocketAddress(TransferProtocol.DATA_PORT_CHANNEL_A))
                }
                Log.d("ZapEngine", "Server Channel A listening on ${TransferProtocol.DATA_PORT_CHANNEL_A}")
                while (isServerRunning && serverSocketA?.isClosed == false) {
                    val socket = serverSocketA!!.accept()
                    launch { handleIncomingStream(socket, channelSlot = 0) }
                }
            } catch (e: Exception) {
                Log.e("ZapEngine", "Error on ServerSocket A: ${e.message}")
            }
        }

        scope.launch {
            try {
                serverSocketB = ServerSocket().apply {
                    reuseAddress = true
                    bind(InetSocketAddress(TransferProtocol.DATA_PORT_CHANNEL_B))
                }
                Log.d("ZapEngine", "Server Channel B listening on ${TransferProtocol.DATA_PORT_CHANNEL_B}")
                while (isServerRunning && serverSocketB?.isClosed == false) {
                    val socket = serverSocketB!!.accept()
                    launch { handleIncomingStream(socket, channelSlot = 1) }
                }
            } catch (e: Exception) {
                Log.e("ZapEngine", "Error on ServerSocket B: ${e.message}")
            }
        }
    }

    fun stopReceiverServer() {
        isServerRunning = false
        try {
            serverSocketA?.close()
            serverSocketB?.close()
        } catch (_: Exception) {}
        serverSocketA = null
        serverSocketB = null
    }

    /**
     * Handle incoming file stream on either Channel A (slot 0) or Channel B (slot 1)
     */
    private suspend fun handleIncomingStream(socket: Socket, channelSlot: Int) = withContext(Dispatchers.IO) {
        var transferItem: TransferItem? = null
        var fileOutputStream: FileOutputStream? = null
        var targetFile: File? = null
        val startTime = System.currentTimeMillis()

        try {
            socket.tcpNoDelay = true
            socket.receiveBufferSize = TransferProtocol.BUFFER_SIZE * 4

            val dis = DataInputStream(BufferedInputStream(socket.getInputStream(), TransferProtocol.BUFFER_SIZE))
            val magic = dis.readInt()
            if (magic != TransferProtocol.MAGIC_HEADER) {
                Log.e("ZapEngine", "Invalid magic header: $magic")
                socket.close()
                return@withContext
            }

            val fileName = dis.readUTF()
            val fileSize = dis.readLong()
            val mimeType = dis.readUTF()
            val peerName = dis.readUTF()

            val downloadDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "ZapShare"
            ).apply { mkdirs() }

            targetFile = File(downloadDir, fileName)
            // Ensure unique name if already exists
            if (targetFile.exists()) {
                val dotIndex = fileName.lastIndexOf('.')
                val nameWithoutExt = if (dotIndex > 0) fileName.substring(0, dotIndex) else fileName
                val ext = if (dotIndex > 0) fileName.substring(dotIndex) else ""
                targetFile = File(downloadDir, "${nameWithoutExt}_${System.currentTimeMillis()}$ext")
            }

            fileOutputStream = FileOutputStream(targetFile)

            val item = TransferItem(
                name = fileName,
                size = fileSize,
                mimeType = mimeType,
                filePath = targetFile.absolutePath,
                direction = TransferDirection.RECEIVING,
                status = TransferStatus.TRANSFERRING,
                channelSlot = channelSlot,
                peerDeviceName = peerName
            )
            transferItem = item
            activeItemMap[item.id] = item
            setSlotItem(channelSlot, item)
            updateActiveTransfersList()

            val buffer = ByteArray(TransferProtocol.BUFFER_SIZE)
            var totalRead = 0L
            var lastUpdate = System.currentTimeMillis()
            var bytesSinceLastUpdate = 0L

            while (totalRead < fileSize) {
                val toRead = Math.min(buffer.size.toLong(), fileSize - totalRead).toInt()
                val read = dis.read(buffer, 0, toRead)
                if (read == -1) break

                fileOutputStream.write(buffer, 0, read)
                totalRead += read
                bytesSinceLastUpdate += read

                val now = System.currentTimeMillis()
                val elapsed = now - lastUpdate
                if (elapsed >= 100 || totalRead == fileSize) {
                    val speed = if (elapsed > 0) (bytesSinceLastUpdate * 1000) / elapsed else 0L
                    val progress = if (fileSize > 0) (totalRead.toFloat() / fileSize) else 1f
                    val updated = item.copy(
                        transferredBytes = totalRead,
                        progress = progress,
                        speedBytesPerSec = speed
                    )
                    activeItemMap[item.id] = updated
                    setSlotItem(channelSlot, updated)
                    updateActiveTransfersList()

                    lastUpdate = now
                    bytesSinceLastUpdate = 0L
                }
            }

            fileOutputStream.flush()

            val finalItem = item.copy(
                transferredBytes = fileSize,
                progress = 1f,
                speedBytesPerSec = 0L,
                status = TransferStatus.COMPLETED
            )
            activeItemMap[item.id] = finalItem
            setSlotItem(channelSlot, null)
            updateActiveTransfersList()

            // Register in MediaStore
            MediaScannerConnection.scanFile(
                context,
                arrayOf(targetFile.absolutePath),
                arrayOf(mimeType),
                null
            )

            // Record in Room Database
            val duration = (System.currentTimeMillis() - startTime).coerceAtLeast(1)
            val avgSpeed = (fileSize * 1000) / duration
            repository.recordTransfer(
                TransferEntity(
                    fileName = fileName,
                    fileSize = fileSize,
                    mimeType = mimeType,
                    direction = "RECEIVE",
                    peerName = peerName,
                    status = "COMPLETED",
                    durationMs = duration,
                    avgSpeedBytesPerSec = avgSpeed,
                    filePathOrUri = targetFile.absolutePath
                )
            )
        } catch (e: Exception) {
            Log.e("ZapEngine", "Error receiving file: ${e.message}", e)
            transferItem?.let {
                val failedItem = it.copy(
                    status = TransferStatus.FAILED,
                    errorMessage = e.localizedMessage,
                    speedBytesPerSec = 0L
                )
                activeItemMap[it.id] = failedItem
                setSlotItem(channelSlot, null)
                updateActiveTransfersList()
            }
        } finally {
            try { fileOutputStream?.close() } catch (_: Exception) {}
            try { socket.close() } catch (_: Exception) {}
        }
    }

    /**
     * Enqueue files and initiate dual-worker parallel pipeline (de dos en dos)
     */
    fun enqueueFilesToSend(
        items: List<TransferItem>,
        targetIp: String,
        peerDeviceName: String
    ) {
        for (item in items) {
            val queued = item.copy(
                status = TransferStatus.QUEUED,
                direction = TransferDirection.SENDING,
                peerDeviceName = peerDeviceName
            )
            pendingSendQueue.offer(queued)
            activeItemMap[queued.id] = queued
        }
        updateActiveTransfersList()

        startDualWorkerPipeline(targetIp, peerDeviceName)
    }

    private fun startDualWorkerPipeline(targetIp: String, peerDeviceName: String) {
        if (_isTransferring.value) return
        _isTransferring.value = true

        // Worker Slot 0 (Channel A, Port 8988)
        scope.launch {
            runWorkerSlot(slotIndex = 0, targetPort = TransferProtocol.DATA_PORT_CHANNEL_A, targetIp = targetIp, peerDeviceName = peerDeviceName)
        }

        // Worker Slot 1 (Channel B, Port 8989) - Running simultaneously de 2 en 2!
        scope.launch {
            runWorkerSlot(slotIndex = 1, targetPort = TransferProtocol.DATA_PORT_CHANNEL_B, targetIp = targetIp, peerDeviceName = peerDeviceName)
        }
    }

    private suspend fun runWorkerSlot(
        slotIndex: Int,
        targetPort: Int,
        targetIp: String,
        peerDeviceName: String
    ) = coroutineScope {
        while (isActive) {
            val nextItem = pendingSendQueue.poll() ?: break

            // Check if cancelled
            if (cancelledItemIds.contains(nextItem.id)) {
                val cancelled = nextItem.copy(status = TransferStatus.CANCELLED)
                activeItemMap[nextItem.id] = cancelled
                updateActiveTransfersList()
                continue
            }

            val active = nextItem.copy(
                status = TransferStatus.TRANSFERRING,
                channelSlot = slotIndex
            )
            activeItemMap[active.id] = active
            setSlotItem(slotIndex, active)
            updateActiveTransfersList()

            // Stream file over socket
            transferFileOverSocket(active, slotIndex, targetPort, targetIp)
            setSlotItem(slotIndex, null)
        }

        // If both queues empty and both slots idle, finish
        if (pendingSendQueue.isEmpty() && _slot0Active.value == null && _slot1Active.value == null) {
            _isTransferring.value = false
            _totalSpeedBytesPerSec.value = 0L
        }
    }

    private suspend fun transferFileOverSocket(
        item: TransferItem,
        slotIndex: Int,
        targetPort: Int,
        targetIp: String
    ) = withContext(Dispatchers.IO) {
        var socket: Socket? = null
        var inputStream: InputStream? = null
        val startTime = System.currentTimeMillis()

        try {
            // Open input stream from Uri or File
            inputStream = if (item.uriString != null) {
                context.contentResolver.openInputStream(Uri.parse(item.uriString))
            } else if (item.filePath != null) {
                FileInputStream(File(item.filePath))
            } else null

            if (inputStream == null) {
                throw FileNotFoundException("No se pudo abrir el archivo: ${item.name}")
            }

            socket = Socket().apply {
                tcpNoDelay = true
                sendBufferSize = TransferProtocol.BUFFER_SIZE * 4
                connect(InetSocketAddress(targetIp, targetPort), 10000)
            }

            val dos = DataOutputStream(BufferedOutputStream(socket.getOutputStream(), TransferProtocol.BUFFER_SIZE))
            dos.writeInt(TransferProtocol.MAGIC_HEADER)
            dos.writeUTF(item.name)
            dos.writeLong(item.size)
            dos.writeUTF(item.mimeType)
            dos.writeUTF(android.os.Build.MODEL ?: "ZapShare Emisor")
            dos.flush()

            val buffer = ByteArray(TransferProtocol.BUFFER_SIZE)
            var totalSent = 0L
            var lastUpdate = System.currentTimeMillis()
            var bytesSinceLastUpdate = 0L

            while (totalSent < item.size && !cancelledItemIds.contains(item.id)) {
                val read = inputStream.read(buffer)
                if (read == -1) break

                dos.write(buffer, 0, read)
                totalSent += read
                bytesSinceLastUpdate += read

                val now = System.currentTimeMillis()
                val elapsed = now - lastUpdate
                if (elapsed >= 100 || totalSent == item.size) {
                    val speed = if (elapsed > 0) (bytesSinceLastUpdate * 1000) / elapsed else 0L
                    val progress = if (item.size > 0) (totalSent.toFloat() / item.size) else 1f
                    val updated = item.copy(
                        transferredBytes = totalSent,
                        progress = progress,
                        speedBytesPerSec = speed
                    )
                    activeItemMap[item.id] = updated
                    setSlotItem(slotIndex, updated)
                    updateActiveTransfersList()

                    lastUpdate = now
                    bytesSinceLastUpdate = 0L
                }
            }

            dos.flush()

            val completedItem = item.copy(
                transferredBytes = item.size,
                progress = 1f,
                speedBytesPerSec = 0L,
                status = TransferStatus.COMPLETED
            )
            activeItemMap[item.id] = completedItem
            updateActiveTransfersList()

            // Record in Room Database
            val duration = (System.currentTimeMillis() - startTime).coerceAtLeast(1)
            val avgSpeed = (item.size * 1000) / duration
            repository.recordTransfer(
                TransferEntity(
                    fileName = item.name,
                    fileSize = item.size,
                    mimeType = item.mimeType,
                    direction = "SEND",
                    peerName = item.peerDeviceName,
                    status = "COMPLETED",
                    durationMs = duration,
                    avgSpeedBytesPerSec = avgSpeed,
                    filePathOrUri = item.uriString ?: item.filePath
                )
            )
        } catch (e: Exception) {
            Log.e("ZapEngine", "Error streaming file ${item.name}: ${e.message}")
            val failed = item.copy(
                status = TransferStatus.FAILED,
                errorMessage = e.localizedMessage ?: "Error de conexión",
                speedBytesPerSec = 0L
            )
            activeItemMap[item.id] = failed
            updateActiveTransfersList()
        } finally {
            try { inputStream?.close() } catch (_: Exception) {}
            try { socket?.close() } catch (_: Exception) {}
        }
    }

    // Pure real transfer engine - simulation logic removed
    fun cancelTransfer(id: String) {
        cancelledItemIds.add(id)
        activeItemMap[id]?.let {
            val cancelled = it.copy(status = TransferStatus.CANCELLED, speedBytesPerSec = 0L)
            activeItemMap[id] = cancelled
            if (_slot0Active.value?.id == id) setSlotItem(0, null)
            if (_slot1Active.value?.id == id) setSlotItem(1, null)
            updateActiveTransfersList()
        }
    }

    fun clearCompleted() {
        val remaining = activeItemMap.values.filter {
            it.status == TransferStatus.TRANSFERRING || it.status == TransferStatus.QUEUED
        }
        activeItemMap.clear()
        remaining.forEach { activeItemMap[it.id] = it }
        updateActiveTransfersList()
    }

    private fun setSlotItem(slot: Int, item: TransferItem?) {
        if (slot == 0) {
            _slot0Active.value = item
        } else {
            _slot1Active.value = item
        }
    }

    private fun updateActiveTransfersList() {
        _activeTransfers.value = activeItemMap.values
            .sortedWith(
                compareByDescending<TransferItem> { it.status == TransferStatus.TRANSFERRING }
                    .thenByDescending { it.status == TransferStatus.QUEUED }
                    .thenByDescending { it.timestamp }
            )
    }
}
