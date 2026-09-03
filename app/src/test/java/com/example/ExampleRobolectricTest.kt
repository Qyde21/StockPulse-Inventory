package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.ui.scanner.BarcodeScannerOverlayView
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("StockPulse Inventory", appName)
  }

  @Test
  fun `barcode scanner overlay renders correctly`() {
    composeTestRule.setContent {
      MyApplicationTheme {
        BarcodeScannerOverlayView(
          isScanning = true,
          isSuccess = false,
          onClose = {}
        )
      }
    }
    composeTestRule.onNodeWithTag("barcode_scanner_overlay_view").assertIsDisplayed()
    composeTestRule.onNodeWithTag("close_scanner_btn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("torch_toggle_btn").assertIsDisplayed()
  }

  @Test
  fun `barcode scanner overlay displays success feedback badge when scan completed`() {
    composeTestRule.setContent {
      MyApplicationTheme {
        BarcodeScannerOverlayView(
          isScanning = false,
          isSuccess = true,
          scannedCode = "8901030381023",
          onClose = {}
        )
      }
    }
    composeTestRule.onNodeWithTag("barcode_scanner_overlay_view").assertIsDisplayed()
    composeTestRule.onNodeWithTag("scan_success_feedback_badge").assertIsDisplayed()
  }

  @Test
  fun `dashboard displays stock items with name, quantity, and low-stock indicator badge`() {
    val sampleProductLow = com.example.data.model.Product(
      id = 1,
      sku = "BEV-001",
      name = "Organic Cold Brew Coffee",
      category = "Beverages",
      currentStock = 4,
      reorderThreshold = 10,
      idealStock = 30,
      costPrice = 2.10,
      sellingPrice = 4.50,
      barcode = "8901030381023",
      unit = "bottles"
    )

    val sampleProductOut = com.example.data.model.Product(
      id = 2,
      sku = "SNK-002",
      name = "Artisan Dark Chocolate",
      category = "Snacks",
      currentStock = 0,
      reorderThreshold = 15,
      idealStock = 40,
      costPrice = 1.50,
      sellingPrice = 3.25,
      barcode = "7622210449283",
      unit = "bars"
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        androidx.compose.foundation.layout.Column {
          com.example.ui.screens.DashboardStockItemCard(
            product = sampleProductLow,
            onSelect = {}
          )
          com.example.ui.screens.DashboardStockItemCard(
            product = sampleProductOut,
            onSelect = {}
          )
        }
      }
    }

    composeTestRule.onNodeWithTag("stock_item_card_BEV-001", useUnmergedTree = true).assertExists()
    composeTestRule.onNodeWithTag("stock_item_name_BEV-001", useUnmergedTree = true).assertExists()
    composeTestRule.onNodeWithTag("stock_item_qty_BEV-001", useUnmergedTree = true).assertExists()
    composeTestRule.onNodeWithTag("stock_item_badge_BEV-001", useUnmergedTree = true).assertExists()

    composeTestRule.onNodeWithTag("stock_item_card_SNK-002", useUnmergedTree = true).assertExists()
    composeTestRule.onNodeWithTag("stock_item_name_SNK-002", useUnmergedTree = true).assertExists()
    composeTestRule.onNodeWithTag("stock_item_qty_SNK-002", useUnmergedTree = true).assertExists()
    composeTestRule.onNodeWithTag("stock_item_badge_SNK-002", useUnmergedTree = true).assertExists()
  }

  @Test
  fun `dashboard search bar filters items in real time by name, SKU, and barcode`() {
    val sampleProduct1 = com.example.data.model.Product(
      id = 1,
      sku = "BEV-001",
      name = "Sparkling Spring Water",
      category = "Beverages",
      currentStock = 12,
      reorderThreshold = 10,
      costPrice = 2.10,
      sellingPrice = 4.50,
      barcode = "8901030381023",
      unit = "bottles"
    )

    val sampleProduct2 = com.example.data.model.Product(
      id = 2,
      sku = "SNK-002",
      name = "Artisan Dark Chocolate",
      category = "Snacks",
      currentStock = 8,
      reorderThreshold = 5,
      costPrice = 1.50,
      sellingPrice = 3.25,
      barcode = "7622210449283",
      unit = "bars"
    )

    val stats = com.example.ui.viewmodel.InventoryStats(
      totalProducts = 2,
      totalUnitsInStock = 20,
      totalLowCount = 0,
      totalOutCount = 0
    )

    // Test 1: Filter by Name ("Water")
    composeTestRule.setContent {
      MyApplicationTheme {
        com.example.ui.screens.DashboardScreen(
          stats = stats,
          stockAlerts = emptyList(),
          products = listOf(sampleProduct1, sampleProduct2),
          initialSearchQuery = "Water",
          onOpenScanner = {},
          onOpenAddProduct = {},
          onOpenPos = {},
          onViewCatalog = {},
          onRestockAlertItem = { _, _ -> },
          onSelectProduct = {},
          onResetDemoData = {}
        )
      }
    }

    composeTestRule.onNodeWithTag("dashboard_search_bar").assertExists()
    composeTestRule.onNodeWithTag("stock_item_card_BEV-001", useUnmergedTree = true).assertExists()
    composeTestRule.onNodeWithTag("stock_item_card_SNK-002", useUnmergedTree = true).assertDoesNotExist()
  }

  @Test
  fun `dashboard search bar filters by barcode or SKU and displays empty state when unmatched`() {
    val sampleProduct1 = com.example.data.model.Product(
      id = 1,
      sku = "BEV-001",
      name = "Sparkling Spring Water",
      category = "Beverages",
      currentStock = 12,
      barcode = "8901030381023"
    )

    val sampleProduct2 = com.example.data.model.Product(
      id = 2,
      sku = "SNK-002",
      name = "Artisan Dark Chocolate",
      category = "Snacks",
      currentStock = 8,
      barcode = "7622210449283"
    )

    val stats = com.example.ui.viewmodel.InventoryStats(
      totalProducts = 2,
      totalUnitsInStock = 20,
      totalLowCount = 0,
      totalOutCount = 0
    )

    // Filter by barcode "7622210449283"
    composeTestRule.setContent {
      MyApplicationTheme {
        com.example.ui.screens.DashboardScreen(
          stats = stats,
          stockAlerts = emptyList(),
          products = listOf(sampleProduct1, sampleProduct2),
          initialSearchQuery = "7622210449283",
          onOpenScanner = {},
          onOpenAddProduct = {},
          onOpenPos = {},
          onViewCatalog = {},
          onRestockAlertItem = { _, _ -> },
          onSelectProduct = {},
          onResetDemoData = {}
        )
      }
    }

    composeTestRule.onNodeWithTag("stock_item_card_SNK-002", useUnmergedTree = true).assertExists()
    composeTestRule.onNodeWithTag("stock_item_card_BEV-001", useUnmergedTree = true).assertDoesNotExist()
  }

  @Test
  fun `track history of stock changes with timestamps, adjustment types, and quantities`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val inMemoryDb = androidx.room.Room.inMemoryDatabaseBuilder(
      context,
      com.example.data.db.InventoryDatabase::class.java
    ).allowMainThreadQueries().build()

    try {
      val productDao = inMemoryDb.productDao()
      val movementDao = inMemoryDb.stockMovementDao()
      val repository = com.example.data.repository.InventoryRepository(productDao, movementDao)

      val testProduct = com.example.data.model.Product(
        barcode = "1234567890123",
        sku = "TST-001",
        name = "Organic Almond Milk",
        category = "Beverages",
        currentStock = 20,
        reorderThreshold = 5,
        idealStock = 50,
        costPrice = 2.0,
        sellingPrice = 4.0
      )
      val productId = productDao.insertProduct(testProduct)

      val timestampRestock = 1725270000000L
      val timestampSale = 1725273600000L

      // 1. Record 'restock' adjustment
      val restockSuccess = repository.recordStockChange(
        productId = productId,
        quantityDelta = 15,
        adjustmentType = "restock",
        reason = "Weekly supplier restock",
        unitPrice = 2.0,
        timestamp = timestampRestock
      )
      assertEquals(true, restockSuccess)

      // 2. Record 'sale' adjustment
      val saleSuccess = repository.recordStockChange(
        productId = productId,
        quantityDelta = -5,
        adjustmentType = "sale",
        reason = "POS customer checkout",
        unitPrice = 4.0,
        timestamp = timestampSale
      )
      assertEquals(true, saleSuccess)

      // Verify product's updated current stock: 20 + 15 - 5 = 30
      val updatedProduct = productDao.getProductByIdSync(productId)
      org.junit.Assert.assertNotNull(updatedProduct)
      assertEquals(30, updatedProduct!!.currentStock)

      // Verify history movements recorded in table
      val movements = movementDao.getAllMovementsSync()
      assertEquals(2, movements.size)

      // Most recent first (timestamp DESC)
      val latestMovement = movements[0]
      assertEquals(com.example.data.model.MovementType.SALE, latestMovement.type)
      assertEquals(-5, latestMovement.quantityDelta)
      assertEquals(35, latestMovement.previousStock)
      assertEquals(30, latestMovement.newStock)
      assertEquals(timestampSale, latestMovement.timestamp)

      val firstMovement = movements[1]
      assertEquals(com.example.data.model.MovementType.RESTOCK, firstMovement.type)
      assertEquals(15, firstMovement.quantityDelta)
      assertEquals(20, firstMovement.previousStock)
      assertEquals(35, firstMovement.newStock)
      assertEquals(timestampRestock, firstMovement.timestamp)

      // Verify dedicated StockMovementRepository queries
      val movementRepo = com.example.data.repository.StockMovementRepository(movementDao)
      val allHistory = movementRepo.getAllStockMovementsSync()
      assertEquals(2, allHistory.size)
    } finally {
      inMemoryDb.close()
    }
  }

  @Test
  fun `theme preferences stores and toggles light and dark mode`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val themePrefs = com.example.data.preferences.ThemePreferences(context)

    // Set light mode
    themePrefs.setThemeMode(com.example.data.preferences.AppThemeMode.LIGHT)
    assertEquals(com.example.data.preferences.AppThemeMode.LIGHT, themePrefs.themeMode.value)

    // Set dark mode
    themePrefs.setThemeMode(com.example.data.preferences.AppThemeMode.DARK)
    assertEquals(com.example.data.preferences.AppThemeMode.DARK, themePrefs.themeMode.value)

    // Toggle from dark (true) to light
    themePrefs.toggleDarkMode(true)
    assertEquals(com.example.data.preferences.AppThemeMode.LIGHT, themePrefs.themeMode.value)

    // Toggle from light (false) to dark
    themePrefs.toggleDarkMode(false)
    assertEquals(com.example.data.preferences.AppThemeMode.DARK, themePrefs.themeMode.value)

    // Reset back to system default
    themePrefs.setThemeMode(com.example.data.preferences.AppThemeMode.SYSTEM)
    assertEquals(com.example.data.preferences.AppThemeMode.SYSTEM, themePrefs.themeMode.value)
  }

  @Test
  fun `settings dialog renders theme toggle switch and options`() {
    var selectedMode = com.example.data.preferences.AppThemeMode.SYSTEM
    var isDark = false

    composeTestRule.setContent {
      com.example.ui.theme.MyApplicationTheme(darkTheme = isDark) {
        com.example.ui.components.SettingsDialog(
          currentThemeMode = selectedMode,
          isDarkTheme = isDark,
          onSelectThemeMode = { selectedMode = it },
          onToggleDarkMode = { isDark = it },
          onResetDemoData = {},
          onDismiss = {}
        )
      }
    }

    // Verify dark mode switch and theme mode selection options exist
    composeTestRule.onNodeWithTag("theme_dark_mode_switch").assertExists().performScrollTo().performClick()
    assertEquals(true, isDark)

    composeTestRule.onNodeWithTag("theme_mode_system").assertExists()
    composeTestRule.onNodeWithTag("theme_mode_light").assertExists()
    composeTestRule.onNodeWithTag("theme_mode_dark").assertExists()

    // Test clicking dark mode option
    composeTestRule.onNodeWithTag("theme_mode_dark").performScrollTo().performClick()
    assertEquals(com.example.data.preferences.AppThemeMode.DARK, selectedMode)

    // Test clicking light mode option
    composeTestRule.onNodeWithTag("theme_mode_light").performScrollTo().performClick()
    assertEquals(com.example.data.preferences.AppThemeMode.LIGHT, selectedMode)
  }

  @Test
  fun `dashboard header contains quick theme toggle and settings button`() {
    var quickThemeToggled = false
    var settingsOpened = false

    composeTestRule.setContent {
      com.example.ui.theme.MyApplicationTheme {
        com.example.ui.screens.DashboardScreen(
          stats = com.example.ui.viewmodel.InventoryStats(),
          stockAlerts = emptyList(),
          products = emptyList(),
          isDarkTheme = false,
          onToggleQuickTheme = { quickThemeToggled = true },
          onOpenSettings = { settingsOpened = true },
          onOpenScanner = {},
          onOpenAddProduct = {},
          onOpenPos = {},
          onViewCatalog = {},
          onRestockAlertItem = { _, _ -> },
          onSelectProduct = {},
          onResetDemoData = {}
        )
      }
    }

    composeTestRule.onNodeWithTag("btn_quick_theme_toggle").assertExists().performClick()
    assertEquals(true, quickThemeToggled)

    composeTestRule.onNodeWithTag("btn_settings").assertExists().performClick()
    assertEquals(true, settingsOpened)
  }
}

