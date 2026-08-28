package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

@Composable
fun RestockDialog(
    product: Product,
    suggestedQty: Int,
    onDismiss: () -> Unit,
    onConfirm: (quantity: Int, note: String) -> Unit
) {
    var quantity by remember { mutableIntStateOf(if (suggestedQty > 0) suggestedQty else 20) }
    var invoiceRef by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.LocalShipping,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Restock Inventory",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Supplier: ${product.supplier}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Current Stock: ${product.currentStock} ${product.unit} (Threshold: ${product.reorderThreshold})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Quantity to Receive (${product.unit}):", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = quantity.toString(),
                    onValueChange = { quantity = it.toIntOrNull()?.coerceAtLeast(1) ?: 1 },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("restock_quantity_input"),
                    singleLine = true
                )

                // Quick preset buttons based on ideal stock & multiples
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val p1 = product.reorderThreshold.coerceAtLeast(5)
                    val p2 = (product.idealStock - product.currentStock).coerceAtLeast(10)
                    val p3 = product.idealStock

                    listOf(p1, p2, p3).distinct().forEach { preset ->
                        FilledTonalButton(
                            onClick = { quantity = preset },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+$preset", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = invoiceRef,
                    onValueChange = { invoiceRef = it },
                    label = { Text("PO / Invoice / Delivery Note #") },
                    placeholder = { Text("e.g. PO-88291") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("restock_note_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                val costEst = quantity * product.costPrice
                Text(
                    text = "Est. Intake Cost: $${String.format("%.2f", costEst)} ($${String.format("%.2f", product.costPrice)} / unit)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val note = if (invoiceRef.isNotBlank()) "Restock PO #$invoiceRef" else "Supplier shipment received"
                    onConfirm(quantity, note)
                },
                modifier = Modifier.testTag("restock_confirm_btn")
            ) {
                Text("Receive Stock (+$quantity)")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("restock_cancel_btn")
            ) {
                Text("Cancel")
            }
        }
    )
}
