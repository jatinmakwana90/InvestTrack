package com.investtrack.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.investtrack.data.dao.AccountDao
import com.investtrack.data.dao.SecurityDao
import com.investtrack.data.dao.TransactionDao
import com.investtrack.data.models.Account
import com.investtrack.data.models.Security
import com.investtrack.data.models.Transaction

@Database(
    entities = [Account::class, Security::class, Transaction::class],
    version = 1,
    exportSchema = false
)
abstract class InvestTrackDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun securityDao(): SecurityDao
    abstract fun transactionDao(): TransactionDao
}
