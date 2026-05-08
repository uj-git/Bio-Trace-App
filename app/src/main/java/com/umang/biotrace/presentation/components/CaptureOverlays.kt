package com.umang.biotrace.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun PalmOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(Color.Black.copy(alpha = 0.45f))
        val ovalWidth = size.width * 0.82f
        val ovalHeight = size.height * 0.50f
        val left = (size.width - ovalWidth) / 2f
        val top = (size.height - ovalHeight) / 2f
        drawOval(
            color = Color.White,
            topLeft = Offset(left, top),
            size = Size(ovalWidth, ovalHeight),
            style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun FingerOverlay(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val ovalWidth = size.width * 0.44f
            val ovalHeight = size.height * 0.42f
            val left = (size.width - ovalWidth) / 2f
            val top = (size.height - ovalHeight) / 2f
            val guidePath = Path().apply {
                fillType = PathFillType.EvenOdd
                addRect(Rect(0f, 0f, size.width, size.height))
                addOval(Rect(left, top, left + ovalWidth, top + ovalHeight))
            }

            drawPath(guidePath, Color.Black.copy(alpha = 0.34f))

            drawOval(
                color = Color.White,
                topLeft = Offset(left, top),
                size = Size(ovalWidth, ovalHeight),
                style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}
