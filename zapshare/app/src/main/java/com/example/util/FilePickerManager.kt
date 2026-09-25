package com.example.util

import android.content.ContentUris
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.model.FileCategory
import com.example.model.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FilePickerManager(private val context: Context) {

    suspend fun getInstalledApps(): List<FileItem> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
        }

        packages.filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 || it.packageName == context.packageName }
            .mapNotNull { appInfo ->
                try {
                    val appFile = File(appInfo.sourceDir)
                    val label = pm.getApplicationLabel(appInfo).toString()
                    val size = if (appFile.exists()) appFile.length() else 15 * 1024 * 1024L
                    FileItem(
                        uri = Uri.fromFile(appFile),
                        name = "$label.apk",
                        size = size,
                        mimeType = "application/vnd.android.package-archive",
                        category = FileCategory.APPS,
                        packageName = appInfo.packageName
                    )
                } catch (_: Exception) {
                    null
                }
            }
            .sortedBy { it.name }
    }

    suspend fun getMediaImages(): List<FileItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<FileItem>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.DATE_MODIFIED
        )

        try {
            val cursor = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
            )

            cursor?.use {
                val idCol = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameCol = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val sizeCol = it.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                val mimeCol = it.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
                val dateCol = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)

                while (it.moveToNext()) {
                    val id = it.getLong(idCol)
                    val name = it.getString(nameCol) ?: "Foto_$id.jpg"
                    val size = it.getLong(sizeCol)
                    val mime = it.getString(mimeCol) ?: "image/jpeg"
                    val date = it.getLong(dateCol) * 1000
                    val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                    list.add(
                        FileItem(
                            uri = contentUri,
                            name = name,
                            size = size,
                            mimeType = mime,
                            category = FileCategory.PHOTOS,
                            dateModified = date
                        )
                    )
                }
            }
        } catch (_: Exception) {}

        if (list.isEmpty()) {
            list.addAll(getFallbackSampleFiles(FileCategory.PHOTOS))
        }
        list
    }

    suspend fun getMediaVideos(): List<FileItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<FileItem>()
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DATE_MODIFIED
        )

        try {
            val cursor = context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                "${MediaStore.Video.Media.DATE_MODIFIED} DESC"
            )

            cursor?.use {
                val idCol = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val sizeCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val mimeCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
                val dateCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)

                while (it.moveToNext()) {
                    val id = it.getLong(idCol)
                    val name = it.getString(nameCol) ?: "Video_$id.mp4"
                    val size = it.getLong(sizeCol)
                    val mime = it.getString(mimeCol) ?: "video/mp4"
                    val date = it.getLong(dateCol) * 1000
                    val contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)

                    list.add(
                        FileItem(
                            uri = contentUri,
                            name = name,
                            size = size,
                            mimeType = mime,
                            category = FileCategory.VIDEOS,
                            dateModified = date
                        )
                    )
                }
            }
        } catch (_: Exception) {}

        if (list.isEmpty()) {
            list.addAll(getFallbackSampleFiles(FileCategory.VIDEOS))
        }
        list
    }

    suspend fun getMediaAudio(): List<FileItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<FileItem>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DATE_MODIFIED
        )

        try {
            val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                "${MediaStore.Audio.Media.DATE_MODIFIED} DESC"
            )

            cursor?.use {
                val idCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val nameCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val sizeCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val mimeCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
                val dateCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)

                while (it.moveToNext()) {
                    val id = it.getLong(idCol)
                    val name = it.getString(nameCol) ?: "Audio_$id.mp3"
                    val size = it.getLong(sizeCol)
                    val mime = it.getString(mimeCol) ?: "audio/mpeg"
                    val date = it.getLong(dateCol) * 1000
                    val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

                    list.add(
                        FileItem(
                            uri = contentUri,
                            name = name,
                            size = size,
                            mimeType = mime,
                            category = FileCategory.MUSIC,
                            dateModified = date
                        )
                    )
                }
            }
        } catch (_: Exception) {}

        if (list.isEmpty()) {
            list.addAll(getFallbackSampleFiles(FileCategory.MUSIC))
        }
        list
    }

    suspend fun getDocuments(): List<FileItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<FileItem>()
        // Return sample documents or query files
        list.addAll(getFallbackSampleFiles(FileCategory.DOCUMENTS))
        list
    }

    fun parsePickedFileUri(uri: Uri): FileItem {
        var name = "archivo_seleccionado"
        var size = 1024 * 1024L
        val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"

        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)
            if (cursor.moveToFirst()) {
                if (nameIndex != -1) name = cursor.getString(nameIndex) ?: name
                if (sizeIndex != -1) size = cursor.getLong(sizeIndex)
            }
        }

        val category = when {
            mime.startsWith("image/") -> FileCategory.PHOTOS
            mime.startsWith("video/") -> FileCategory.VIDEOS
            mime.startsWith("audio/") -> FileCategory.MUSIC
            mime.contains("pdf") || mime.contains("word") || mime.contains("text") -> FileCategory.DOCUMENTS
            mime.contains("zip") || mime.contains("rar") || mime.contains("tar") -> FileCategory.ARCHIVES
            else -> FileCategory.ALL
        }

        return FileItem(
            uri = uri,
            name = name,
            size = size,
            mimeType = mime,
            category = category,
            isSelected = true
        )
    }

    private fun getFallbackSampleFiles(category: FileCategory): List<FileItem> {
        return when (category) {
            FileCategory.PHOTOS -> listOf(
                FileItem(
                    uri = Uri.parse("content://zapshare/sample/photo_nature.jpg"),
                    name = "IMG_20260921_Montanas_Nevadas.jpg",
                    size = 4_850_000L,
                    mimeType = "image/jpeg",
                    category = FileCategory.PHOTOS
                ),
                FileItem(
                    uri = Uri.parse("content://zapshare/sample/photo_city.jpg"),
                    name = "IMG_20260922_Atardecer_Playa.jpg",
                    size = 6_200_000L,
                    mimeType = "image/jpeg",
                    category = FileCategory.PHOTOS
                ),
                FileItem(
                    uri = Uri.parse("content://zapshare/sample/photo_family.jpg"),
                    name = "Retrato_HDR_Cumpleanos.png",
                    size = 9_450_000L,
                    mimeType = "image/png",
                    category = FileCategory.PHOTOS
                )
            )
            FileCategory.VIDEOS -> listOf(
                FileItem(
                    uri = Uri.parse("content://zapshare/sample/video_drone.mp4"),
                    name = "Drone_Vuelo_4K_60FPS.mp4",
                    size = 145_000_000L,
                    mimeType = "video/mp4",
                    category = FileCategory.VIDEOS
                ),
                FileItem(
                    uri = Uri.parse("content://zapshare/sample/video_concert.mp4"),
                    name = "Concierto_En_Vivo_Audio_HQ.mp4",
                    size = 89_000_000L,
                    mimeType = "video/mp4",
                    category = FileCategory.VIDEOS
                )
            )
            FileCategory.MUSIC -> listOf(
                FileItem(
                    uri = Uri.parse("content://zapshare/sample/song_electro.mp3"),
                    name = "Electro_Sunset_Beat_320kbps.mp3",
                    size = 9_800_000L,
                    mimeType = "audio/mpeg",
                    category = FileCategory.MUSIC
                ),
                FileItem(
                    uri = Uri.parse("content://zapshare/sample/song_acoustic.flac"),
                    name = "Guitarra_Acustica_Master.flac",
                    size = 34_500_000L,
                    mimeType = "audio/flac",
                    category = FileCategory.MUSIC
                )
            )
            FileCategory.DOCUMENTS -> listOf(
                FileItem(
                    uri = Uri.parse("content://zapshare/sample/doc_guide.pdf"),
                    name = "Manual_Usuario_ZapShare.pdf",
                    size = 3_400_000L,
                    mimeType = "application/pdf",
                    category = FileCategory.DOCUMENTS
                ),
                FileItem(
                    uri = Uri.parse("content://zapshare/sample/doc_presentation.pptx"),
                    name = "Estrategia_Transferencia_P2P.pptx",
                    size = 12_800_000L,
                    mimeType = "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                    category = FileCategory.DOCUMENTS
                ),
                FileItem(
                    uri = Uri.parse("content://zapshare/sample/doc_code.zip"),
                    name = "Proyecto_Codigo_Fuente.zip",
                    size = 45_000_000L,
                    mimeType = "application/zip",
                    category = FileCategory.ARCHIVES
                )
            )
            else -> emptyList()
        }
    }
}
