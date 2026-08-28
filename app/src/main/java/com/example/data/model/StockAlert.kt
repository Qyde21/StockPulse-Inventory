package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CatApparel
import com.example.ui.theme.CatBeverages
import com.example.ui.theme.CatElectronics
import com.example.ui.theme.CatGroceries
import com.example.ui.theme.CatHome
import com.example.ui.theme.CatOther
import com.example.ui.theme.CatPersonalCare

enum class AlertSeverity {
    CRITICAL_OUT_OF_STOCK,
    WARNING_LOW_STOCK
}

data class StockAlert(
    val product: Product,
    val severity: AlertSeverity,
    val message: String,
    val suggestedReorderQty: Int
)

data class InventoryCategory(
    val id: String,
    val name: String,
    val color: Color
)

object RetailCategories {
    val ALL = listOf(
        InventoryCategory("beverages", "Beverages", CatBeverages),
        InventoryCategory("groceries", "Groceries & Food", CatGroceries),
        InventoryCategory("electronics", "Electronics & Acc", CatElectronics),
        InventoryCategory("personal_care", "Personal Care & Health", CatPersonalCare),
        InventoryCategory("apparel", "Apparel & Accessories", CatApparel),
        InventoryCategory("home", "Home & Hardware", CatHome),
        InventoryCategory("stationery", "Stationery & Office", CatOther),
        InventoryCategory("other", "General Merchandise", CatOther)
    )

    fun getColor(categoryName: String): Color {
        return ALL.firstOrNull { it.name.equals(categoryName, ignoreCase = true) || it.id.equals(categoryName, ignoreCase = true) }?.color
            ?: CatOther
    }
}
