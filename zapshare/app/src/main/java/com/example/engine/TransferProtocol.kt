package com.example.engine

object TransferProtocol {
    const val CONTROL_PORT = 8987
    const val DATA_PORT_CHANNEL_A = 8988
    const val DATA_PORT_CHANNEL_B = 8989

    // High throughput 64KB buffer for optimal TCP stream flow over 802.11ac/ax Wi-Fi Direct
    const val BUFFER_SIZE = 65536

    // Magic header bytes for protocol validation
    const val MAGIC_HEADER: Int = 0x5A415053 // "ZAPS"

    const val CMD_PREPARE_FILE: Byte = 1
    const val CMD_FILE_DATA: Byte = 2
    const val CMD_FILE_COMPLETE: Byte = 3
    const val CMD_HEARTBEAT: Byte = 4
    const val CMD_CANCEL: Byte = 5
}
