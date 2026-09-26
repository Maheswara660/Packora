package com.maheswara660.packora.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Custom 8-dot circular spinner based on React Loader component with pulsing dots.
 */
@Composable
fun PackoraDotLoader(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    durationMillis: Int = 900
) {
    val infiniteTransition = rememberInfiniteTransition(label = "PackoraDotLoaderTransition")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "DotProgress"
    )

    Canvas(
        modifier = modifier.size(size)
    ) {
        val totalDiameter = this.size.minDimension
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val dotMaxRadius = totalDiameter * 0.10f
        val orbitRadius = (totalDiameter / 2f) - dotMaxRadius

        for (i in 0 until 8) {
            val angleRad = (i * 45f) * (PI.toFloat() / 180f)
            val delayOffset = i / 8f
            val dotProgress = (progress - delayOffset + 1f) % 1f
            // Sine pulse between 0 and PI: peak at 0.5 (1.0), base at 0 and 1 (0.0)
            val wave = sin(dotProgress * PI.toFloat()).coerceIn(0f, 1f)

            val scale = 0.20f + (0.80f * wave)
            val alpha = (0.25f + (0.75f * wave)).coerceIn(0.1f, 1f)

            val x = center.x + orbitRadius * cos(angleRad)
            val y = center.y + orbitRadius * sin(angleRad)

            drawCircle(
                color = color.copy(alpha = alpha),
                radius = dotMaxRadius * scale,
                center = Offset(x, y)
            )
        }
    }
}
