package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun StreakFlame(
    streakDays: Int,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "flamePulse")

    // Pulsing scale
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Flicker angle
    val flickerAngle by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flickerAngle"
    )

    // Flame color palette based on streak milestones
    val (flameColors, innerColors, auraColor) = when {
        streakDays >= 30 -> Triple(
            listOf(Color(0xFF8B5CF6), Color(0xFF06B6D4), Color(0xFF38BDF8)),
            listOf(Color(0xFFEDE9FE), Color(0xFFA78BFA)),
            Color(0x408B5CF6)
        )
        streakDays >= 14 -> Triple(
            listOf(Color(0xFFEF4444), Color(0xFFF97316), Color(0xFFFBBF24)),
            listOf(Color(0xFFFEF3C7), Color(0xFFFDE047)),
            Color(0x33EF4444)
        )
        streakDays >= 7 -> Triple(
            listOf(Color(0xFFF97316), Color(0xFFFBBF24)),
            listOf(Color(0xFFFFFBEB), Color(0xFFFDE047)),
            Color(0x28F97316)
        )
        streakDays >= 3 -> Triple(
            listOf(Color(0xFFF97316), Color(0xFFFB923C)),
            listOf(Color(0xFFFED7AA), Color(0xFFFDBA74)),
            Color(0x18F97316)
        )
        else -> Triple(
            listOf(Color(0xFFFB923C), Color(0xFFFDBA74)),
            listOf(Color(0xFFFFEDD5), Color(0xFFFED7AA)),
            Color(0x00000000)
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .scale(if (streakDays > 0) pulseScale else 1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // Outer Aura Glow for high streaks
            if (streakDays >= 7) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(auraColor, Color.Transparent),
                        center = Offset(w * 0.5f, h * 0.55f),
                        radius = w * 0.65f
                    )
                )
            }

            // Outer Flame Path
            val outerFlame = Path().apply {
                val tipX = w * 0.5f + (flickerAngle * 1.5f)
                val tipY = h * 0.12f

                moveTo(tipX, tipY)
                // Right curve down
                cubicTo(
                    w * 0.72f, h * 0.28f,
                    w * 0.88f, h * 0.52f,
                    w * 0.82f, h * 0.74f
                )
                cubicTo(
                    w * 0.78f, h * 0.88f,
                    w * 0.62f, h * 0.94f,
                    w * 0.50f, h * 0.94f
                )
                // Left curve down
                cubicTo(
                    w * 0.38f, h * 0.94f,
                    w * 0.22f, h * 0.88f,
                    w * 0.18f, h * 0.74f
                )
                cubicTo(
                    w * 0.12f, h * 0.52f,
                    w * 0.28f, h * 0.28f,
                    tipX, tipY
                )
                close()
            }

            drawPath(
                path = outerFlame,
                brush = Brush.verticalGradient(
                    colors = flameColors,
                    startY = h * 0.1f,
                    endY = h * 0.95f
                )
            )

            // Inner Flame Path (Intense Core)
            val innerFlame = Path().apply {
                val innerTipX = w * 0.5f + (flickerAngle * 0.8f)
                val innerTipY = h * 0.45f

                moveTo(innerTipX, innerTipY)
                cubicTo(
                    w * 0.65f, h * 0.55f,
                    w * 0.68f, h * 0.72f,
                    w * 0.62f, h * 0.82f
                )
                cubicTo(
                    w * 0.58f, h * 0.88f,
                    w * 0.54f, h * 0.90f,
                    w * 0.50f, h * 0.90f
                )
                cubicTo(
                    w * 0.46f, h * 0.90f,
                    w * 0.42f, h * 0.88f,
                    w * 0.38f, h * 0.82f
                )
                cubicTo(
                    w * 0.32f, h * 0.72f,
                    w * 0.35f, h * 0.55f,
                    innerTipX, innerTipY
                )
                close()
            }

            drawPath(
                path = innerFlame,
                brush = Brush.verticalGradient(
                    colors = innerColors,
                    startY = h * 0.45f,
                    endY = h * 0.90f
                )
            )

            // Tiny floating spark particles around legendary flame
            if (streakDays >= 14) {
                val sparks = listOf(
                    Offset(w * 0.25f, h * 0.35f),
                    Offset(w * 0.75f, h * 0.30f),
                    Offset(w * 0.52f, h * 0.08f)
                )
                for (s in sparks) {
                    drawCircle(
                        color = Color(0xFFFFE066),
                        radius = 2.5f,
                        center = s
                    )
                }
            }
        }
    }
}
