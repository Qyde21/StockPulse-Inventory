package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertSeverity
import com.example.data.model.Product
import com.example.data.model.StockAlert
import com.example.data.model.StockStatus
import com.example.ui.components.LowStockIndicatorBadge
import com.example.ui.components.StockBadge
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.AlertInStock
import com.example.ui.theme.AlertInStockBg
import com.example.ui.theme.AlertLowStock
import com.example.ui.theme.AlertLowStockBg
import com.example.ui.theme.AlertOutOfStock
import com.example.ui.theme.AlertOutOfStockBg
import com.example.ui.theme.BrandNavy900
import com.example.ui.theme.PrimaryBlue
import com.example.ui.viewmodel.InventoryStats

@Composable
fun DashboardScreen(
    stats: InventoryStats,
    stockAlerts: List<StockAlert>,
    products: List<Product> = emptyList(),
    onOpenScanner: () -> Unit,
    onOpenAddProduct: () -> Unit,
    onOpenPos: () -> Unit,
    onViewCatalog: () -> Unit,
    onRestockAlertItem: (Product, Int) -> Unit,
    onSelectProduct: (Product) -> Unit,
    onResetDemoData: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Header Greeting & Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "StockPulse Retail",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Real-time stock monitoring & store operations",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onResetDemoData,
                    modifier = Modifier.testTag("btn_reset_demo")
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Reload Sample Data",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Real-Time Stock Alerts Banner
        item {
            val totalAlerts = stockAlerts.size
            val outOfStockCount = stockAlerts.count { it.severity == AlertSeverity.CRITICAL_OUT_OF_STOCK }
            val lowStockCount = stockAlerts.count { it.severity == AlertSeverity.WARNING_LOW_STOCK }

            if (totalAlerts > 0) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (outOfStockCount > 0) AlertOutOfStockBg else AlertLowStockBg
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (outOfStockCount > 0) AlertOutOfStock.copy(alpha = 0.4f) else AlertLowStock.copy(alpha = 0.4f),
                            RoundedCornerShape(16.dp)
                        )
                        .testTag("realtime_stock_alerts_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (outOfStockCount > 0) AlertOutOfStock else AlertLowStock),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Real-Time Stock Alerts ($totalAlerts items)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (outOfStockCount > 0) AlertOutOfStock else AlertLowStock
                            )
                            Text(
                                text = "$outOfStockCount items out of stock • $lowStockCount items low",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            } else {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AlertInStockBg),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AlertInStock)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "All inventory levels are optimal. Zero threshold alerts.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AlertInStock
                        )
                    }
                }
            }
        }

        // 4 KPI Metric Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCard(
                        title = "Retail Value",
                        value = "$${String.format("%.2f", stats.totalRetailValue)}",
                        subtitle = "Cost: $${String.format("%.2f", stats.totalCostValue)}",
                        icon = Icons.Default.AttachMoney,
                        iconColor = PrimaryBlue,
                        modifier = Modifier.weight(1f)
                    )

                    KpiCard(
                        title = "Total Units",
                        value = "${stats.totalUnitsInStock} pcs",
                        subtitle = "${stats.totalProducts} SKUs active",
                        icon = Icons.Default.Inventory,
                        iconColor = AccentTeal,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCard(
                        title = "Out of Stock",
                        value = "${stats.totalOutCount} items",
                        subtitle = "Immediate reorder req.",
                        icon = Icons.Default.Error,
                        iconColor = AlertOutOfStock,
                        modifier = Modifier.weight(1f)
                    )

                    KpiCard(
                        title = "Low Stock Alerts",
                        value = "${stats.totalLowCount} items",
                        subtitle = "Below reorder level",
                        icon = Icons.Default.Warning,
                        iconColor = AlertLowStock,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick Operations Action Row
        item {
            Text(
                text = "Quick Operations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Barcode Scanner Action
                ElevatedCard(
                    onClick = onOpenScanner,
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dashboard_btn_scan_barcode")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Barcode Scan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Instant camera lookup",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // POS Quick Sale Action
                ElevatedCard(
                    onClick = onOpenPos,
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dashboard_btn_pos_sale")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PointOfSale,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "POS Checkout",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Record store sales",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Urgent Restock Feed
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Items Requiring Action",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "View All Catalog →",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onViewCatalog() }
                        .padding(4.dp)
                )
            }
        }

        if (stockAlerts.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = AlertInStock,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No Stock Alerts Active", fontWeight = FontWeight.Bold)
                        Text(
                            "Every product is currently above its reorder threshold level.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(stockAlerts, key = { "alert_${it.product.id}" }) { alert ->
                StockAlertCard(
                    alert = alert,
                    onRestock = { onRestockAlertItem(alert.product, alert.suggestedReorderQty) },
                    onSelect = { onSelectProduct(alert.product) }
                )
            }
        }

        // Main Inventory Stock Items List
        val displayProducts = if (products.isNotEmpty()) {
            products
        } else {
            stockAlerts.map { it.product }
        }

        if (displayProducts.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Current Stock Items",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("dashboard_stock_items_header")
                        )
                        Text(
                            text = "${displayProducts.size} products tracked in live inventory",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "Full Inventory (${displayProducts.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            items(displayProducts, key = { "product_${it.id}" }) { product ->
                DashboardStockItemCard(
                    product = product,
                    onSelect = { onSelectProduct(product) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StockAlertCard(
    alert: StockAlert,
    onRestock: () -> Unit,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCritical = alert.severity == AlertSeverity.CRITICAL_OUT_OF_STOCK
    val cardBg = if (isCritical) AlertOutOfStockBg.copy(alpha = 0.5f) else AlertLowStockBg.copy(alpha = 0.5f)
    val accentColor = if (isCritical) AlertOutOfStock else AlertLowStock

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .clickable { onSelect() }
            .testTag("alert_card_${alert.product.sku}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StockBadge(
                    status = alert.product.stockStatus,
                    currentStock = alert.product.currentStock,
                    unit = alert.product.unit
                )

                Text(
                    text = "SKU: ${alert.product.sku}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = alert.product.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = alert.message,
                fontSize = 12.sp,
                color = accentColor,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Supplier: ${alert.product.supplier}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Location: ${alert.product.locationRack}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onRestock,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    modifier = Modifier.testTag("btn_quick_restock_${alert.product.sku}")
                ) {
                    Icon(
                        Icons.Default.LocalShipping,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+${alert.suggestedReorderQty} Restock",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardStockItemCard(
    product: Product,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOutOfStock = product.stockStatus == StockStatus.OUT_OF_STOCK
    val isLowStock = product.stockStatus == StockStatus.LOW_STOCK

    val borderColor = when {
        isOutOfStock -> AlertOutOfStock.copy(alpha = 0.45f)
        isLowStock -> AlertLowStock.copy(alpha = 0.45f)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable { onSelect() }
            .testTag("stock_item_card_${product.sku}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stock Item Name & Category
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("stock_item_name_${product.sku}")
                    )
                    Text(
                        text = "${product.category} • SKU: ${product.sku}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Low-stock indicator badge / Status badge
                LowStockIndicatorBadge(
                    isLowStock = isLowStock,
                    isOutOfStock = isOutOfStock,
                    currentStock = product.currentStock,
                    unit = product.unit,
                    modifier = Modifier.testTag("stock_item_badge_${product.sku}")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quantity & Pricing details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${product.currentStock}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = when {
                            isOutOfStock -> AlertOutOfStock
                            isLowStock -> AlertLowStock
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.testTag("stock_item_qty_${product.sku}")
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${product.unit} on hand (min: ${product.reorderThreshold})",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                Text(
                    text = "$${String.format("%.2f", product.sellingPrice)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

