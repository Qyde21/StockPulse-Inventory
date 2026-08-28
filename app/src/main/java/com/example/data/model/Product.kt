package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class StockStatus {
    OUT_OF_STOCK,
    LOW_STOCK,
    IN_STOCK,
    OVERSTOCK
}

@Entity(
    tableName = "products",
    indices = [
        Index(value = ["barcode"], unique = false),
        Index(value = ["category"]),
        Index(value = ["sku"], unique = false)
    ]
)
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val barcode: String,
    val sku: String,
    val name: String,
    val category: String,
    val costPrice: Double,
    val sellingPrice: Double,
    val currentStock: Int,
    val reorderThreshold: Int = 10,
    val idealStock: Int = 50,
    val unit: String = "pcs",
    val locationRack: String = "Aisle 1",
    val supplier: String = "Direct Wholesale",
    val notes: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val stockStatus: StockStatus
        get() = when {
            currentStock <= 0 -> StockStatus.OUT_OF_STOCK
            currentStock <= reorderThreshold -> StockStatus.LOW_STOCK
            currentStock > idealStock * 1.5 -> StockStatus.OVERSTOCK
            else -> StockStatus.IN_STOCK
        }

    val profitMargin: Double
        get() = if (sellingPrice > 0) {
            ((sellingPrice - costPrice) / sellingPrice) * 100.0
        } else 0.0

    val totalRetailValue: Double
        get() = currentStock.coerceAtLeast(0) * sellingPrice

    val totalCostValue: Double
        get() = currentStock.coerceAtLeast(0) * costPrice

    val stockDeficit: Int
        get() = if (currentStock < reorderThreshold) {
            idealStock - currentStock
        } else 0
}
