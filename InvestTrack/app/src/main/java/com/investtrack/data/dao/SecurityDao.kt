package com.investtrack.data.dao

import androidx.room.*
import com.investtrack.data.models.Security
import com.investtrack.data.models.SecurityType
import kotlinx.coroutines.flow.Flow

@Dao
interface SecurityDao {
    @Query("SELECT * FROM securities WHERE isActive = 1 ORDER BY name ASC")
    fun getAllSecurities(): Flow<List<Security>>

    @Query("SELECT * FROM securities WHERE type = :type AND isActive = 1 ORDER BY name ASC")
    fun getSecuritiesByType(type: SecurityType): Flow<List<Security>>

    @Query("SELECT * FROM securities WHERE id = :id")
    suspend fun getSecurityById(id: Long): Security?

    @Query("SELECT * FROM securities WHERE name LIKE '%' || :query || '%' OR symbol LIKE '%' || :query || '%'")
    suspend fun searchSecurities(query: String): List<Security>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSecurity(security: Security): Long

    @Update
    suspend fun updateSecurity(security: Security)

    @Query("UPDATE securities SET currentPrice = :price, lastUpdated = :timestamp WHERE id = :id")
    suspend fun updatePrice(id: Long, price: Double, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE securities SET isActive = 0 WHERE id = :id")
    suspend fun deactivateSecurity(id: Long)

    @Query("SELECT * FROM securities WHERE isActive = 1 ORDER BY name ASC")
    suspend fun getAllSecuritiesList(): List<Security>
}
