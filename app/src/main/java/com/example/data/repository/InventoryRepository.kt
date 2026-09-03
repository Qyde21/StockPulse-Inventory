package com.example.data.repository

import com.example.data.db.InventoryItemDao
import com.example.data.db.ProductDao
import com.example.data.db.StockMovementDao
import com.example.data.model.AlertSeverity
import com.example.data.model.InventoryItem
import com.example.data.model.MovementType
import com.example.data.model.Product
import com.example.data.model.StockAlert
import com.example.data.model.StockMovement
import com.example.data.model.toInventoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class InventoryRepository(
    private val productDao: ProductDao,
    private val movementDao: StockMovementDao,
    private val inventoryItemDao: InventoryItemDao? = null
) {
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()

    /**
     * Reactive stream of inventory items for observing persistence across sessions.
     */
    val inventoryItems: Flow<List<Product>> = allProducts

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
        inventoryItemDao?.insertItem(product.toInventoryItem().copy(id = id))
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
        inventoryItemDao?.updateItem(product.toInventoryItem())
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
        unitPrice: Double = 0.0,
        timestamp: Long = System.currentTimeMillis()
    ): Boolean {
        val product = productDao.getProductByIdSync(productId) ?: return false
        val newStock = (product.currentStock + quantityDelta).coerceAtLeast(0)
        productDao.updateStock(productId, newStock, timestamp)

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
                timestamp = timestamp
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
        inventoryItemDao?.deleteItemById(productId)
    }

    suspend fun insert(product: Product): Long = insertProduct(product)

    suspend fun update(product: Product) = updateProduct(product)

    suspend fun delete(product: Product) = deleteProduct(product.id)

    suspend fun deleteById(id: Long) = deleteProduct(id)

    suspend fun getItemById(id: Long): Product? = getProductByIdSync(id)

    suspend fun getItemByBarcode(barcode: String): Product? = findProductByBarcodeSync(barcode)

    /**
     * Inserts an inventory item with name, quantity, and barcode,
     * persisting to Room database.
     */
    suspend fun insertItem(name: String, quantity: Int, barcode: String): Long {
        val product = Product(
            name = name,
            currentStock = quantity,
            barcode = barcode
        )
        return insertProduct(product)
    }

    /**
     * Saves or updates an inventory item (name, quantity, barcode).
     */
    suspend fun saveInventoryItem(name: String, quantity: Int, barcode: String): Long {
        val existing = if (barcode.isNotBlank()) findProductByBarcodeSync(barcode) else null
        return if (existing != null) {
            val updated = existing.copy(
                name = name,
                currentStock = quantity,
                barcode = barcode,
                lastUpdated = System.currentTimeMillis()
            )
            updateProduct(updated)
            existing.id
        } else {
            insertItem(name, quantity, barcode)
        }
    }

    /**
     * Updates inventory quantity for an item by ID.
     */
    suspend fun updateInventoryItemQuantity(id: Long, newQuantity: Int) {
        val current = productDao.getProductByIdSync(id) ?: return
        val delta = newQuantity - current.currentStock
        adjustStock(
            productId = id,
            quantityDelta = delta,
            type = MovementType.ADJUSTMENT,
            reason = "Inventory quantity adjustment"
        )
    }

    /**
     * Records a stock change in the database history, recording adjustment type ('restock', 'sale', etc.),
     * timestamp, and quantity changes.
     */
    suspend fun recordStockChange(
        productId: Long,
        quantityDelta: Int,
        adjustmentType: String,
        reason: String = "",
        unitPrice: Double = 0.0,
        timestamp: Long = System.currentTimeMillis()
    ): Boolean {
        val type = MovementType.fromString(adjustmentType)
        return adjustStock(
            productId = productId,
            quantityDelta = quantityDelta,
            type = type,
            reason = reason,
            unitPrice = unitPrice,
            timestamp = timestamp
        )
    }

    /**
     * Records a stock change directly using MovementType.
     */
    suspend fun recordStockChange(
        productId: Long,
        quantityDelta: Int,
        type: MovementType,
        reason: String = "",
        unitPrice: Double = 0.0,
        timestamp: Long = System.currentTimeMillis()
    ): Boolean {
        return adjustStock(
            productId = productId,
            quantityDelta = quantityDelta,
            type = type,
            reason = reason,
            unitPrice = unitPrice,
            timestamp = timestamp
        )
    }

    /**
     * Returns history of stock changes for a specific product.
     */
    fun getStockHistoryForProduct(productId: Long): Flow<List<StockMovement>> =
        movementDao.getMovementsForProduct(productId)

    /**
     * Returns stock movements filtered by adjustment type (e.g. RESTOCK, SALE).
     */
    fun getStockHistoryByType(type: MovementType): Flow<List<StockMovement>> =
        movementDao.getMovementsByType(type)

    /**
     * Returns stock movements within a timestamp range.
     */
    fun getStockHistoryBetween(startTime: Long, endTime: Long): Flow<List<StockMovement>> =
        movementDao.getMovementsBetweenTimestamps(startTime, endTime)

    suspend fun resetToSampleData() {
        productDao.clearAllProducts()
        movementDao.clearAllMovements()
        inventoryItemDao?.clearAll()
        com.example.data.db.InventoryDatabase.INITIAL_PRODUCTS.forEach { product ->
            val id = productDao.insertProduct(product)
            inventoryItemDao?.insertItem(product.toInventoryItem().copy(id = id))
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
