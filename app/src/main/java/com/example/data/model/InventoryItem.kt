package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room database entity representing an inventory item with name, quantity, and barcode,
 * persisting consistently across app sessions.
 */
@Entity(
    tableName = "inventory_items",
    indices = [
        Index(value = ["barcode"], unique = false)
    ]
)
data class InventoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val quantity: Int,
    val barcode: String,
    val category: String = "General",
    val costPrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val reorderThreshold: Int = 10,
    val unit: String = "pcs",
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun toProduct(): Product = Product(
        id = id,
        barcode = barcode,
        sku = if (barcode.isNotBlank()) "SKU-${barcode.takeLast(6)}" else "SKU-$id",
        name = name,
        category = category,
        costPrice = costPrice,
        sellingPrice = sellingPrice,
        currentStock = quantity,
        reorderThreshold = reorderThreshold,
        unit = unit,
        lastUpdated = lastUpdated
    )
}

fun Product.toInventoryItem(): InventoryItem = InventoryItem(
    id = id,
    name = name,
    quantity = currentStock,
    barcode = barcode,
    category = category,
    costPrice = costPrice,
    sellingPrice = sellingPrice,
    reorderThreshold = reorderThreshold,
    unit = unit,
    lastUpdated = lastUpdated
)
