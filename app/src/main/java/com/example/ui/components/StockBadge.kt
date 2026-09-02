package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StockStatus
import com.example.ui.theme.AlertInStock
import com.example.ui.theme.AlertInStockBg
import com.example.ui.theme.AlertLowStock
import com.example.ui.theme.AlertLowStockBg
import com.example.ui.theme.AlertOutOfStock
import com.example.ui.theme.AlertOutOfStockBg
import com.example.ui.theme.AlertOverstock
import com.example.ui.theme.AlertOverstockBg

@Composable
fun StockBadge(
    status: StockStatus,
    currentStock: Int,
    unit: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label, icon) = when (status) {
        StockStatus.OUT_OF_STOCK -> Quadruple(
            AlertOutOfStockBg,
            AlertOutOfStock,
            "OUT OF STOCK",
            Icons.Default.Error
        )
        StockStatus.LOW_STOCK -> Quadruple(
            AlertLowStockBg,
            AlertLowStock,
            "LOW STOCK ($currentStock $unit)",
            Icons.Default.Warning
        )
        StockStatus.IN_STOCK -> Quadruple(
            AlertInStockBg,
            AlertInStock,
            "IN STOCK ($currentStock $unit)",
            Icons.Default.CheckCircle
        )
        StockStatus.OVERSTOCK -> Quadruple(
            AlertOverstockBg,
            AlertOverstock,
            "WELL STOCKED ($currentStock $unit)",
            Icons.Default.CheckCircle
        )
    }

    val isAlert = status == StockStatus.OUT_OF_STOCK || status == StockStatus.LOW_STOCK

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isAlert) 0.35f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        if (isAlert) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .alpha(pulseAlpha)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }

        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * Dedicated Low-Stock Indicator Badge.
 * Highlights low stock and out-of-stock items with clear color coding,
 * icon indicator, animated pulse for critical alerts, and quantity readout.
 */
@Composable
fun LowStockIndicatorBadge(
    isLowStock: Boolean,
    isOutOfStock: Boolean = false,
    currentStock: Int? = null,
    unit: String = "pcs",
    modifier: Modifier = Modifier
) {
    if (isOutOfStock) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .background(AlertOutOfStockBg)
                .border(1.dp, AlertOutOfStock.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "out_of_stock_pulse")
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 0.35f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 750),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulseAlpha"
            )
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .alpha(pulseAlpha)
                    .clip(CircleShape)
                    .background(AlertOutOfStock)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Out of Stock",
                tint = AlertOutOfStock,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "OUT OF STOCK",
                color = AlertOutOfStock,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.4.sp
            )
        }
    } else if (isLowStock) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .background(AlertLowStockBg)
                .border(1.dp, AlertLowStock.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "low_stock_pulse")
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 0.45f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 850),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulseAlpha"
            )
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .alpha(pulseAlpha)
                    .clip(CircleShape)
                    .background(AlertLowStock)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Low Stock",
                tint = AlertLowStock,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (currentStock != null) "LOW STOCK ($currentStock $unit)" else "LOW STOCK",
                color = AlertLowStock,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp
            )
        }
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .background(AlertInStockBg)
                .border(1.dp, AlertInStock.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "In Stock",
                tint = AlertInStock,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (currentStock != null) "IN STOCK ($currentStock $unit)" else "IN STOCK",
                color = AlertInStock,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp
            )
        }
    }
}

@Composable
fun StockStatusBar(
    currentStock: Int,
    threshold: Int,
    idealStock: Int,
    modifier: Modifier = Modifier
) {
    val safeIdeal = if (idealStock > 0) idealStock else (threshold * 2).coerceAtLeast(10)
    val progress = (currentStock.toFloat() / safeIdeal.toFloat()).coerceIn(0f, 1f)

    val barColor = when {
        currentStock <= 0 -> AlertOutOfStock
        currentStock <= threshold -> AlertLowStock
        else -> AlertInStock
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeightWidth(progress)
                    .background(barColor)
            )
        }
    }
}

private fun Modifier.fillMaxHeightWidth(fraction: Float): Modifier = this.then(
    Modifier.padding(0.dp) // dummy to chain
)

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
