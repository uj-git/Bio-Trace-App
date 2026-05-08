package com.umang.biotrace.presentation.components

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun PalmOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(Color.Black.copy(alpha = 0.45f))
        val ovalWidth = size.width * 0.72f
        val ovalHeight = size.height * 0.46f
        val left = (size.width - ovalWidth) / 2f
        val top = (size.height - ovalHeight) / 2f
        drawOval(
            color = Color.White,
            topLeft = Offset(left, top),
            size = Size(ovalWidth, ovalHeight),
            style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
        )
        drawPalmGuides(left, top, ovalWidth, ovalHeight)
    }
}

@Composable
fun FingerOverlay(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {

        // Blurred background layer (API 31+), plain dim on older devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        renderEffect = BlurEffect(
                            radiusX = 18f,
                            radiusY = 18f,
                            edgeTreatment = TileMode.Clamp
                        )
                        alpha = 0.85f
                    }
                    .background(Color.Black.copy(alpha = 0.35f))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
            )
        }

        // Clear oval cutout drawn on top
        Canvas(modifier = Modifier.fillMaxSize()) {
            val ovalWidth = size.width * 0.44f
            val ovalHeight = size.height * 0.42f
            val left = (size.width - ovalWidth) / 2f
            val top = (size.height - ovalHeight) / 2f

            // Punch a transparent hole in the dim layer so the camera shows through
            drawOval(
                color = Color.Transparent,
                topLeft = Offset(left, top),
                size = Size(ovalWidth, ovalHeight),
                blendMode = BlendMode.Clear
            )

            // White guide border
            drawOval(
                color = Color.White,
                topLeft = Offset(left, top),
                size = Size(ovalWidth, ovalHeight),
                style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPalmGuides(
    left: Float,
    top: Float,
    ovalWidth: Float,
    ovalHeight: Float
) {
    val path = Path().apply {
        val fingerTop = top - ovalHeight * 0.18f
        val fingerBottom = top + ovalHeight * 0.24f
        val gap = ovalWidth / 6f
        repeat(5) { index ->
            val x = left + gap * (index + 1)
            addOval(Rect(x - gap * 0.24f, fingerTop, x + gap * 0.24f, fingerBottom))
        }
    }
    drawPath(path, Color.White.copy(alpha = 0.75f), style = Stroke(width = 3.dp.toPx()))
}
