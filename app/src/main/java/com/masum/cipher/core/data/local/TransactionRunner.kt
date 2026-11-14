package com.masum.cipher.core.data.local

import androidx.room.withTransaction
import javax.inject.Inject
import javax.inject.Singleton

interface TransactionRunner {
    suspend fun <T> run(block: suspend () -> T): T
}

@Singleton
class RoomTransactionRunner @Inject constructor(
    private val database: AppDatabase
) : TransactionRunner {
    override suspend fun <T> run(block: suspend () -> T): T = database.withTransaction { block() }
}
