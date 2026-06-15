package com.investtrack.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: AccountType,
    val description: String = "",
    val currency: String = "INR",
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
) : Parcelable

enum class AccountType {
    INDIVIDUAL, JOINT, DEMAT, NRI, CORPORATE
}
