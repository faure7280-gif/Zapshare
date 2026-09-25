package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.local.TransferRepository
import com.example.engine.DualChannelTransferEngine
import com.example.p2p.WifiDirectManager
import com.example.util.FilePickerManager

class ZapShareApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { TransferRepository(database.transferDao()) }
    val transferEngine by lazy { DualChannelTransferEngine(this, repository) }
    val wifiDirectManager by lazy { WifiDirectManager(this) }
    val filePickerManager by lazy { FilePickerManager(this) }

    override fun onCreate() {
        super.onCreate()
        wifiDirectManager.initialize()
    }
}
