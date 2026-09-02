package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.InventoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryItemDao {

    @Query("SELECT * FROM inventory_items ORDER BY name ASC")
    fun getAllItems(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE id = :id LIMIT 1")
    fun observeItemById(id: Long): Flow<InventoryItem?>

    @Query("SELECT * FROM inventory_items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: Long): InventoryItem?

    @Query("SELECT * FROM inventory_items WHERE barcode = :barcode LIMIT 1")
    fun observeItemByBarcode(barcode: String): Flow<InventoryItem?>

    @Query("SELECT * FROM inventory_items WHERE barcode = :barcode LIMIT 1")
    suspend fun findItemByBarcodeSync(barcode: String): InventoryItem?

    @Query("SELECT * FROM inventory_items WHERE quantity <= reorderThreshold ORDER BY quantity ASC")
    fun getLowStockItems(): Flow<List<InventoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<InventoryItem>): List<Long>

    @Update
    suspend fun updateItem(item: InventoryItem)

    @Query("UPDATE inventory_items SET quantity = :newQuantity, lastUpdated = :timestamp WHERE id = :id")
    suspend fun updateQuantity(id: Long, newQuantity: Int, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteItem(item: InventoryItem)

    @Query("DELETE FROM inventory_items WHERE id = :id")
    suspend fun deleteItemById(id: Long)

    @Query("DELETE FROM inventory_items")
    suspend fun clearAll()
}
