package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

@Composable
fun BarcodeCanvasView(
    barcode: String,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true,
    height: Int = 64
) {
    val barPattern = remember(barcode) {
        generateBarcodePattern(barcode)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height.dp)
        ) {
            val totalBars = barPattern.size
            if (totalBars == 0) return@Canvas

            val barWidth = size.width / totalBars.toFloat()
            val canvasHeight = size.height

            for (i in barPattern.indices) {
                if (barPattern[i]) {
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(i * barWidth, 0f),
                        size = Size(barWidth + 0.5f, canvasHeight)
                    )
                }
            }
        }

        if (showLabel) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = formatBarcodeDisplay(barcode),
                color = Color.Black,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
        }
    }
}

private fun formatBarcodeDisplay(code: String): String {
    if (code.length == 13) {
        return "${code.substring(0, 1)}  ${code.substring(1, 7)}  ${code.substring(7, 13)}"
    }
    if (code.length == 12) {
        return "${code.substring(0, 1)}  ${code.substring(1, 6)}  ${code.substring(6, 11)}  ${code.substring(11)}"
    }
    return code
}

/**
 * Generates an accurate-looking, deterministic binary barcode pattern from barcode string
 * using standard start, data, guard, and stop patterns.
 */
private fun generateBarcodePattern(code: String): List<Boolean> {
    if (code.isEmpty()) return emptyList()

    val pattern = mutableListOf<Boolean>()

    // Quiet zone / Start guard (101)
    pattern.addAll(listOf(true, false, true))

    // Hash deterministic pseudo-encoding based on code characters
    var seed = abs(code.hashCode())
    for (char in code) {
        val digit = if (char.isDigit()) char.digitToInt() else (char.code % 10)
        // 7-module encoding per character
        val bits = when (digit) {
            0 -> listOf(false, false, false, true, true, false, true)
            1 -> listOf(false, false, true, true, false, false, true)
            2 -> listOf(false, false, true, false, false, true, true)
            3 -> listOf(false, true, true, true, true, false, true)
            4 -> listOf(false, true, false, false, false, true, true)
            5 -> listOf(false, true, true, false, false, false, true)
            6 -> listOf(false, true, false, true, true, true, true)
            7 -> listOf(false, true, true, true, false, true, true)
            8 -> listOf(false, true, true, false, true, true, true)
            else -> listOf(false, false, false, true, false, true, true)
        }
        pattern.addAll(bits)
    }

    // Center guard (01010)
    pattern.addAll(listOf(false, true, false, true, false))

    // Supplementary checksum modulation
    for (char in code.reversed()) {
        val digit = (char.code * 3 + seed) % 10
        val bits = when (digit) {
            0 -> listOf(true, true, true, false, false, true, false)
            1 -> listOf(true, true, false, false, true, true, false)
            2 -> listOf(true, true, false, true, true, false, false)
            3 -> listOf(true, false, false, false, false, true, false)
            4 -> listOf(true, false, true, true, true, false, false)
            5 -> listOf(true, false, false, true, true, true, false)
            6 -> listOf(true, false, true, false, false, false, false)
            7 -> listOf(true, false, false, false, true, false, false)
            8 -> listOf(true, false, false, true, false, false, false)
            else -> listOf(true, true, true, false, true, false, false)
        }
        pattern.addAll(bits)
        seed = (seed * 31 + digit)
    }

    // End guard (101)
    pattern.addAll(listOf(true, false, true))

    return pattern
}
