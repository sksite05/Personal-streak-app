package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random

data class ConfettiParticle(
    val initialX: Float,
    val initialY: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val size: Float,
    val rotationSpeed: Float,
    val isCircle: Boolean
)

@Composable
fun ConfettiEffect(
    trigger: Boolean,
    onFinished: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (!trigger) return

    val progress = remember { Animatable(0f) }

    val particles = remember {
        val colors = listOf(
            Color(0xFFF97316), // Flame Amber
            Color(0xFFFBBF24), // Flame Gold
            Color(0xFF06B6D4), // Cyan
            Color(0xFF38BDF8), // Light Sky
            Color(0xFF10B981), // Emerald
            Color(0xFF8B5CF6), // Violet
            Color(0xFFEC4899)  // Pink
        )
        List(65) {
            val angle = Random.nextFloat() * Math.PI.toFloat() * 2f
            val speed = Random.nextFloat() * 450f + 250f
            ConfettiParticle(
                initialX = 0.5f,
                initialY = 0.45f,
                vx = kotlin.math.cos(angle) * speed,
                vy = kotlin.math.sin(angle) * speed - 200f,
                color = colors[Random.nextInt(colors.size)],
                size = Random.nextFloat() * 10f + 8f,
                rotationSpeed = Random.nextFloat() * 720f - 360f,
                isCircle = Random.nextBoolean()
            )
        }
    }

    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1800, easing = LinearEasing)
        )
        onFinished()
    }

    val p = progress.value
    if (p < 1f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            for (particle in particles) {
                val t = p * 1.5f
                val x = w * particle.initialX + particle.vx * t
                val y = h * particle.initialY + particle.vy * t + 0.5f * 980f * t * t
                val alpha = (1f - p).coerceIn(0f, 1f)
                val rot = particle.rotationSpeed * t

                if (x in -50f..(w + 50f) && y in -50f..(h + 50f)) {
                    rotate(rot, pivot = Offset(x, y)) {
                        if (particle.isCircle) {
                            drawCircle(
                                color = particle.color.copy(alpha = alpha),
                                radius = particle.size / 2f,
                                center = Offset(x, y)
                            )
                        } else {
                            drawRect(
                                color = particle.color.copy(alpha = alpha),
                                topLeft = Offset(x - particle.size / 2f, y - particle.size / 3f),
                                size = Size(particle.size, particle.size * 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}
