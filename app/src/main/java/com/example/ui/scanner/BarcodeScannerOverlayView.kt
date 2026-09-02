package com.example.ui.scanner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.AccentTealLight

/**
 * Dedicated visual overlay view for the camera barcode scanner.
 * Includes:
 * - High-precision viewfinder reticle with darkened outer scrim
 * - Corner alignment brackets and laser scanning animation
 * - Rich visual feedback on successful scan:
 *   * Viewfinder flash & glowing emerald border
 *   * Corner bracket bounce and color change
 *   * Animated center checkmark badge with recognized barcode digits
 *   * Dynamic scan guidance status
 * - Camera controls (torch, camera flip, close)
 * - Quick demo barcode simulator for testing in emulator environments
 * - Manual barcode digit input
 */
@Composable
fun BarcodeScannerOverlayView(
    isScanning: Boolean = true,
    isSuccess: Boolean = false,
    scannedCode: String? = null,
    isTorchOn: Boolean = false,
    onToggleTorch: () -> Unit = {},
    onFlipCamera: () -> Unit = {},
    onClose: () -> Unit,
    onSimulateScan: (String) -> Unit = {},
    onManualSubmit: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var manualBarcodeInput by remember { mutableStateOf("") }
    val density = LocalDensity.current

    // Laser Animation when scanning
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser_anim")
    val laserFraction by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_fraction"
    )

    // Scanning dot pulse
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )

    // Flash animation on scan success
    val flashAlpha = remember { Animatable(0f) }
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            flashAlpha.snapTo(0.55f)
            flashAlpha.animateTo(0f, animationSpec = tween(450, easing = FastOutSlowInEasing))
        } else {
            flashAlpha.snapTo(0f)
        }
    }

    // Corner bracket spring expansion on scan success
    val cornerBracketLength by animateDpAsState(
        targetValue = if (isSuccess) 36.dp else 26.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "corner_length"
    )

    // Corner and reticle color shift on success (Teal -> Emerald Green)
    val reticleColor by animateColorAsState(
        targetValue = if (isSuccess) Color(0xFF10B981) else AccentTealLight,
        animationSpec = tween(200),
        label = "reticle_color"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("barcode_scanner_overlay_view")
    ) {
        val screenWidthPx = with(density) { maxWidth.toPx() }
        val screenHeightPx = with(density) { maxHeight.toPx() }

        val boxWidthPx = screenWidthPx * 0.80f
        val boxHeightPx = boxWidthPx * 0.65f
        val leftPx = (screenWidthPx - boxWidthPx) / 2f
        val topPx = (screenHeightPx - boxHeightPx) / 2.35f
        val cornerRadiusPx = with(density) { 18.dp.toPx() }

        // 1. Viewfinder Cutout & Darkened Scrim Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val scrimPath = Path().apply {
                addRect(Rect(0f, 0f, size.width, size.height))
            }
            val cutoutPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        left = leftPx,
                        top = topPx,
                        right = leftPx + boxWidthPx,
                        bottom = topPx + boxHeightPx,
                        radiusX = cornerRadiusPx,
                        radiusY = cornerRadiusPx
                    )
                )
            }

            // Translucent scrim covering the area outside the viewfinder
            val overlayPath = Path.combine(PathOperation.Difference, scrimPath, cutoutPath)
            drawPath(
                path = overlayPath,
                color = Color.Black.copy(alpha = 0.62f)
            )

            // Flash overlay across the cutout on successful scan
            if (flashAlpha.value > 0f) {
                drawRoundRect(
                    color = Color(0xFF10B981).copy(alpha = flashAlpha.value),
                    topLeft = Offset(leftPx, topPx),
                    size = Size(boxWidthPx, boxHeightPx),
                    cornerRadius = CornerRadius(cornerRadiusPx)
                )
            }

            // Viewfinder border (Thicker and glowing on success)
            drawRoundRect(
                color = if (isSuccess) Color(0xFF10B981) else Color.White.copy(alpha = 0.65f),
                topLeft = Offset(leftPx, topPx),
                size = Size(boxWidthPx, boxHeightPx),
                cornerRadius = CornerRadius(cornerRadiusPx),
                style = Stroke(width = if (isSuccess) 3.5.dp.toPx() else 1.5.dp.toPx())
            )

            // Outer Glow ring on success
            if (isSuccess) {
                drawRoundRect(
                    color = Color(0xFF10B981).copy(alpha = 0.4f),
                    topLeft = Offset(leftPx - 4.dp.toPx(), topPx - 4.dp.toPx()),
                    size = Size(boxWidthPx + 8.dp.toPx(), boxHeightPx + 8.dp.toPx()),
                    cornerRadius = CornerRadius(cornerRadiusPx + 4.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // Center targeting reticle ticks (+)
            val centerX = leftPx + boxWidthPx / 2f
            val centerY = topPx + boxHeightPx / 2f
            val tickLength = 10.dp.toPx()
            val tickColor = if (isSuccess) Color(0xFF10B981).copy(alpha = 0.7f) else Color.White.copy(alpha = 0.35f)
            val tickStroke = 1.5.dp.toPx()

            // Center horizontal cross tick
            drawLine(tickColor, Offset(centerX - tickLength, centerY), Offset(centerX + tickLength, centerY), tickStroke)
            // Center vertical cross tick
            drawLine(tickColor, Offset(centerX, centerY - tickLength), Offset(centerX, centerY + tickLength), tickStroke)

            // Corner Brackets
            val cornerLen = with(density) { cornerBracketLength.toPx() }
            val cornerStroke = with(density) { (if (isSuccess) 5.dp else 4.dp).toPx() }

            // Top-Left
            drawLine(reticleColor, Offset(leftPx, topPx + cornerLen), Offset(leftPx, topPx), cornerStroke)
            drawLine(reticleColor, Offset(leftPx, topPx), Offset(leftPx + cornerLen, topPx), cornerStroke)

            // Top-Right
            drawLine(reticleColor, Offset(leftPx + boxWidthPx - cornerLen, topPx), Offset(leftPx + boxWidthPx, topPx), cornerStroke)
            drawLine(reticleColor, Offset(leftPx + boxWidthPx, topPx), Offset(leftPx + boxWidthPx, topPx + cornerLen), cornerStroke)

            // Bottom-Left
            drawLine(reticleColor, Offset(leftPx, topPx + boxHeightPx - cornerLen), Offset(leftPx, topPx + boxHeightPx), cornerStroke)
            drawLine(reticleColor, Offset(leftPx, topPx + boxHeightPx), Offset(leftPx + cornerLen, topPx + boxHeightPx), cornerStroke)

            // Bottom-Right
            drawLine(reticleColor, Offset(leftPx + boxWidthPx - cornerLen, topPx + boxHeightPx), Offset(leftPx + boxWidthPx, topPx + boxHeightPx), cornerStroke)
            drawLine(reticleColor, Offset(leftPx + boxWidthPx, topPx + boxHeightPx), Offset(leftPx + boxWidthPx, topPx + boxHeightPx - cornerLen), cornerStroke)

            // Scanning Laser Line (Sweeps when active, pauses and expands on success)
            if (isScanning && !isSuccess) {
                val laserY = topPx + (boxHeightPx * laserFraction)
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            AccentTealLight.copy(alpha = 0.2f),
                            AccentTealLight,
                            Color.White,
                            AccentTealLight,
                            AccentTealLight.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(leftPx + 12.dp.toPx(), laserY),
                    end = Offset(leftPx + boxWidthPx - 12.dp.toPx(), laserY),
                    strokeWidth = 3.dp.toPx()
                )
            } else if (isSuccess) {
                // Confirmation laser burst across center
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            Color(0xFF10B981).copy(alpha = 0.3f),
                            Color(0xFF10B981),
                            Color.White,
                            Color(0xFF10B981),
                            Color(0xFF10B981).copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(leftPx + 8.dp.toPx(), centerY),
                    end = Offset(leftPx + boxWidthPx - 8.dp.toPx(), centerY),
                    strokeWidth = 4.dp.toPx()
                )
            }
        }

        // 2. Success Feedback Center Badge
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = with(density) { (topPx + (boxHeightPx * 0.22f)).toDp() })
                .width(with(density) { (boxWidthPx * 0.90f).toDp() }),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = isSuccess,
                enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.95f),
                    border = BorderStroke(1.5.dp, Color(0xFF10B981)),
                    shadowElevation = 8.dp,
                    modifier = Modifier.testTag("scan_success_feedback_badge")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Scan Success",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "BARCODE CAPTURED",
                                color = Color(0xFF10B981),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        }

                        if (!scannedCode.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E293B),
                                border = BorderStroke(1.dp, Color(0xFF334155))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode,
                                        contentDescription = null,
                                        tint = AccentTealLight,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = scannedCode,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Top Header Bar (Close, Torch, Camera Flip)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledIconButton(
                onClick = onClose,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.65f)
                ),
                modifier = Modifier
                    .size(44.dp)
                    .testTag("close_scanner_btn")
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close Scanner", tint = Color.White)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Torch Toggle
                FilledIconButton(
                    onClick = onToggleTorch,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isTorchOn) AccentTeal else Color.Black.copy(alpha = 0.65f)
                    ),
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("torch_toggle_btn")
                ) {
                    Icon(
                        imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Flashlight",
                        tint = if (isTorchOn) Color.White else Color.LightGray
                    )
                }

                // Camera Flip
                FilledIconButton(
                    onClick = onFlipCamera,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.65f)
                    ),
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("flip_camera_btn")
                ) {
                    Icon(
                        Icons.Default.FlipCameraAndroid,
                        contentDescription = "Switch Camera",
                        tint = Color.White
                    )
                }
            }
        }

        // 4. Bottom Controls, Status Indicator & Test Barcode Drawer
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.88f), Color.Black)
                    )
                )
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // Viewfinder Status Indicator
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSuccess) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Scan complete! Processing item...",
                        color = Color(0xFF10B981),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(AccentTealLight.copy(alpha = dotAlpha), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Align barcode or QR code within the frame",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Quick Demo Barcode Carousel
            Text(
                text = "Quick Demo Barcodes (Tap to Test Scan):",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Cold Brew (Low)" to "8901030381023",
                    "Chocolate (Out)" to "7622210449283",
                    "USB Cable (Low)" to "6941059632847",
                    "Mineral Water" to "5000159482104",
                    "New Item Barcode" to "9900881122334"
                ).forEach { (label, code) ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, Color(0xFF475569)),
                        modifier = Modifier
                            .clickable { onSimulateScan(code) }
                            .testTag("sample_scan_$code")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Default.QrCode,
                                contentDescription = null,
                                tint = AccentTealLight,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Manual Barcode Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = manualBarcodeInput,
                    onValueChange = { manualBarcodeInput = it },
                    placeholder = { Text("Or enter barcode digits manually...", color = Color.Gray, fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        if (manualBarcodeInput.isNotBlank()) {
                            onManualSubmit(manualBarcodeInput)
                        }
                    }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentTeal,
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.85f),
                        unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.85f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("manual_barcode_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                FilledIconButton(
                    onClick = {
                        if (manualBarcodeInput.isNotBlank()) {
                            onManualSubmit(manualBarcodeInput)
                        }
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = AccentTeal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .size(50.dp)
                        .testTag("btn_lookup_manual_barcode")
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Lookup Barcode", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
