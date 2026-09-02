package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.StockMovement
import kotlinx.coroutines.flow.Flow

@Dao
interface StockMovementDao {
    @Query("SELECT * FROM stock_movements ORDER BY timestamp DESC")
    fun getAllMovements(): Flow<List<StockMovement>>

    @Query("SELECT * FROM stock_movements ORDER BY timestamp DESC")
    suspend fun getAllMovementsSync(): List<StockMovement>

    @Query("SELECT * FROM stock_movements WHERE productId = :productId ORDER BY timestamp DESC")
    fun getMovementsForProduct(productId: Long): Flow<List<StockMovement>>

    @Query("SELECT * FROM stock_movements WHERE productId = :productId ORDER BY timestamp DESC")
    suspend fun getMovementsForProductSync(productId: Long): List<StockMovement>

    @Query("SELECT * FROM stock_movements WHERE type = :type ORDER BY timestamp DESC")
    fun getMovementsByType(type: com.example.data.model.MovementType): Flow<List<StockMovement>>

    @Query("SELECT * FROM stock_movements WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getMovementsBetweenTimestamps(startTime: Long, endTime: Long): Flow<List<StockMovement>>

    @Query("SELECT * FROM stock_movements ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentMovements(limit: Int): Flow<List<StockMovement>>

    @Query("SELECT * FROM stock_movements WHERE id = :id LIMIT 1")
    suspend fun getMovementById(id: Long): StockMovement?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: StockMovement): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovements(movements: List<StockMovement>): List<Long>

    @Query("DELETE FROM stock_movements WHERE id = :id")
    suspend fun deleteMovementById(id: Long)

    @Query("DELETE FROM stock_movements")
    suspend fun clearAllMovements()
}
