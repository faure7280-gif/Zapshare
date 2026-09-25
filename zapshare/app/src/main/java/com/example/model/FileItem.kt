package com.example.model

import android.net.Uri

enum class FileCategory {
    ALL,
    APPS,
    PHOTOS,
    VIDEOS,
    MUSIC,
    DOCUMENTS,
    ARCHIVES
}

data class FileItem(
    val uri: Uri,
    val name: String,
    val size: Long,
    val mimeType: String,
    val category: FileCategory,
    val dateModified: Long = System.currentTimeMillis(),
    val packageName: String? = null, // For installed Android apps
    val isSelected: Boolean = false,
    val iconUri: Uri? = null
) {
    val formattedSize: String
        get() = TransferItem.formatBytes(size)
}
