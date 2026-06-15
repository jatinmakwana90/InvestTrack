package com.investtrack.data.dao

import androidx.room.*
import com.investtrack.data.models.Account
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE isActive = 1 ORDER BY name ASC")
    fun getAllAccounts(): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Long): Account?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: Account): Long

    @Update
    suspend fun updateAccount(account: Account)

    @Query("UPDATE accounts SET isActive = 0 WHERE id = :id")
    suspend fun deactivateAccount(id: Long)

    @Query("SELECT COUNT(*) FROM accounts WHERE isActive = 1")
    suspend fun getAccountCount(): Int
}
