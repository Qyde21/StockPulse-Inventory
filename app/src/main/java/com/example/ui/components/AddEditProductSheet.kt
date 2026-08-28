package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.data.model.RetailCategories
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductSheet(
    product: Product? = null,
    initialBarcode: String? = null,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onOpenScanner: () -> Unit,
    onSave: (Product, Boolean) -> Unit
) {
    val isNew = product == null

    var barcode by remember(product, initialBarcode) {
        mutableStateOf(product?.barcode ?: initialBarcode ?: "")
    }
    var sku by remember(product) { mutableStateOf(product?.sku ?: "") }
    var name by remember(product) { mutableStateOf(product?.name ?: "") }
    var category by remember(product) { mutableStateOf(product?.category ?: "Beverages") }
    var costPriceStr by remember(product) { mutableStateOf(product?.costPrice?.toString() ?: "1.50") }
    var sellingPriceStr by remember(product) { mutableStateOf(product?.sellingPrice?.toString() ?: "3.99") }
    var currentStockStr by remember(product) { mutableStateOf(product?.currentStock?.toString() ?: "10") }
    var reorderThresholdStr by remember(product) { mutableStateOf(product?.reorderThreshold?.toString() ?: "5") }
    var idealStockStr by remember(product) { mutableStateOf(product?.idealStock?.toString() ?: "30") }
    var unit by remember(product) { mutableStateOf(product?.unit ?: "pcs") }
    var locationRack by remember(product) { mutableStateOf(product?.locationRack ?: "Aisle 1") }
    var supplier by remember(product) { mutableStateOf(product?.supplier ?: "Direct Wholesale") }
    var notes by remember(product) { mutableStateOf(product?.notes ?: "") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val costPrice = costPriceStr.toDoubleOrNull() ?: 0.0
    val sellingPrice = sellingPriceStr.toDoubleOrNull() ?: 0.0
    val margin = if (sellingPrice > 0) ((sellingPrice - costPrice) / sellingPrice) * 100.0 else 0.0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        modifier = Modifier.testTag("add_edit_product_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isNew) "Add New Product" else "Edit Product Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isNew) "Register SKU, pricing & alert threshold" else "SKU: ${product?.sku ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close sheet")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Barcode Input with Scan Button & Generator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    label = { Text("Barcode / EAN / UPC *") },
                    placeholder = { Text("e.g. 8901030381023") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_barcode"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onOpenScanner,
                    modifier = Modifier.testTag("btn_scan_barcode_for_input")
                ) {
                    Icon(
                        Icons.Default.QrCodeScanner,
                        contentDescription = "Scan Barcode with Camera",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = {
                        // Generate realistic 13-digit EAN barcode
                        val randomEan = "7" + (100000000000L..999999999999L).random(Random(System.currentTimeMillis()))
                        barcode = randomEan.take(13)
                    }
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = "Generate Random Barcode",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Product Name
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (sku.isBlank() && it.isNotBlank()) {
                        val prefix = it.take(3).uppercase()
                        val num = (100..999).random()
                        sku = "$prefix-$num"
                    }
                },
                label = { Text("Product Title / Name *") },
                placeholder = { Text("e.g. Organic Sparkling Lemonade 330ml") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_product_name"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // SKU & Unit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = sku,
                    onValueChange = { sku = it },
                    label = { Text("SKU Code") },
                    placeholder = { Text("e.g. BEV-001") },
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("input_sku"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unit") },
                    placeholder = { Text("pcs / bottle / box") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_unit"),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Chips
            Text("Category *", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                RetailCategories.ALL.forEach { cat ->
                    FilterChip(
                        selected = category == cat.name,
                        onClick = { category = cat.name },
                        label = { Text(cat.name, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Prices & Live Margin Preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = costPriceStr,
                    onValueChange = { costPriceStr = it },
                    label = { Text("Cost Price ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_cost_price"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = sellingPriceStr,
                    onValueChange = { sellingPriceStr = it },
                    label = { Text("Selling Price ($) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_selling_price"),
                    singleLine = true
                )
            }

            // Margin info pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Gross Profit: $${String.format("%.2f", (sellingPrice - costPrice).coerceAtLeast(0.0))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Margin: ${String.format("%.1f", margin)}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (margin >= 30) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Stock Quantities & Real-Time Alert Settings
            Text("Stock Quantities & Alert Trigger Level", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = currentStockStr,
                    onValueChange = { currentStockStr = it },
                    label = { Text("Current Stock") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_current_stock"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = reorderThresholdStr,
                    onValueChange = { reorderThresholdStr = it },
                    label = { Text("Alert Threshold") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_reorder_threshold"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = idealStockStr,
                    onValueChange = { idealStockStr = it },
                    label = { Text("Ideal Stock") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_ideal_stock"),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Location Rack & Supplier
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = locationRack,
                    onValueChange = { locationRack = it },
                    label = { Text("Aisle / Shelf / Rack") },
                    placeholder = { Text("Aisle 1 - Shelf B") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_location_rack"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = supplier,
                    onValueChange = { supplier = it },
                    label = { Text("Supplier Name") },
                    placeholder = { Text("Global Foods Co") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_supplier"),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Product Notes / Storage Instructions") },
                placeholder = { Text("Keep refrigerated, fragile packaging...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_notes"),
                maxLines = 2
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Save Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        if (name.isBlank()) {
                            errorMessage = "Please enter a product name."
                            return@Button
                        }
                        if (barcode.isBlank()) {
                            errorMessage = "Please provide or scan a barcode."
                            return@Button
                        }

                        val parsedCost = costPriceStr.toDoubleOrNull() ?: 0.0
                        val parsedSell = sellingPriceStr.toDoubleOrNull() ?: 0.0
                        val parsedStock = currentStockStr.toIntOrNull() ?: 0
                        val parsedThreshold = reorderThresholdStr.toIntOrNull() ?: 5
                        val parsedIdeal = idealStockStr.toIntOrNull() ?: 30

                        val finalProduct = Product(
                            id = product?.id ?: 0,
                            barcode = barcode.trim(),
                            sku = if (sku.isNotBlank()) sku.trim() else "SKU-${(100..999).random()}",
                            name = name.trim(),
                            category = category,
                            costPrice = parsedCost,
                            sellingPrice = parsedSell,
                            currentStock = parsedStock,
                            reorderThreshold = parsedThreshold,
                            idealStock = parsedIdeal,
                            unit = if (unit.isNotBlank()) unit.trim() else "pcs",
                            locationRack = if (locationRack.isNotBlank()) locationRack.trim() else "Main Warehouse",
                            supplier = if (supplier.isNotBlank()) supplier.trim() else "Direct",
                            notes = notes.trim(),
                            lastUpdated = System.currentTimeMillis()
                        )

                        onSave(finalProduct, isNew)
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("btn_save_product")
                ) {
                    Text(if (isNew) "Register Product" else "Save Changes")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
