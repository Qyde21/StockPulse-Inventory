package com.example.data.repository

import com.example.data.db.InventoryItemDao
import com.example.data.model.InventoryItem
import kotlinx.coroutines.flow.Flow

/**
 * Repository to persist and manage inventory items (name, quantity, barcode)
 * using Room to ensure data remains consistent across sessions.
 */
class InventoryItemRepository(
    private val inventoryItemDao: InventoryItemDao
) {
    val allItems: Flow<List<InventoryItem>> = inventoryItemDao.getAllItems()

    val lowStockItems: Flow<List<InventoryItem>> = inventoryItemDao.getLowStockItems()

    suspend fun insert(item: InventoryItem): Long = inventoryItemDao.insertItem(item)

    suspend fun insert(name: String, quantity: Int, barcode: String): Long {
        return inventoryItemDao.insertItem(
            InventoryItem(
                name = name.trim(),
                quantity = quantity.coerceAtLeast(0),
                barcode = barcode.trim()
            )
        )
    }

    suspend fun update(item: InventoryItem) {
        inventoryItemDao.updateItem(item)
    }

    suspend fun updateQuantity(id: Long, newQuantity: Int) {
        inventoryItemDao.updateQuantity(id, newQuantity.coerceAtLeast(0))
    }

    suspend fun delete(item: InventoryItem) {
        inventoryItemDao.deleteItem(item)
    }

    suspend fun deleteById(id: Long) {
        inventoryItemDao.deleteItemById(id)
    }

    suspend fun getById(id: Long): InventoryItem? = inventoryItemDao.getItemById(id)

    fun observeById(id: Long): Flow<InventoryItem?> = inventoryItemDao.observeItemById(id)

    suspend fun getByBarcode(barcode: String): InventoryItem? =
        inventoryItemDao.findItemByBarcodeSync(barcode.trim())

    fun observeByBarcode(barcode: String): Flow<InventoryItem?> =
        inventoryItemDao.observeItemByBarcode(barcode.trim())

    suspend fun clearAll() {
        inventoryItemDao.clearAll()
    }
}
