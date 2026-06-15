package com.investtrack.data.dao

import androidx.room.*
import com.investtrack.data.models.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY transactionDate DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY transactionDate DESC")
    fun getTransactionsByAccount(accountId: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId AND securityId = :securityId ORDER BY transactionDate DESC")
    fun getTransactionsByFolio(accountId: Long, securityId: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): Transaction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    // Get distinct (accountId, securityId) pairs for folio view
    @Query("""
        SELECT DISTINCT t.accountId, t.securityId, 
               COALESCE(SUM(CASE WHEN t.type IN ('BUY','SIP','SWITCH_IN','BONUS') THEN t.quantity 
                                 WHEN t.type IN ('SELL','SWP','SWITCH_OUT') THEN -t.quantity 
                                 ELSE 0 END), 0) as netQty
        FROM transactions t
        WHERE t.accountId = :accountId
        GROUP BY t.accountId, t.securityId
        HAVING netQty > 0
    """)
    suspend fun getActiveFoliosByAccount(accountId: Long): List<FolioRaw>

    @Query("""
        SELECT DISTINCT t.accountId, t.securityId,
               COALESCE(SUM(CASE WHEN t.type IN ('BUY','SIP','SWITCH_IN','BONUS') THEN t.quantity 
                                 WHEN t.type IN ('SELL','SWP','SWITCH_OUT') THEN -t.quantity 
                                 ELSE 0 END), 0) as netQty
        FROM transactions t
        GROUP BY t.accountId, t.securityId
        HAVING netQty > 0
    """)
    suspend fun getAllActiveFolios(): List<FolioRaw>

    @Query("""
        SELECT COALESCE(SUM(CASE WHEN type IN ('BUY','SIP','SWITCH_IN','BONUS') THEN amount 
                               WHEN type IN ('SELL','SWP','SWITCH_OUT') THEN -amount 
                               ELSE 0 END), 0)
        FROM transactions 
        WHERE accountId = :accountId AND securityId = :securityId
    """)
    suspend fun getNetInvestment(accountId: Long, securityId: Long): Double

    @Query("""
        SELECT COALESCE(SUM(CASE WHEN type IN ('BUY','SIP','SWITCH_IN','BONUS') THEN quantity 
                               WHEN type IN ('SELL','SWP','SWITCH_OUT') THEN -quantity 
                               ELSE 0 END), 0)
        FROM transactions 
        WHERE accountId = :accountId AND securityId = :securityId
    """)
    suspend fun getNetQuantity(accountId: Long, securityId: Long): Double

    @Query("SELECT DISTINCT folioNumber FROM transactions WHERE accountId = :accountId AND securityId = :securityId AND folioNumber != ''")
    suspend fun getFolioNumbers(accountId: Long, securityId: Long): List<String>
}

data class FolioRaw(
    val accountId: Long,
    val securityId: Long,
    val netQty: Double
)
