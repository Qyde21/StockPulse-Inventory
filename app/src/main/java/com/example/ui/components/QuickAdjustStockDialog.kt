package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.ui.theme.AlertInStock
import com.example.ui.theme.AlertOutOfStock

@Composable
fun QuickAdjustStockDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (delta: Int, reason: String) -> Unit
) {
    var delta by remember { mutableIntStateOf(1) }
    var isIncrement by remember { mutableStateOf(true) }
    var selectedReason by remember { mutableStateOf("Manual Audit Count") }
    var customReason by remember { mutableStateOf("") }

    val presetReasons = listOf("Manual Audit Count", "Stock Intake", "Customer Return", "Damaged / Expired", "Store Display")

    val calculatedNewStock = if (isIncrement) {
        product.currentStock + delta
    } else {
        (product.currentStock - delta).coerceAtLeast(0)
    }

    val actualDelta = if (isIncrement) delta else -delta

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Adjust Stock Count",
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
                // Direction Toggle (Add vs Subtract)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { isIncrement = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("adjust_increment_toggle"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isIncrement) AlertInStock else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isIncrement) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Stock (+)")
                    }

                    Button(
                        onClick = { isIncrement = false },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("adjust_decrement_toggle"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isIncrement) AlertOutOfStock else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (!isIncrement) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Remove (-)")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stepper & Count
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = { if (delta > 1) delta-- },
                        modifier = Modifier.testTag("adjust_minus_btn")
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease count")
                    }

                    OutlinedTextField(
                        value = delta.toString(),
                        onValueChange = { delta = it.toIntOrNull()?.coerceAtLeast(1) ?: 1 },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .width(100.dp)
                            .testTag("adjust_quantity_input"),
                        singleLine = true
                    )

                    IconButton(
                        onClick = { delta++ },
                        modifier = Modifier.testTag("adjust_plus_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase count")
                    }
                }

                // Quick presets
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(1, 5, 10, 25).forEach { qty ->
                        FilledTonalButton(
                            onClick = { delta = qty },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("+$qty")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Result Preview Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Current: ${product.currentStock} ${product.unit}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "New: $calculatedNewStock ${product.unit}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Reason / Note:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    presetReasons.take(3).forEach { reason ->
                        FilterChip(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason },
                            label = { Text(reason, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = customReason,
                    onValueChange = { customReason = it },
                    placeholder = { Text("Additional note (optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("adjust_note_input"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalReason = if (customReason.isNotBlank()) customReason else selectedReason
                    onConfirm(actualDelta, finalReason)
                },
                modifier = Modifier.testTag("adjust_confirm_button")
            ) {
                Text("Confirm Update")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("adjust_cancel_button")
            ) {
                Text("Cancel")
            }
        }
    )
}
