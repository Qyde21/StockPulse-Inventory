package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class MovementType(val displayName: String, val isPositive: Boolean) {
    STOCK_IN("Stock In / Received", true),
    SALE("Sale (POS)", false),
    ADJUSTMENT("Manual Audit Count", false),
    RETURN("Customer Return", true),
    DAMAGED("Damaged / Expired", false)
}

@Entity(
    tableName = "stock_movements",
    indices = [
        Index(value = ["productId"]),
        Index(value = ["timestamp"])
    ]
)
data class StockMovement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val productName: String,
    val barcode: String,
    val type: MovementType,
    val quantityDelta: Int,
    val previousStock: Int,
    val newStock: Int,
    val reason: String = "",
    val unitPrice: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
