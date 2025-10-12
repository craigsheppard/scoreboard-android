package com.scoreboard.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.core.content.res.ResourcesCompat
import com.scoreboard.R

@Composable
fun OutlinedText(
    text: String,
    fontFamily: FontFamily,
    fontSize: Float,
    textColor: Color,
    strokeColor: Color,
    strokeWidth: Float,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        drawIntoCanvas { canvas ->
            val paint = Paint().asFrameworkPaint()

            // Load the Jersey M54 font
            paint.typeface = try {
                ResourcesCompat.getFont(context, R.font.jersey_m54)
            } catch (e: Exception) {
                android.graphics.Typeface.DEFAULT
            }

            // Set text size
            paint.textSize = fontSize

            // Measure text
            val textWidth = paint.measureText(text)

            // Calculate centered position
            val x = (canvasWidth - textWidth) / 2f
            val y = canvasHeight / 2f + (fontSize / 3f)

            // Draw stroke (outline)
            paint.style = android.graphics.Paint.Style.STROKE
            paint.strokeWidth = strokeWidth
            paint.color = strokeColor.toArgb()
            paint.strokeJoin = android.graphics.Paint.Join.ROUND
            paint.strokeCap = android.graphics.Paint.Cap.ROUND
            canvas.nativeCanvas.drawText(text, x, y, paint)

            // Draw fill (main text)
            paint.style = android.graphics.Paint.Style.FILL
            paint.color = textColor.toArgb()
            canvas.nativeCanvas.drawText(text, x, y, paint)
        }
    }
}
