package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.InventoryDatabase
import com.example.data.model.MovementType
import com.example.data.model.Product
import com.example.data.model.StockAlert
import com.example.data.model.StockMovement
import com.example.data.model.StockStatus
import com.example.data.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class StockFilter {
    ALL,
    ALERTS_ONLY,
    OUT_OF_STOCK,
    LOW_STOCK,
    IN_STOCK
}

enum class SortOption(val title: String) {
    STOCK_LOW_TO_HIGH("Stock: Low → High"),
    STOCK_HIGH_TO_LOW("Stock: High → Low"),
    NAME_ASC("Name: A → Z"),
    PRICE_HIGH_TO_LOW("Price: High → Low"),
    PRICE_LOW_TO_HIGH("Price: Low → High"),
    MARGIN_HIGH_TO_LOW("Profit Margin: High → Low")
}

data class InventoryStats(
    val totalProducts: Int = 0,
    val totalUnitsInStock: Int = 0,
    val totalRetailValue: Double = 0.0,
    val totalCostValue: Double = 0.0,
    val totalOutCount: Int = 0,
    val totalLowCount: Int = 0,
    val averageMargin: Double = 0.0
)

data class CartItem(
    val product: Product,
    val quantity: Int
)

class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: InventoryRepository

    init {
        val database = InventoryDatabase.getDatabase(application, viewModelScope)
        repository = InventoryRepository(database.productDao(), database.stockMovementDao())
    }

    val allProducts: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stockAlerts: StateFlow<List<StockAlert>> = repository.stockAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentMovements: StateFlow<List<StockMovement>> = repository.recentMovements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search and filter state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>("All")
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _selectedStockFilter = MutableStateFlow(StockFilter.ALL)
    val selectedStockFilter: StateFlow<StockFilter> = _selectedStockFilter.asStateFlow()

    private val _selectedSort = MutableStateFlow(SortOption.STOCK_LOW_TO_HIGH)
    val selectedSort: StateFlow<SortOption> = _selectedSort.asStateFlow()

    // Filtered Products
    val filteredProducts: StateFlow<List<Product>> = combine(
        allProducts,
        searchQuery,
        selectedCategory,
        selectedStockFilter,
        selectedSort
    ) { products, query, category, filter, sort ->
        var list = products

        // Search Filter
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) ||
                it.barcode.lowercase().contains(q) ||
                it.sku.lowercase().contains(q) ||
                it.category.lowercase().contains(q) ||
                it.supplier.lowercase().contains(q)
            }
        }

        // Category Filter
        if (category != null && category != "All") {
            list = list.filter { it.category.equals(category, ignoreCase = true) }
        }

        // Stock Status Filter
        list = when (filter) {
            StockFilter.ALL -> list
            StockFilter.ALERTS_ONLY -> list.filter { it.currentStock <= it.reorderThreshold }
            StockFilter.OUT_OF_STOCK -> list.filter { it.currentStock <= 0 }
            StockFilter.LOW_STOCK -> list.filter { it.currentStock > 0 && it.currentStock <= it.reorderThreshold }
            StockFilter.IN_STOCK -> list.filter { it.currentStock > it.reorderThreshold }
        }

        // Sorting
        when (sort) {
            SortOption.STOCK_LOW_TO_HIGH -> list.sortedBy { it.currentStock }
            SortOption.STOCK_HIGH_TO_LOW -> list.sortedByDescending { it.currentStock }
            SortOption.NAME_ASC -> list.sortedBy { it.name.lowercase() }
            SortOption.PRICE_HIGH_TO_LOW -> list.sortedByDescending { it.sellingPrice }
            SortOption.PRICE_LOW_TO_HIGH -> list.sortedBy { it.sellingPrice }
            SortOption.MARGIN_HIGH_TO_LOW -> list.sortedByDescending { it.profitMargin }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Summary Statistics
    val stats: StateFlow<InventoryStats> = allProducts.combine(stockAlerts) { products, alerts ->
        val totalProducts = products.size
        val totalUnits = products.sumOf { it.currentStock.coerceAtLeast(0) }
        val totalRetail = products.sumOf { it.totalRetailValue }
        val totalCost = products.sumOf { it.totalCostValue }
        val outCount = products.count { it.currentStock <= 0 }
        val lowCount = products.count { it.currentStock > 0 && it.currentStock <= it.reorderThreshold }
        val avgMargin = if (totalRetail > 0) ((totalRetail - totalCost) / totalRetail) * 100.0 else 0.0

        InventoryStats(
            totalProducts = totalProducts,
            totalUnitsInStock = totalUnits,
            totalRetailValue = totalRetail,
            totalCostValue = totalCost,
            totalOutCount = outCount,
            totalLowCount = lowCount,
            averageMargin = avgMargin
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InventoryStats())

    // Quick POS Cart State
    private val _cart = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val cart: StateFlow<Map<Long, Int>> = _cart.asStateFlow()

    private val _cartDiscountPercent = MutableStateFlow(0.0)
    val cartDiscountPercent: StateFlow<Double> = _cartDiscountPercent.asStateFlow()

    // Scanned Barcode Result Sheet State
    private val _scannedBarcode = MutableStateFlow<String?>(null)
    val scannedBarcode: StateFlow<String?> = _scannedBarcode.asStateFlow()

    private val _scannedProduct = MutableStateFlow<Product?>(null)
    val scannedProduct: StateFlow<Product?> = _scannedProduct.asStateFlow()

    private val _showScannerDialog = MutableStateFlow(false)
    val showScannerDialog: StateFlow<Boolean> = _showScannerDialog.asStateFlow()

    // Toast/Feedback banner message
    private val _userFeedbackMessage = MutableStateFlow<String?>(null)
    val userFeedbackMessage: StateFlow<String?> = _userFeedbackMessage.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun selectStockFilter(filter: StockFilter) {
        _selectedStockFilter.value = filter
    }

    fun selectSort(sort: SortOption) {
        _selectedSort.value = sort
    }

    fun openScanner() {
        _showScannerDialog.value = true
    }

    fun closeScanner() {
        _showScannerDialog.value = false
    }

    fun onBarcodeScanned(barcode: String) {
        val trimmed = barcode.trim()
        if (trimmed.isBlank()) return
        _scannedBarcode.value = trimmed
        _showScannerDialog.value = false

        viewModelScope.launch {
            val product = repository.findProductByBarcodeSync(trimmed)
            _scannedProduct.value = product
            if (product != null) {
                _userFeedbackMessage.value = "Found: ${product.name} (Stock: ${product.currentStock} ${product.unit})"
            } else {
                _userFeedbackMessage.value = "Barcode $trimmed not found in catalog. You can register it now."
            }
        }
    }

    fun clearScannedResult() {
        _scannedBarcode.value = null
        _scannedProduct.value = null
    }

    fun dismissFeedback() {
        _userFeedbackMessage.value = null
    }

    // Quick Stock Adjustments
    fun quickStockAdjust(productId: Long, delta: Int, reason: String = "") {
        viewModelScope.launch {
            val type = if (delta > 0) MovementType.STOCK_IN else MovementType.ADJUSTMENT
            val success = repository.adjustStock(productId, delta, type, reason)
            if (success) {
                _userFeedbackMessage.value = "Stock updated (${if (delta > 0) "+$delta" else "$delta"})"
                // update scanned product if it's open
                val updated = repository.getProductByIdSync(productId)
                if (_scannedProduct.value?.id == productId) {
                    _scannedProduct.value = updated
                }
            }
        }
    }

    fun restockProduct(productId: Long, quantity: Int, note: String = "") {
        viewModelScope.launch {
            val success = repository.restockProduct(productId, quantity, note)
            if (success) {
                _userFeedbackMessage.value = "Restocked +$quantity units successfully"
                val updated = repository.getProductByIdSync(productId)
                if (_scannedProduct.value?.id == productId) {
                    _scannedProduct.value = updated
                }
            }
        }
    }

    fun saveProduct(product: Product, isNew: Boolean) {
        viewModelScope.launch {
            if (isNew) {
                val newId = repository.insertProduct(product)
                _userFeedbackMessage.value = "Registered '${product.name}' with SKU ${product.sku}"
            } else {
                repository.updateProduct(product)
                _userFeedbackMessage.value = "Updated '${product.name}'"
            }
            clearScannedResult()
        }
    }

    fun deleteProduct(productId: Long, productName: String) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
            _userFeedbackMessage.value = "Deleted '$productName'"
            clearScannedResult()
        }
    }

    // Cart / POS Actions
    fun addToCart(product: Product, qty: Int = 1) {
        val currentQty = _cart.value[product.id] ?: 0
        if (currentQty + qty > product.currentStock) {
            _userFeedbackMessage.value = "Only ${product.currentStock} ${product.unit} available in stock!"
            return
        }
        val updated = _cart.value.toMutableMap()
        updated[product.id] = currentQty + qty
        _cart.value = updated
        _userFeedbackMessage.value = "Added 1x ${product.name} to POS Cart"
    }

    fun removeFromCart(productId: Long) {
        val updated = _cart.value.toMutableMap()
        val currentQty = updated[productId] ?: 0
        if (currentQty > 1) {
            updated[productId] = currentQty - 1
        } else {
            updated.remove(productId)
        }
        _cart.value = updated
    }

    fun removeAllFromCart(productId: Long) {
        val updated = _cart.value.toMutableMap()
        updated.remove(productId)
        _cart.value = updated
    }

    fun clearCart() {
        _cart.value = emptyMap()
        _cartDiscountPercent.value = 0.0
    }

    fun setCartDiscount(discount: Double) {
        _cartDiscountPercent.value = discount.coerceIn(0.0, 100.0)
    }

    fun checkoutCart() {
        val cartMap = _cart.value
        if (cartMap.isEmpty()) return

        viewModelScope.launch {
            val allList = allProducts.value
            val cartList = cartMap.mapNotNull { (prodId, qty) ->
                val prod = allList.find { it.id == prodId } ?: repository.getProductByIdSync(prodId)
                if (prod != null) prod to qty else null
            }

            if (cartList.isNotEmpty()) {
                repository.processSale(cartList, _cartDiscountPercent.value)
                val totalSoldUnits = cartList.sumOf { it.second }
                _userFeedbackMessage.value = "Sale complete! $totalSoldUnits items sold. Stock updated."
                clearCart()
            }
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.resetToSampleData()
            clearCart()
            clearScannedResult()
            _userFeedbackMessage.value = "Sample retail inventory & alert logs reloaded."
        }
    }
}
