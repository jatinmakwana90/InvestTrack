package com.investtrack.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(entity = Account::class, parentColumns = ["id"], childColumns = ["accountId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Security::class, parentColumns = ["id"], childColumns = ["securityId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("accountId"), Index("securityId")]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val accountId: Long,
    val securityId: Long,
    val type: TransactionType,
    val quantity: Double,
    val price: Double,
    val amount: Double,             // quantity * price
    val charges: Double = 0.0,     // brokerage, exit load etc
    val nav: Double? = null,       // For MF transactions
    val units: Double? = null,     // For MF transactions
    val folioNumber: String = "",  // For MF
    val notes: String = "",
    val transactionDate: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable

enum class TransactionType(val displayName: String) {
    BUY("Buy"),
    SELL("Sell"),
    DIVIDEND("Dividend"),
    BONUS("Bonus"),
    SPLIT("Split"),
    INTEREST("Interest"),
    MATURITY("Maturity"),
    SIP("SIP"),
    SWP("SWP"),
    SWITCH_IN("Switch In"),
    SWITCH_OUT("Switch Out")
}

// Folio - groups transactions by security in an account
data class Folio(
    val accountId: Long,
    val accountName: String,
    val securityId: Long,
    val securityName: String,
    val securitySymbol: String,
    val securityType: SecurityType,
    val folioNumber: String,
    val totalUnits: Double,
    val avgCostPrice: Double,
    val totalInvested: Double,
    val currentValue: Double,
    val currentPrice: Double,
    val pnl: Double,
    val pnlPercent: Double,
    val dayChange: Double = 0.0
)

// Aggregated portfolio summary
data class PortfolioSummary(
    val totalInvested: Double,
    val currentValue: Double,
    val totalPnL: Double,
    val totalPnLPercent: Double,
    val dayChange: Double,
    val dayChangePercent: Double,
    val folioCount: Int,
    val accountCount: Int
)
