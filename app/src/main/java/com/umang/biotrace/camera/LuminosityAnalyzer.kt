package com.umang.biotrace.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.umang.biotrace.domain.model.FrameAnalysis
import com.umang.biotrace.domain.model.HandSide
import com.umang.biotrace.domain.model.LightType
import java.io.ByteArrayOutputStream
import kotlin.math.abs

class LuminosityAnalyzer(
    private val handDetector: GoogleHandLandmarkerDetector?,
    private val onFrameAnalyzed: (FrameAnalysis) -> Unit
) : ImageAnalysis.Analyzer {

    private var frameIndex = 0
    private var lastHandAnalysis: HandLandmarkAnalysis? = null

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
        val fallbackHandSide = if (leftSum >= rightSum) HandSide.Left else HandSide.Right

        val detector = handDetector
        if (detector != null && frameIndex % HAND_DETECTION_INTERVAL == 0) {
            lastHandAnalysis = runCatching {
                detector.detect(image.toBitmap())
            }.getOrNull()
        }
        frameIndex++

        onFrameAnalyzed(
            FrameAnalysis(
                brightnessScore = brightness,
                blurScore = blurScore,
                lightType = lightType,
                estimatedHandSide = lastHandAnalysis?.handSide ?: fallbackHandSide,
                dorsalDetected = lastHandAnalysis?.isDorsal ?: false,
                handDetected = lastHandAnalysis != null,
                fingerCount = lastHandAnalysis?.fingerCount ?: 0,
                detectedFinger = lastHandAnalysis?.detectedFinger,
                aiProvider = if (lastHandAnalysis != null) "Google MediaPipe Hand Landmarker" else "MediaPipe fallback"
            )
        )
        image.close()
    }

    private fun ImageProxy.toBitmap(): Bitmap {
        val nv21 = yuv420ToNv21()
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val jpeg = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 80, jpeg)
        val bitmap = BitmapFactory.decodeByteArray(jpeg.toByteArray(), 0, jpeg.size())
        val rotation = imageInfo.rotationDegrees
        if (rotation == 0) return bitmap
        val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun ImageProxy.yuv420ToNv21(): ByteArray {
        val yPlane = planes[0]
        val uPlane = planes[1]
        val vPlane = planes[2]
        val output = ByteArray(width * height * 3 / 2)
        var outputIndex = 0

        for (row in 0 until height) {
            val rowStart = row * yPlane.rowStride
            yPlane.buffer.position(rowStart)
            yPlane.buffer.get(output, outputIndex, width)
            outputIndex += width
        }

        val chromaHeight = height / 2
        val chromaWidth = width / 2
        for (row in 0 until chromaHeight) {
            for (col in 0 until chromaWidth) {
                val vIndex = row * vPlane.rowStride + col * vPlane.pixelStride
                val uIndex = row * uPlane.rowStride + col * uPlane.pixelStride
                output[outputIndex++] = vPlane.buffer.get(vIndex)
                output[outputIndex++] = uPlane.buffer.get(uIndex)
            }
        }

        return output
    }

    companion object {
        private const val MAX_LUMA = 255f
        private const val HAND_DETECTION_INTERVAL = 4
    }
}
