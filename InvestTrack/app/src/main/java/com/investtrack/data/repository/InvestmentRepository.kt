package com.investtrack.data.repository

import com.investtrack.data.dao.AccountDao
import com.investtrack.data.dao.SecurityDao
import com.investtrack.data.dao.TransactionDao
import com.investtrack.data.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvestmentRepository @Inject constructor(
    private val accountDao: AccountDao,
    private val securityDao: SecurityDao,
    private val transactionDao: TransactionDao
) {
    // Accounts
    fun getAllAccounts(): Flow<List<Account>> = accountDao.getAllAccounts()
    suspend fun getAccountById(id: Long): Account? = accountDao.getAccountById(id)
    suspend fun insertAccount(account: Account): Long = accountDao.insertAccount(account)
    suspend fun updateAccount(account: Account) = accountDao.updateAccount(account)
    suspend fun deactivateAccount(id: Long) = accountDao.deactivateAccount(id)

    // Securities
    fun getAllSecurities(): Flow<List<Security>> = securityDao.getAllSecurities()
    fun getSecuritiesByType(type: SecurityType): Flow<List<Security>> = securityDao.getSecuritiesByType(type)
    suspend fun getSecurityById(id: Long): Security? = securityDao.getSecurityById(id)
    suspend fun searchSecurities(query: String): List<Security> = securityDao.searchSecurities(query)
    suspend fun insertSecurity(security: Security): Long = securityDao.insertSecurity(security)
    suspend fun updateSecurity(security: Security) = securityDao.updateSecurity(security)
    suspend fun updatePrice(id: Long, price: Double) = securityDao.updatePrice(id, price)
    suspend fun deactivateSecurity(id: Long) = securityDao.deactivateSecurity(id)

    // Transactions
    fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions()
    fun getTransactionsByAccount(accountId: Long): Flow<List<Transaction>> = transactionDao.getTransactionsByAccount(accountId)
    fun getTransactionsByFolio(accountId: Long, securityId: Long): Flow<List<Transaction>> = transactionDao.getTransactionsByFolio(accountId, securityId)
    suspend fun insertTransaction(transaction: Transaction): Long = transactionDao.insertTransaction(transaction)
    suspend fun updateTransaction(transaction: Transaction) = transactionDao.updateTransaction(transaction)
    suspend fun deleteTransaction(transaction: Transaction) = transactionDao.deleteTransaction(transaction)

    // Build Folios for an account
    suspend fun getFoliosForAccount(accountId: Long): List<Folio> {
        val rawFolios = transactionDao.getActiveFoliosByAccount(accountId)
        return buildFolios(rawFolios)
    }

    // Build consolidated Folios across all accounts
    suspend fun getAllFolios(): List<Folio> {
        val rawFolios = transactionDao.getAllActiveFolios()
        return buildFolios(rawFolios)
    }

    private suspend fun buildFolios(rawFolios: List<com.investtrack.data.dao.FolioRaw>): List<Folio> {
        val result = mutableListOf<Folio>()
        for (raw in rawFolios) {
            val account = accountDao.getAccountById(raw.accountId) ?: continue
            val security = securityDao.getSecurityById(raw.securityId) ?: continue
            val netInvested = transactionDao.getNetInvestment(raw.accountId, raw.securityId)
            val netQty = raw.netQty
            val currentPrice = security.currentPrice
            val currentValue = netQty * currentPrice
            val pnl = currentValue - netInvested
            val pnlPercent = if (netInvested > 0) (pnl / netInvested) * 100 else 0.0
            val avgCost = if (netQty > 0) netInvested / netQty else 0.0
            val folioNums = transactionDao.getFolioNumbers(raw.accountId, raw.securityId)

            result.add(
                Folio(
                    accountId = raw.accountId,
                    accountName = account.name,
                    securityId = raw.securityId,
                    securityName = security.name,
                    securitySymbol = security.symbol,
                    securityType = security.type,
                    folioNumber = folioNums.joinToString(", "),
                    totalUnits = netQty,
                    avgCostPrice = avgCost,
                    totalInvested = netInvested,
                    currentValue = currentValue,
                    currentPrice = currentPrice,
                    pnl = pnl,
                    pnlPercent = pnlPercent
                )
            )
        }
        return result
    }

    suspend fun getPortfolioSummary(): PortfolioSummary {
        val folios = getAllFolios()
        val totalInvested = folios.sumOf { it.totalInvested }
        val currentValue = folios.sumOf { it.currentValue }
        val pnl = currentValue - totalInvested
        val pnlPercent = if (totalInvested > 0) (pnl / totalInvested) * 100 else 0.0
        val accountCount = accountDao.getAccountCount()
        return PortfolioSummary(
            totalInvested = totalInvested,
            currentValue = currentValue,
            totalPnL = pnl,
            totalPnLPercent = pnlPercent,
            dayChange = 0.0,
            dayChangePercent = 0.0,
            folioCount = folios.size,
            accountCount = accountCount
        )
    }

    suspend fun getAccountPortfolioSummary(accountId: Long): PortfolioSummary {
        val folios = getFoliosForAccount(accountId)
        val totalInvested = folios.sumOf { it.totalInvested }
        val currentValue = folios.sumOf { it.currentValue }
        val pnl = currentValue - totalInvested
        val pnlPercent = if (totalInvested > 0) (pnl / totalInvested) * 100 else 0.0
        return PortfolioSummary(
            totalInvested = totalInvested,
            currentValue = currentValue,
            totalPnL = pnl,
            totalPnLPercent = pnlPercent,
            dayChange = 0.0,
            dayChangePercent = 0.0,
            folioCount = folios.size,
            accountCount = 1
        )
    }
}
