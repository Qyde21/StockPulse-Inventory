package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.data.model.RetailCategories
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.AlertInStock
import com.example.ui.theme.AlertOutOfStock

@Composable
fun PosCheckoutScreen(
    cart: Map<Long, Int>,
    allProducts: List<Product>,
    discountPercent: Double,
    onAddToCart: (Product) -> Unit,
    onRemoveSingleFromCart: (Long) -> Unit,
    onRemoveItemCompletely: (Long) -> Unit,
    onClearCart: () -> Unit,
    onSetDiscount: (Double) -> Unit,
    onCheckout: () -> Unit,
    onOpenScanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showCheckoutSuccessDialog by remember { mutableStateOf(false) }

    val cartEntries = cart.mapNotNull { (id, qty) ->
        val prod = allProducts.find { it.id == id }
        if (prod != null) prod to qty else null
    }

    val subtotal = cartEntries.sumOf { (prod, qty) -> prod.sellingPrice * qty }
    val discountAmount = subtotal * (discountPercent / 100.0)
    val discountedSubtotal = subtotal - discountAmount
    val estimatedTax = discountedSubtotal * 0.05 // 5% retail tax
    val grandTotal = discountedSubtotal + estimatedTax
    val totalItemsCount = cartEntries.sumOf { it.second }

    val filteredQuickPickProducts = remember(searchQuery, allProducts) {
        if (searchQuery.isBlank()) {
            allProducts.filter { it.currentStock > 0 }.take(6)
        } else {
            val q = searchQuery.trim().lowercase()
            allProducts.filter {
                it.currentStock > 0 &&
                (it.name.lowercase().contains(q) ||
                 it.barcode.contains(q) ||
                 it.sku.lowercase().contains(q))
            }.take(8)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Title Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "POS Rapid Checkout",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Scan or select items to ring up store sales",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (cart.isNotEmpty()) {
                IconButton(
                    onClick = onClearCart,
                    modifier = Modifier.testTag("pos_btn_clear_cart")
                ) {
                    Icon(
                        Icons.Default.RemoveShoppingCart,
                        contentDescription = "Clear Cart",
                        tint = AlertOutOfStock
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Scan & Search Input Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search product to add...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("pos_search_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            FilledIconButton(
                onClick = onOpenScanner,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .size(54.dp)
                    .testTag("pos_scan_barcode_btn")
            ) {
                Icon(
                    Icons.Default.QrCodeScanner,
                    contentDescription = "Scan Barcode to Add",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Select Shelf items (Horizontal strip)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            filteredQuickPickProducts.take(3).forEach { prod ->
                ElevatedCard(
                    onClick = { onAddToCart(prod) },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pos_quick_pick_${prod.sku}")
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = prod.name,
                            maxLines = 1,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("$${String.format("%.2f", prod.sellingPrice)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text("${prod.currentStock} in stock", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Cart items or Empty State
        if (cartEntries.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "POS Cart is Empty",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Scan a barcode or tap quick items above to ring up.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cartEntries, key = { it.first.id }) { (product, quantity) ->
                    PosCartItemRow(
                        product = product,
                        quantity = quantity,
                        onIncrement = { onAddToCart(product) },
                        onDecrement = { onRemoveSingleFromCart(product.id) },
                        onRemove = { onRemoveItemCompletely(product.id) }
                    )
                }
            }
        }

        // Bottom Receipt & Checkout Calculation Panel
        Card(
            shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 70.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Discount Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Store Discount:", style = MaterialTheme.typography.labelSmall)

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(0.0, 5.0, 10.0, 15.0).forEach { disc ->
                            FilterChip(
                                selected = discountPercent == disc,
                                onClick = { onSetDiscount(disc) },
                                label = { Text("${disc.toInt()}%", fontSize = 10.sp) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Breakdown lines
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Subtotal ($totalItemsCount units):", style = MaterialTheme.typography.bodySmall)
                    Text("$${String.format("%.2f", subtotal)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }

                if (discountAmount > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Discount (${discountPercent.toInt()}%):", style = MaterialTheme.typography.bodySmall, color = AlertInStock)
                        Text("-$${String.format("%.2f", discountAmount)}", style = MaterialTheme.typography.bodySmall, color = AlertInStock)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Est. Sales Tax (5%):", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$${String.format("%.2f", estimatedTax)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Grand Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Due:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "$${String.format("%.2f", grandTotal)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        onCheckout()
                        showCheckoutSuccessDialog = true
                    },
                    enabled = cartEntries.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = AlertInStock),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("pos_btn_complete_sale")
                ) {
                    Icon(Icons.Default.PointOfSale, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Charge $${String.format("%.2f", grandTotal)} & Complete Sale",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }

    if (showCheckoutSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showCheckoutSuccessDialog = false },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = AlertInStock,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text("Sale Recorded Successfully!", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Inventory counts have been automatically deducted and logged into the audit timeline.")
            },
            confirmButton = {
                Button(
                    onClick = { showCheckoutSuccessDialog = false }
                ) {
                    Text("Next Sale")
                }
            }
        )
    }
}

@Composable
private fun PosCartItemRow(
    product: Product,
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .fillMaxWidth()
            .testTag("pos_cart_row_${product.sku}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "$${String.format("%.2f", product.sellingPrice)} each • SKU: ${product.sku}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Stepper
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onDecrement,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Minus", modifier = Modifier.size(16.dp))
                }

                Text(
                    text = "$quantity",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                IconButton(
                    onClick = onIncrement,
                    enabled = quantity < product.currentStock,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Plus", modifier = Modifier.size(16.dp))
                }

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "$${String.format("%.2f", product.sellingPrice * quantity)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(60.dp)
                )

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Remove line",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
