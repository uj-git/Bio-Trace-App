package com.umang.biotrace.camera

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.umang.biotrace.domain.model.HandSide
import java.io.Closeable

class GoogleHandLandmarkerDetector private constructor(
    private val handLandmarker: HandLandmarker
) : Closeable {

    fun detect(bitmap: Bitmap): HandLandmarkAnalysis? {
        val result = handLandmarker.detect(BitmapImageBuilder(bitmap).build())
        val landmarks = result.landmarks().firstOrNull() ?: return null
        val handedness = result.handedness()
            .firstOrNull()
            ?.maxByOrNull { it.score() }
            ?.categoryName()

        return HandLandmarkAnalysis(
            handSide = if (handedness.equals("Right", ignoreCase = true)) HandSide.Right else HandSide.Left,
            fingerCount = countRaisedFingers(landmarks, handedness)
        )
    }

    override fun close() {
        handLandmarker.close()
    }

    private fun countRaisedFingers(
        landmarks: List<NormalizedLandmark>,
        handedness: String?
    ): Int {
        if (landmarks.size < 21) return 0

        val thumbRaised = if (handedness.equals("Right", ignoreCase = true)) {
            landmarks[THUMB_TIP].x() > landmarks[THUMB_IP].x()
        } else {
            landmarks[THUMB_TIP].x() < landmarks[THUMB_IP].x()
        }

        val raisedFingers = listOf(
            INDEX_TIP to INDEX_PIP,
            MIDDLE_TIP to MIDDLE_PIP,
            RING_TIP to RING_PIP,
            LITTLE_TIP to LITTLE_PIP
        ).count { (tip, pip) ->
            landmarks[tip].y() < landmarks[pip].y()
        }

        return raisedFingers + if (thumbRaised) 1 else 0
    }

    companion object {
        private const val MODEL_NAME = "hand_landmarker.task"
        private const val THUMB_TIP = 4
        private const val THUMB_IP = 3
        private const val INDEX_TIP = 8
        private const val INDEX_PIP = 6
        private const val MIDDLE_TIP = 12
        private const val MIDDLE_PIP = 10
        private const val RING_TIP = 16
        private const val RING_PIP = 14
        private const val LITTLE_TIP = 20
        private const val LITTLE_PIP = 18

        fun create(context: Context): GoogleHandLandmarkerDetector? {
            return runCatching {
                val baseOptions = BaseOptions.builder()
                    .setModelAssetPath(MODEL_NAME)
                    .build()
                val options = HandLandmarker.HandLandmarkerOptions.builder()
                    .setBaseOptions(baseOptions)
                    .setRunningMode(RunningMode.IMAGE)
                    .setNumHands(1)
                    .setMinHandDetectionConfidence(0.5f)
                    .setMinHandPresenceConfidence(0.5f)
                    .build()
                GoogleHandLandmarkerDetector(HandLandmarker.createFromOptions(context, options))
            }.getOrNull()
        }
    }
}

data class HandLandmarkAnalysis(
    val handSide: HandSide,
    val fingerCount: Int
)