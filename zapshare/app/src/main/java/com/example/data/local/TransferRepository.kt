package com.example.data.local

import kotlinx.coroutines.flow.Flow

class TransferRepository(private val transferDao: TransferDao) {
    val allTransfers: Flow<List<TransferEntity>> = transferDao.getAllTransfers()

    suspend fun recordTransfer(transfer: TransferEntity): Long {
        return transferDao.insertTransfer(transfer)
    }

    suspend fun deleteTransfer(id: Long) {
        transferDao.deleteTransferById(id)
    }

    suspend fun clearHistory() {
        transferDao.clearAll()
    }
}
