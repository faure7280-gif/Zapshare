package com.example.model

import java.util.UUID

enum class TransferStatus {
    QUEUED,
    TRANSFERRING,
    COMPLETED,
    FAILED,
    CANCELLED
}

enum class TransferDirection {
    SENDING,
    RECEIVING
}

data class TransferItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val size: Long,
    val mimeType: String,
    val uriString: String? = null,
    val filePath: String? = null,
    val progress: Float = 0f,
    val transferredBytes: Long = 0L,
    val speedBytesPerSec: Long = 0L,
    val channelSlot: Int = 0, // 0 = Canal A, 1 = Canal B
    val status: TransferStatus = TransferStatus.QUEUED,
    val direction: TransferDirection = TransferDirection.SENDING,
    val timestamp: Long = System.currentTimeMillis(),
    val peerDeviceName: String = "Dispositivo Par",
    val errorMessage: String? = null
) {
    val progressPercent: Int
        get() = (progress * 100).toInt().coerceIn(0, 100)

    val speedFormatted: String
        get() {
            val mbs = speedBytesPerSec / (1024.0 * 1024.0)
            return if (mbs >= 1.0) {
                String.format("%.1f MB/s", mbs)
            } else {
                val kbs = speedBytesPerSec / 1024.0
                String.format("%.0f KB/s", kbs)
            }
        }

    val formattedSize: String
        get() = formatBytes(size)

    val formattedTransferred: String
        get() = formatBytes(transferredBytes)

    companion object {
        fun formatBytes(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
                .coerceIn(0, units.size - 1)
            val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
            return String.format("%.1f %s", value, units[digitGroups])
        }
    }
}
