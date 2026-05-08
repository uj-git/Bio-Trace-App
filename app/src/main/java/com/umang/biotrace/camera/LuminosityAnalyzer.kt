package com.umang.biotrace.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.umang.biotrace.domain.model.FrameAnalysis
import com.umang.biotrace.domain.model.HandSide
import com.umang.biotrace.domain.model.LightType
import kotlin.math.abs

class LuminosityAnalyzer(
    private val onFrameAnalyzed: (FrameAnalysis) -> Unit
) : ImageAnalysis.Analyzer {

    override fun analyze(image: ImageProxy) {
        val buffer = image.planes.first().buffer
        val data = ByteArray(buffer.remaining())
        buffer.get(data)

        var sum = 0L
        var leftSum = 0L
        var rightSum = 0L
        var edgeSum = 0L
        var edgeCount = 0
        val width = image.width

        data.forEachIndexed { index, byte ->
            val luminance = byte.toInt() and 0xFF
            sum += luminance
            if (index % width < width / 2) leftSum += luminance else rightSum += luminance
            if (index > 0 && index % width != 0) {
                edgeSum += abs(luminance - (data[index - 1].toInt() and 0xFF))
                edgeCount++
            }
        }

        val brightness = (sum.toFloat() / data.size) / MAX_LUMA
        val blurScore = ((edgeSum.toFloat() / edgeCount.coerceAtLeast(1)) / 32f).coerceIn(0f, 1f)
        val lightType = when {
            brightness < 0.28f -> LightType.Low
            brightness > 0.78f -> LightType.Bright
            else -> LightType.Normal
        }

        onFrameAnalyzed(
            FrameAnalysis(
                brightnessScore = brightness,
                blurScore = blurScore,
                lightType = lightType,
                estimatedHandSide = if (leftSum >= rightSum) HandSide.Left else HandSide.Right,
                dorsalDetected = blurScore < 0.12f && brightness > 0.35f
            )
        )
        image.close()
    }

    companion object {
        private const val MAX_LUMA = 255f
    }
}
