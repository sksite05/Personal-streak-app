package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.repository.WaterScheduleSlot
import kotlin.math.sin

@Composable
fun WaterBottleCanvas(
    currentBottleMl: Int,
    bottleCapacityMl: Int = 1000,
    targetLevelNowMl: Int = 500,
    scheduleSlots: List<WaterScheduleSlot> = emptyList(),
    modifier: Modifier = Modifier,
    height: Dp = 380.dp,
    interactive: Boolean = true,
    onWaterLevelChanged: ((Int) -> Unit)? = null
) {
    val density = LocalDensity.current

    // Smooth water level animation
    val targetFraction = (currentBottleMl.toFloat() / bottleCapacityMl.coerceAtLeast(1)).coerceIn(0f, 1f)
    val animatedFillFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "waterFillAnimation"
    )

    // Animated sine wave
    val infiniteTransition = rememberInfiniteTransition(label = "waveAndBubbles")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    val bubbleYOffset by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubbleFloat"
    )

    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val glassStrokeColor = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
    val bottleCapColor = if (isDark) Color(0xFF0284C7) else Color(0xFF0369A1)
    val textPaintColor = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.DKGRAY

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .pointerInput(interactive) {
                if (interactive && onWaterLevelChanged != null) {
                    detectTapGestures { offset ->
                        // Calculate clicked percentage
                        val canvasH = size.height
                        val topY = canvasH * 0.18f
                        val bottomY = canvasH * 0.88f
                        val effectiveH = bottomY - topY
                        if (offset.y in topY..bottomY) {
                            val ratio = 1f - ((offset.y - topY) / effectiveH).coerceIn(0f, 1f)
                            val newMl = (ratio * bottleCapacityMl).toInt()
                            onWaterLevelChanged(newMl)
                        }
                    }
                }
            }
            .pointerInput(interactive) {
                if (interactive && onWaterLevelChanged != null) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val canvasH = size.height
                        val topY = canvasH * 0.18f
                        val bottomY = canvasH * 0.88f
                        val effectiveH = bottomY - topY
                        val ratio = 1f - ((change.position.y - topY) / effectiveH).coerceIn(0f, 1f)
                        val newMl = (ratio * bottleCapacityMl).toInt()
                        onWaterLevelChanged(newMl)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val heightPx = size.height

            // Dimensions of the bottle
            val bottleLeft = width * 0.24f
            val bottleRight = width * 0.76f
            val bottleWidth = bottleRight - bottleLeft

            val neckWidth = bottleWidth * 0.38f
            val neckLeft = width * 0.5f - neckWidth * 0.5f
            val neckRight = neckLeft + neckWidth
            val neckTop = heightPx * 0.08f
            val neckBottom = heightPx * 0.16f

            val capTop = heightPx * 0.03f
            val capBottom = neckTop

            val shoulderTop = neckBottom
            val shoulderBottom = heightPx * 0.22f

            val bodyTop = shoulderBottom
            val bodyBottom = heightPx * 0.88f
            val bodyHeight = bodyBottom - bodyTop

            // Bottle path
            val bottlePath = Path().apply {
                // Neck
                moveTo(neckLeft, shoulderTop)
                lineTo(neckLeft, neckTop)
                lineTo(neckRight, neckTop)
                lineTo(neckRight, shoulderTop)

                // Right shoulder
                cubicTo(
                    neckRight + bottleWidth * 0.1f, shoulderTop,
                    bottleRight, shoulderTop + (shoulderBottom - shoulderTop) * 0.5f,
                    bottleRight, shoulderBottom
                )

                // Body right down
                lineTo(bottleRight, bodyBottom - 24f)
                // Bottom-right corner
                quadraticTo(bottleRight, bodyBottom, bottleRight - 28f, bodyBottom)

                // Bottom
                lineTo(bottleLeft + 28f, bodyBottom)

                // Bottom-left corner
                quadraticTo(bottleLeft, bodyBottom, bottleLeft, bodyBottom - 24f)

                // Body left up
                lineTo(bottleLeft, shoulderBottom)

                // Left shoulder
                cubicTo(
                    bottleLeft, shoulderTop + (shoulderBottom - shoulderTop) * 0.5f,
                    neckLeft - bottleWidth * 0.1f, shoulderTop,
                    neckLeft, shoulderTop
                )
                close()
            }

            // Fill subtle translucent background inside bottle
            drawPath(
                path = bottlePath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x1A38BDF8),
                        Color(0x280284C7)
                    )
                )
            )

            // Clip drawing inside bottle for water, bubbles, and waves
            clipPath(bottlePath) {
                // Calculate water height
                val waterY = bodyBottom - (bodyHeight * animatedFillFraction)

                if (animatedFillFraction > 0.01f) {
                    val waveAmplitude = 12f * (1f - (animatedFillFraction - 0.5f) * (animatedFillFraction - 0.5f) * 1.5f).coerceIn(0.2f, 1f)

                    val waterPath = Path().apply {
                        moveTo(bottleLeft - 20f, bodyBottom + 20f)
                        lineTo(bottleLeft - 20f, waterY)

                        // Sine wave surface
                        val steps = 40
                        val stepW = (bottleRight - bottleLeft + 40f) / steps
                        for (i in 0..steps) {
                            val x = bottleLeft - 20f + i * stepW
                            val angle = wavePhase + (x / bottleWidth) * 4f
                            val y = waterY + sin(angle) * waveAmplitude
                            lineTo(x, y)
                        }

                        lineTo(bottleRight + 20f, bodyBottom + 20f)
                        close()
                    }

                    // Water gradient (rich cyan to deep ocean aqua)
                    drawPath(
                        path = waterPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF38BDF8),
                                Color(0xFF0284C7),
                                Color(0xFF0369A1)
                            ),
                            startY = waterY,
                            endY = bodyBottom
                        )
                    )

                    // Wave foam crest highlight
                    val crestPath = Path().apply {
                        val steps = 40
                        val stepW = (bottleRight - bottleLeft + 40f) / steps
                        var started = false
                        for (i in 0..steps) {
                            val x = bottleLeft - 20f + i * stepW
                            val angle = wavePhase + (x / bottleWidth) * 4f
                            val y = waterY + sin(angle) * waveAmplitude
                            if (!started) {
                                moveTo(x, y)
                                started = true
                            } else {
                                lineTo(x, y)
                            }
                        }
                    }
                    drawPath(
                        path = crestPath,
                        color = Color(0x99E0F2FE),
                        style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                    )

                    // Floating bubbles inside water
                    val bubbleColors = Color(0x88FFFFFF)
                    val bubbleSeeds = listOf(
                        Triple(0.35f, 0.45f, 5f),
                        Triple(0.50f, 0.70f, 7f),
                        Triple(0.65f, 0.25f, 4f),
                        Triple(0.42f, 0.85f, 6f),
                        Triple(0.58f, 0.60f, 8f)
                    )

                    for ((bxFrac, byFrac, radius) in bubbleSeeds) {
                        val bx = bottleLeft + bottleWidth * bxFrac
                        val cycleY = ((byFrac + bubbleYOffset) % 1f)
                        val by = waterY + (bodyBottom - waterY) * cycleY
                        if (by > waterY + 10f && by < bodyBottom - 8f) {
                            drawCircle(
                                color = bubbleColors,
                                radius = radius,
                                center = Offset(bx, by)
                            )
                        }
                    }
                }
            }

            // Target Line (where water level should be right now)
            val targetFrac = (targetLevelNowMl.toFloat() / bottleCapacityMl).coerceIn(0f, 1f)
            val targetY = bodyBottom - (bodyHeight * targetFrac)

            // Dashed Target Line across bottle
            drawLine(
                color = Color(0xFFFFB703),
                start = Offset(bottleLeft - 18f, targetY),
                end = Offset(bottleRight + 18f, targetY),
                strokeWidth = 3f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f),
                cap = StrokeCap.Round
            )

            // Glass specular reflection highlight on left side
            val glassHighlight = Path().apply {
                moveTo(bottleLeft + 14f, shoulderBottom + 10f)
                lineTo(bottleLeft + 14f, bodyBottom - 30f)
            }
            drawPath(
                path = glassHighlight,
                color = Color(0x55FFFFFF),
                style = Stroke(width = 6f, cap = StrokeCap.Round)
            )

            // Draw Bottle Outer Outline
            drawPath(
                path = bottlePath,
                color = glassStrokeColor,
                style = Stroke(width = 4.5f, cap = StrokeCap.Round)
            )

            // Draw Bottle Cap
            val capWidth = neckWidth * 1.15f
            val capLeft = width * 0.5f - capWidth * 0.5f
            drawRoundRect(
                color = bottleCapColor,
                topLeft = Offset(capLeft, capTop),
                size = Size(capWidth, capBottom - capTop),
                cornerRadius = CornerRadius(8f, 8f)
            )
            // Cap ridge details
            drawLine(
                color = Color(0x66FFFFFF),
                start = Offset(capLeft + 8f, (capTop + capBottom) / 2),
                end = Offset(capLeft + capWidth - 8f, (capTop + capBottom) / 2),
                strokeWidth = 2f
            )

            // Draw Tick Markings and Time Labels along right & left edges
            val textPaint = android.graphics.Paint().apply {
                color = textPaintColor
                textSize = with(density) { 11.dp.toPx() }
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.LEFT
            }
            val timePaint = android.graphics.Paint().apply {
                color = if (isDark) android.graphics.Color.parseColor("#38BDF8") else android.graphics.Color.parseColor("#0284C7")
                textSize = with(density) { 10.dp.toPx() }
                isAntiAlias = true
                isFakeBoldText = true
                textAlign = android.graphics.Paint.Align.RIGHT
            }
            val targetTagPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#FFB703")
                textSize = with(density) { 9.dp.toPx() }
                isAntiAlias = true
                isFakeBoldText = true
                textAlign = android.graphics.Paint.Align.RIGHT
            }

            // Ml ticks (0, 250, 500, 750, 1000)
            val marks = listOf(0, 250, 500, 750, 1000)
            for (ml in marks) {
                val frac = ml.toFloat() / bottleCapacityMl
                val y = bodyBottom - (bodyHeight * frac)

                // Tick mark inside & outside bottle right edge
                drawLine(
                    color = glassStrokeColor,
                    start = Offset(bottleRight - 14f, y),
                    end = Offset(bottleRight + 8f, y),
                    strokeWidth = 2.5f
                )

                // Label on right
                drawContext.canvas.nativeCanvas.drawText(
                    "${ml}ml",
                    bottleRight + 14f,
                    y + 4f,
                    textPaint
                )
            }

            // Schedule Time labels along left edge corresponding to target ticks
            for (slot in scheduleSlots) {
                if (slot.levelMl > 0) {
                    val frac = (slot.levelMl.toFloat() / bottleCapacityMl).coerceIn(0f, 1f)
                    val y = bodyBottom - (bodyHeight * frac)

                    // Tick mark on left edge
                    drawLine(
                        color = Color(0xFF0284C7),
                        start = Offset(bottleLeft - 8f, y),
                        end = Offset(bottleLeft + 12f, y),
                        strokeWidth = 2.5f
                    )

                    // Time label on left
                    drawContext.canvas.nativeCanvas.drawText(
                        slot.timeLabel,
                        bottleLeft - 14f,
                        y + 4f,
                        timePaint
                    )
                }
            }

            // Draw "Target Now" marker on the left of target line
            drawContext.canvas.nativeCanvas.drawText(
                "TARGET NOW",
                bottleLeft - 14f,
                targetY - 5f,
                targetTagPaint
            )
        }
    }
}
