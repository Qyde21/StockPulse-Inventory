package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
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
}

