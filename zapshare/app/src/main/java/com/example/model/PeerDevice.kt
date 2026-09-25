package com.example.model

data class PeerDevice(
    val deviceName: String,
    val deviceAddress: String, // MAC or IP
    val status: String = "Disponible",
    val isGroupOwner: Boolean = false,
    val isConnected: Boolean = false,
    val ipAddress: String? = null,
    val port: Int = 8988,
    val signalStrength: Int = 85 // Percentage
)
