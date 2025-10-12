package com.scoreboard.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.scoreboard.data.models.ScoreSide

@Composable
fun BasketballTargets(
    side: ScoreSide,
    twoPointHit: Boolean,
    threePointHit: Boolean,
    modifier: Modifier = Modifier
) {
    val density = androidx.compose.ui.platform.LocalDensity.current

    // Pulse animation for targets
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // 2-Point target position (same row as 3-point)
        val twoPointPos = Offset(
            x = canvasWidth / 2,
            y = canvasHeight * 0.15f
        )

        // 3-Point target position (corner based on side)
        val threePointPos = when (side) {
            ScoreSide.LEFT -> Offset(
                x = canvasWidth * 0.15f,
                y = canvasHeight * 0.15f
            )
            ScoreSide.RIGHT -> Offset(
                x = canvasWidth * 0.85f,
                y = canvasHeight * 0.15f
            )
        }

        // Draw 2-Point target
        drawTarget(
            center = twoPointPos,
            points = "2",
            color = Color(0xFFFF9800),
            isHit = twoPointHit,
            scale = if (twoPointHit) 1.5f else pulseScale,
            density = density.density
        )

        // Draw 3-Point target
        drawTarget(
            center = threePointPos,
            points = "3",
            color = Color(0xFF4CAF50),
            isHit = threePointHit,
            scale = if (threePointHit) 1.5f else pulseScale,
            density = density.density
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTarget(
    center: Offset,
    points: String,
    color: Color,
    isHit: Boolean,
    scale: Float,
    density: Float
) {
    // iOS targets are 70pt diameter, which equals 70dp in Android
    // With density scaling, 70dp * density gives us the pixel size
    val baseRadius = 35f * density  // 70dp diameter / 2 = 35dp radius
    val radius = baseRadius * scale

    // Outer glow (slightly larger than main circle)
    val glowRadius = radius * 1.4f
    drawCircle(
        color = color.copy(alpha = 0.3f),
        radius = glowRadius,
        center = center
    )

    // Main circle
    drawCircle(
        color = Color.White.copy(alpha = 0.9f),
        radius = radius,
        center = center
    )

    // Border
    drawCircle(
        color = color,
        radius = radius,
        center = center,
        style = Stroke(width = 4f * scale * density)
    )

    // Points text (iOS uses 40pt text)
    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            this.color = color.toArgb()
            textSize = 40f * density * scale
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        drawText(
            points,
            center.x,
            center.y + (paint.textSize / 3),
            paint
        )
    }
}
