package com.example.data.model

import androidx.room.Entity
import androidx.room.Ignore
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
    val barcode: String = "",
    val sku: String = "",
    val name: String,
    val category: String = "General",
    val costPrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val currentStock: Int = 0,
    val reorderThreshold: Int = 10,
    val idealStock: Int = 50,
    val unit: String = "pcs",
    val locationRack: String = "Aisle 1",
    val supplier: String = "Direct Wholesale",
    val notes: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
) {
    /**
     * Alias for currentStock to support standard inventory terminology.
     */
    val quantity: Int
        get() = currentStock

    @Ignore
    constructor(
        name: String,
        quantity: Int,
        barcode: String,
        id: Long = 0L
    ) : this(
        id = id,
        barcode = barcode,
        sku = if (barcode.isNotBlank()) "SKU-${barcode.takeLast(6)}" else "SKU-${System.currentTimeMillis() % 100000}",
        name = name,
        category = "General",
        costPrice = 0.0,
        sellingPrice = 0.0,
        currentStock = quantity
    )
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
