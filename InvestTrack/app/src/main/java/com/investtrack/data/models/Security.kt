package com.investtrack.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "securities")
data class Security(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val symbol: String,
    val type: SecurityType,
    val isin: String = "",
    val exchange: String = "",
    val sector: String = "",
    val currentPrice: Double = 0.0,
    val faceValue: Double = 10.0,
    val currency: String = "INR",
    val maturityDate: Long? = null,        // For FD/Bonds
    val interestRate: Double? = null,      // For FD/Bonds
    val nav: Double? = null,               // For Mutual Funds
    val amc: String = "",                  // For Mutual Funds
    val category: String = "",             // Large Cap, Mid Cap etc
    val lastUpdated: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
) : Parcelable

enum class SecurityType(val displayName: String) {
    MUTUAL_FUND("Mutual Fund"),
    EQUITY("Equity"),
    FIXED_DEPOSIT("Fixed Deposit"),
    BOND("Bond"),
    ETF("ETF"),
    GOLD("Gold"),
    REAL_ESTATE("Real Estate"),
    CRYPTO("Crypto"),
    PPF("PPF"),
    NPS("NPS")
}
