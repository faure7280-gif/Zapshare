package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transfer_history")
data class TransferEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String,
    val direction: String, // "SEND" or "RECEIVE"
    val peerName: String,
    val status: String, // "COMPLETED" or "FAILED"
    val durationMs: Long,
    val avgSpeedBytesPerSec: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val filePathOrUri: String? = null
)
