package com.example.data.repository

import com.example.data.db.ProductDao
import com.example.data.db.StockMovementDao
import com.example.data.model.AlertSeverity
import com.example.data.model.MovementType
import com.example.data.model.Product
import com.example.data.model.StockAlert
import com.example.data.model.StockMovement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class InventoryRepository(
    private val productDao: ProductDao,
    private val movementDao: StockMovementDao
) {
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()

    val stockAlerts: Flow<List<StockAlert>> = productDao.getLowStockAndOutOfStockProducts().map { products ->
        products.map { product ->
            val severity = if (product.currentStock <= 0) {
                AlertSeverity.CRITICAL_OUT_OF_STOCK
            } else {
                AlertSeverity.WARNING_LOW_STOCK
            }
            val message = if (product.currentStock <= 0) {
                "Out of stock! 0 ${product.unit} remaining. Customers cannot purchase."
            } else {
                "Low stock warning: ${product.currentStock} ${product.unit} left (Threshold is ${product.reorderThreshold})."
            }
            val suggestedQty = (product.idealStock - product.currentStock).coerceAtLeast(product.reorderThreshold)
            StockAlert(
                product = product,
                severity = severity,
                message = message,
                suggestedReorderQty = suggestedQty
            )
        }.sortedWith(compareBy({ it.severity }, { it.product.currentStock }))
    }

    val recentMovements: Flow<List<StockMovement>> = movementDao.getAllMovements()

    fun searchProducts(query: String): Flow<List<Product>> {
        return if (query.isBlank()) {
            productDao.getAllProducts()
        } else {
            productDao.searchProducts(query.trim())
        }
    }

    fun getProductByBarcode(barcode: String): Flow<Product?> = productDao.getProductByBarcode(barcode.trim())

    suspend fun findProductByBarcodeSync(barcode: String): Product? = productDao.findProductByBarcodeSync(barcode.trim())

    suspend fun getProductByIdSync(id: Long): Product? = productDao.getProductByIdSync(id)

    suspend fun insertProduct(product: Product): Long {
        val id = productDao.insertProduct(product)
        if (product.currentStock > 0) {
            movementDao.insertMovement(
                StockMovement(
                    productId = id,
                    productName = product.name,
                    barcode = product.barcode,
                    type = MovementType.STOCK_IN,
                    quantityDelta = product.currentStock,
                    previousStock = 0,
                    newStock = product.currentStock,
                    reason = "New product registration",
                    unitPrice = product.costPrice,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        return id
    }

    suspend fun updateProduct(product: Product) {
        val existing = productDao.getProductByIdSync(product.id)
        productDao.updateProduct(product)
        if (existing != null && existing.currentStock != product.currentStock) {
            val delta = product.currentStock - existing.currentStock
            movementDao.insertMovement(
                StockMovement(
                    productId = product.id,
                    productName = product.name,
                    barcode = product.barcode,
                    type = if (delta > 0) MovementType.STOCK_IN else MovementType.ADJUSTMENT,
                    quantityDelta = delta,
                    previousStock = existing.currentStock,
                    newStock = product.currentStock,
                    reason = "Product details edit & stock adjustment",
                    unitPrice = product.costPrice,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun adjustStock(
        productId: Long,
        quantityDelta: Int,
        type: MovementType,
        reason: String,
        unitPrice: Double = 0.0
    ): Boolean {
        val product = productDao.getProductByIdSync(productId) ?: return false
        val newStock = (product.currentStock + quantityDelta).coerceAtLeast(0)
        productDao.updateStock(productId, newStock, System.currentTimeMillis())

        movementDao.insertMovement(
            StockMovement(
                productId = productId,
                productName = product.name,
                barcode = product.barcode,
                type = type,
                quantityDelta = quantityDelta,
                previousStock = product.currentStock,
                newStock = newStock,
                reason = reason.ifBlank { type.displayName },
                unitPrice = if (unitPrice > 0.0) unitPrice else product.sellingPrice,
                timestamp = System.currentTimeMillis()
            )
        )
        return true
    }

    suspend fun restockProduct(productId: Long, quantityToAdd: Int, note: String = ""): Boolean {
        val product = productDao.getProductByIdSync(productId) ?: return false
        return adjustStock(
            productId = productId,
            quantityDelta = quantityToAdd,
            type = MovementType.STOCK_IN,
            reason = if (note.isNotBlank()) note else "Restock from supplier (${product.supplier})",
            unitPrice = product.costPrice
        )
    }

    suspend fun processSale(items: List<Pair<Product, Int>>, discountPercent: Double = 0.0): Boolean {
        for ((product, qty) in items) {
            val current = productDao.getProductByIdSync(product.id) ?: continue
            val newStock = (current.currentStock - qty).coerceAtLeast(0)
            productDao.updateStock(product.id, newStock, System.currentTimeMillis())

            val effectivePrice = product.sellingPrice * (1.0 - (discountPercent / 100.0))
            movementDao.insertMovement(
                StockMovement(
                    productId = product.id,
                    productName = product.name,
                    barcode = product.barcode,
                    type = MovementType.SALE,
                    quantityDelta = -qty,
                    previousStock = current.currentStock,
                    newStock = newStock,
                    reason = "POS Retail Checkout (${qty} ${product.unit})",
                    unitPrice = effectivePrice,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        return true
    }

    suspend fun deleteProduct(productId: Long) {
        productDao.deleteProductById(productId)
    }

    suspend fun resetToSampleData() {
        productDao.clearAllProducts()
        movementDao.clearAllMovements()
        com.example.data.db.InventoryDatabase.INITIAL_PRODUCTS.forEach { product ->
            val id = productDao.insertProduct(product)
            movementDao.insertMovement(
                StockMovement(
                    productId = id,
                    productName = product.name,
                    barcode = product.barcode,
                    type = MovementType.STOCK_IN,
                    quantityDelta = product.currentStock,
                    previousStock = 0,
                    newStock = product.currentStock,
                    reason = "Sample inventory initialization",
                    unitPrice = product.costPrice,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }
}
