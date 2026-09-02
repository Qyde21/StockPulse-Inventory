package com.example.data.repository

import com.example.data.db.StockMovementDao
import com.example.data.model.MovementType
import com.example.data.model.StockMovement
import kotlinx.coroutines.flow.Flow

/**
 * Dedicated repository to track and query history of stock changes,
 * including timestamps, adjustment types (e.g., 'restock', 'sale'), and quantities.
 */
class StockMovementRepository(
    private val stockMovementDao: StockMovementDao
) {
    /**
     * All stock movements ordered by timestamp descending.
     */
    val allStockMovements: Flow<List<StockMovement>> = stockMovementDao.getAllMovements()

    /**
     * Synchronously get all stock movements.
     */
    suspend fun getAllStockMovementsSync(): List<StockMovement> =
        stockMovementDao.getAllMovementsSync()

    /**
     * Recent stock movements limited by count.
     */
    fun getRecentMovements(limit: Int = 20): Flow<List<StockMovement>> =
        stockMovementDao.getRecentMovements(limit)

    /**
     * Movements for a specific product by ID.
     */
    fun getMovementsForProduct(productId: Long): Flow<List<StockMovement>> =
        stockMovementDao.getMovementsForProduct(productId)

    /**
     * Movements filtered by adjustment type (e.g., RESTOCK, SALE).
     */
    fun getMovementsByType(type: MovementType): Flow<List<StockMovement>> =
        stockMovementDao.getMovementsByType(type)

    /**
     * Movements within a given timestamp interval.
     */
    fun getMovementsBetweenTimestamps(startTime: Long, endTime: Long): Flow<List<StockMovement>> =
        stockMovementDao.getMovementsBetweenTimestamps(startTime, endTime)

    /**
     * Inserts a stock change history entry with timestamp, adjustment type, and quantities.
     */
    suspend fun recordStockChange(
        productId: Long,
        productName: String = "",
        barcode: String = "",
        type: MovementType,
        quantityDelta: Int,
        previousStock: Int,
        newStock: Int,
        reason: String = "",
        unitPrice: Double = 0.0,
        timestamp: Long = System.currentTimeMillis()
    ): Long {
        val movement = StockMovement(
            productId = productId,
            productName = productName,
            barcode = barcode,
            type = type,
            quantityDelta = quantityDelta,
            previousStock = previousStock,
            newStock = newStock,
            reason = reason.ifBlank { type.displayName },
            unitPrice = unitPrice,
            timestamp = timestamp
        )
        return stockMovementDao.insertMovement(movement)
    }

    /**
     * Overload accepting string adjustment types (e.g., 'restock', 'sale', 'adjustment').
     */
    suspend fun recordStockChange(
        productId: Long,
        productName: String = "",
        barcode: String = "",
        adjustmentType: String,
        quantityDelta: Int,
        previousStock: Int,
        newStock: Int,
        reason: String = "",
        unitPrice: Double = 0.0,
        timestamp: Long = System.currentTimeMillis()
    ): Long {
        val type = MovementType.fromString(adjustmentType)
        return recordStockChange(
            productId = productId,
            productName = productName,
            barcode = barcode,
            type = type,
            quantityDelta = quantityDelta,
            previousStock = previousStock,
            newStock = newStock,
            reason = reason,
            unitPrice = unitPrice,
            timestamp = timestamp
        )
    }

    /**
     * Inserts a stock movement object directly.
     */
    suspend fun insertMovement(movement: StockMovement): Long =
        stockMovementDao.insertMovement(movement)

    /**
     * Clears all movement history logs.
     */
    suspend fun clearHistory() = stockMovementDao.clearAllMovements()
}
