package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Product
import com.example.data.preferences.AppThemeMode
import com.example.ui.components.AddEditProductSheet
import com.example.ui.components.ProductDetailModal
import com.example.ui.components.QuickAdjustStockDialog
import com.example.ui.components.RestockDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.scanner.BarcodeScannerScreen
import com.example.ui.scanner.ScannedProductActionDialog
import com.example.ui.screens.AuditLogScreen
import com.example.ui.screens.CatalogScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.PosCheckoutScreen
import com.example.ui.viewmodel.InventoryViewModel
import com.example.ui.viewmodel.StockFilter
import kotlinx.coroutines.launch

enum class AppDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD("Alerts", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    CATALOG("Catalog", Icons.Filled.Inventory, Icons.Outlined.Inventory),
    SCANNER("Scanner", Icons.Filled.QrCodeScanner, Icons.Outlined.QrCodeScanner),
    POS("POS Sale", Icons.Filled.PointOfSale, Icons.Outlined.PointOfSale),
    AUDIT_LOGS("History", Icons.Filled.History, Icons.Outlined.History)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockPulseApp(
    viewModel: InventoryViewModel = viewModel(),
    isDarkTheme: Boolean = false
) {
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val filteredProducts by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val stockAlerts by viewModel.stockAlerts.collectAsStateWithLifecycle()
    val recentMovements by viewModel.recentMovements.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val cart by viewModel.cart.collectAsStateWithLifecycle()
    val cartDiscount by viewModel.cartDiscountPercent.collectAsStateWithLifecycle()
    val currentThemeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedStockFilter by viewModel.selectedStockFilter.collectAsStateWithLifecycle()
    val selectedSort by viewModel.selectedSort.collectAsStateWithLifecycle()

    val scannedBarcode by viewModel.scannedBarcode.collectAsStateWithLifecycle()
    val scannedProduct by viewModel.scannedProduct.collectAsStateWithLifecycle()
    val showScannerDialog by viewModel.showScannerDialog.collectAsStateWithLifecycle()
    val userFeedbackMessage by viewModel.userFeedbackMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(userFeedbackMessage) {
        userFeedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissFeedback()
        }
    }

    var currentDestination by remember { mutableStateOf(AppDestination.DASHBOARD) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Dialog & Sheet State
    var selectedProductForDetail by remember { mutableStateOf<Product?>(null) }
    val productDetailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var productForAddEdit by remember { mutableStateOf<Product?>(null) }
    var initialBarcodeForAdd by remember { mutableStateOf<String?>(null) }
    var showAddEditSheet by remember { mutableStateOf(false) }
    val addEditSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var productForAdjustDialog by remember { mutableStateOf<Product?>(null) }
    var productForRestockDialog by remember { mutableStateOf<Product?>(null) }
    var suggestedRestockQty by remember { mutableStateOf(20) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .testTag("main_navigation_bar")
            ) {
                AppDestination.values().forEach { destination ->
                    val isSelected = currentDestination == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = destination },
                        icon = {
                            if (destination == AppDestination.DASHBOARD && stockAlerts.isNotEmpty()) {
                                BadgedBox(
                                    badge = {
                                        Badge {
                                            Text("${stockAlerts.size}")
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                        contentDescription = destination.title
                                    )
                                }
                            } else if (destination == AppDestination.POS && cart.isNotEmpty()) {
                                BadgedBox(
                                    badge = {
                                        Badge {
                                            Text("${cart.values.sum()}")
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                        contentDescription = destination.title
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                    contentDescription = destination.title
                                )
                            }
                        },
                        label = { Text(destination.title) },
                        modifier = Modifier.testTag("nav_tab_${destination.name.lowercase()}")
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentDestination) {
                AppDestination.DASHBOARD -> {
                    DashboardScreen(
                        stats = stats,
                        stockAlerts = stockAlerts,
                        products = allProducts,
                        isDarkTheme = isDarkTheme,
                        onToggleQuickTheme = { viewModel.toggleDarkMode(isDarkTheme) },
                        onOpenSettings = { showSettingsDialog = true },
                        onOpenScanner = { viewModel.openScanner() },
                        onOpenAddProduct = {
                            productForAddEdit = null
                            initialBarcodeForAdd = null
                            showAddEditSheet = true
                        },
                        onOpenPos = { currentDestination = AppDestination.POS },
                        onViewCatalog = { currentDestination = AppDestination.CATALOG },
                        onRestockAlertItem = { prod, qty ->
                            productForRestockDialog = prod
                            suggestedRestockQty = qty
                        },
                        onSelectProduct = { prod -> selectedProductForDetail = prod },
                        onResetDemoData = { viewModel.resetDemoData() }
                    )
                }

                AppDestination.CATALOG -> {
                    CatalogScreen(
                        products = filteredProducts,
                        searchQuery = searchQuery,
                        selectedCategory = selectedCategory,
                        selectedFilter = selectedStockFilter,
                        selectedSort = selectedSort,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onCategoryChange = { viewModel.selectCategory(it) },
                        onFilterChange = { viewModel.selectStockFilter(it) },
                        onSortChange = { viewModel.selectSort(it) },
                        onOpenScanner = { viewModel.openScanner() },
                        onOpenAddProduct = {
                            productForAddEdit = null
                            initialBarcodeForAdd = null
                            showAddEditSheet = true
                        },
                        onSelectProduct = { prod -> selectedProductForDetail = prod },
                        onQuickAdjust = { prod, delta -> viewModel.quickStockAdjust(prod.id, delta) },
                        onAddToCart = { prod -> viewModel.addToCart(prod) }
                    )
                }

                AppDestination.SCANNER -> {
                    BarcodeScannerScreen(
                        onBarcodeDetected = { barcode ->
                            viewModel.onBarcodeScanned(barcode)
                        },
                        onClose = {
                            currentDestination = AppDestination.DASHBOARD
                        }
                    )
                }

                AppDestination.POS -> {
                    PosCheckoutScreen(
                        cart = cart,
                        allProducts = allProducts,
                        discountPercent = cartDiscount,
                        onAddToCart = { prod -> viewModel.addToCart(prod) },
                        onRemoveSingleFromCart = { id -> viewModel.removeFromCart(id) },
                        onRemoveItemCompletely = { id -> viewModel.removeAllFromCart(id) },
                        onClearCart = { viewModel.clearCart() },
                        onSetDiscount = { disc -> viewModel.setCartDiscount(disc) },
                        onCheckout = { viewModel.checkoutCart() },
                        onOpenScanner = { viewModel.openScanner() }
                    )
                }

                AppDestination.AUDIT_LOGS -> {
                    AuditLogScreen(
                        movements = recentMovements
                    )
                }
            }
        }
    }

    // Camera Scanner Full Screen Modal Dialog
    if (showScannerDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { viewModel.closeScanner() },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            BarcodeScannerScreen(
                onBarcodeDetected = { code ->
                    viewModel.onBarcodeScanned(code)
                },
                onClose = { viewModel.closeScanner() }
            )
        }
    }

    // Scanned Barcode Result Sheet Dialog
    if (scannedBarcode != null) {
        ScannedProductActionDialog(
            barcode = scannedBarcode!!,
            product = scannedProduct,
            onDismiss = { viewModel.clearScannedResult() },
            onQuickAdjust = { prod, delta ->
                viewModel.quickStockAdjust(prod.id, delta)
            },
            onRestock = { prod ->
                viewModel.clearScannedResult()
                productForRestockDialog = prod
                suggestedRestockQty = prod.stockDeficit.coerceAtLeast(10)
            },
            onAddToCart = { prod ->
                viewModel.addToCart(prod)
                viewModel.clearScannedResult()
            },
            onEditProduct = { prod ->
                viewModel.clearScannedResult()
                productForAddEdit = prod
                showAddEditSheet = true
            },
            onRegisterNew = { barcode ->
                viewModel.clearScannedResult()
                productForAddEdit = null
                initialBarcodeForAdd = barcode
                showAddEditSheet = true
            }
        )
    }

    // Product Detail Bottom Sheet
    if (selectedProductForDetail != null) {
        ProductDetailModal(
            product = selectedProductForDetail!!,
            sheetState = productDetailSheetState,
            onDismiss = { selectedProductForDetail = null },
            onEdit = {
                val p = selectedProductForDetail
                selectedProductForDetail = null
                productForAddEdit = p
                showAddEditSheet = true
            },
            onDelete = {
                selectedProductForDetail?.let { p ->
                    viewModel.deleteProduct(p.id, p.name)
                }
                selectedProductForDetail = null
            },
            onQuickAdjust = { delta ->
                selectedProductForDetail?.let { p ->
                    viewModel.quickStockAdjust(p.id, delta)
                    // Keep detail updated
                    selectedProductForDetail = p.copy(currentStock = (p.currentStock + delta).coerceAtLeast(0))
                }
            },
            onOpenRestock = {
                val p = selectedProductForDetail
                selectedProductForDetail = null
                if (p != null) {
                    productForRestockDialog = p
                    suggestedRestockQty = p.stockDeficit.coerceAtLeast(15)
                }
            },
            onAddToCart = {
                selectedProductForDetail?.let { p ->
                    viewModel.addToCart(p)
                }
                selectedProductForDetail = null
            }
        )
    }

    // Add / Edit Product Sheet
    if (showAddEditSheet) {
        AddEditProductSheet(
            product = productForAddEdit,
            initialBarcode = initialBarcodeForAdd,
            sheetState = addEditSheetState,
            onDismiss = { showAddEditSheet = false },
            onOpenScanner = { viewModel.openScanner() },
            onSave = { prod, isNew ->
                viewModel.saveProduct(prod, isNew)
                showAddEditSheet = false
            }
        )
    }

    // Quick Adjust Dialog
    if (productForAdjustDialog != null) {
        QuickAdjustStockDialog(
            product = productForAdjustDialog!!,
            onDismiss = { productForAdjustDialog = null },
            onConfirm = { delta, reason ->
                viewModel.quickStockAdjust(productForAdjustDialog!!.id, delta, reason)
                productForAdjustDialog = null
            }
        )
    }

    // Restock Dialog
    if (productForRestockDialog != null) {
        RestockDialog(
            product = productForRestockDialog!!,
            suggestedQty = suggestedRestockQty,
            onDismiss = { productForRestockDialog = null },
            onConfirm = { qty, note ->
                viewModel.restockProduct(productForRestockDialog!!.id, qty, note)
                productForRestockDialog = null
            }
        )
    }

    // Application Settings Dialog
    if (showSettingsDialog) {
        SettingsDialog(
            currentThemeMode = currentThemeMode,
            isDarkTheme = isDarkTheme,
            onSelectThemeMode = { mode -> viewModel.setThemeMode(mode) },
            onToggleDarkMode = { isDark -> viewModel.toggleDarkMode(!isDark) },
            onResetDemoData = { viewModel.resetDemoData() },
            onDismiss = { showSettingsDialog = false }
        )
    }
}
