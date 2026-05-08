package com.umang.biotrace.camera

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.umang.biotrace.domain.model.FingerType
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
            fingerCount = countRaisedFingers(landmarks, handedness),
            detectedFinger = detectSingleRaisedFinger(landmarks, handedness),
            isDorsal = isDorsalSide(landmarks, handedness)
        )
    }

    override fun close() {
        handLandmarker.close()
    }

    /**
     * Detects whether the dorsal (back) side of the hand is facing the camera.
     *
     * MediaPipe's z-axis is depth relative to the wrist — negative z means closer
     * to the camera. On a palm-facing hand the finger tips have lower (more negative)
     * z than the knuckle MCPs. On a dorsal-facing hand this relationship flips.
     *
     * We also cross-check handedness: MediaPipe labels the hand as seen from the
     * camera. A "Right" label on screen means the hand's own right side is visible,
     * which for a normally-held right hand means it IS the palm side. If the person
     * flips their hand the label stays the same but the z-relationship reverses,
     * giving us the dorsal signal.
     */
    private fun isDorsalSide(landmarks: List<NormalizedLandmark>, handedness: String?): Boolean {
        if (landmarks.size < 21) return false

        // Compare z of finger tips vs their MCP (knuckle) bases.
        // On a palm-facing hand: tip.z < mcp.z  (tips are closer to camera)
        // On a dorsal-facing hand: tip.z > mcp.z (knuckles are closer)
        val tipMcpPairs = listOf(
            INDEX_TIP to INDEX_MCP,
            MIDDLE_TIP to MIDDLE_MCP,
            RING_TIP to RING_MCP,
            LITTLE_TIP to LITTLE_MCP
        )

        val dorsalVotes = tipMcpPairs.count { (tip, mcp) ->
            landmarks[tip].z() > landmarks[mcp].z()
        }

        // Require majority (3 of 4) to reduce noise from bent fingers
        return dorsalVotes >= 3
    }

    private fun countRaisedFingers(
        landmarks: List<NormalizedLandmark>,
        handedness: String?
    ): Int {
        return raisedFingers(landmarks, handedness).size
    }

    private fun detectSingleRaisedFinger(
        landmarks: List<NormalizedLandmark>,
        handedness: String?
    ): FingerType? {
        val raisedFingers = raisedFingers(landmarks, handedness)
        return raisedFingers.singleOrNull()
    }

    private fun raisedFingers(
        landmarks: List<NormalizedLandmark>,
        handedness: String?
    ): List<FingerType> {
        if (landmarks.size < 21) return emptyList()

        val raised = mutableListOf<FingerType>()

        val thumbRaised = if (handedness.equals("Right", ignoreCase = true)) {
            landmarks[THUMB_TIP].x() > landmarks[THUMB_IP].x()
        } else {
            landmarks[THUMB_TIP].x() < landmarks[THUMB_IP].x()
        }
        if (thumbRaised) raised += FingerType.Thumb

        listOf(
            FingerType.Index to (INDEX_TIP to INDEX_PIP),
            FingerType.Middle to (MIDDLE_TIP to MIDDLE_PIP),
            FingerType.Ring to (RING_TIP to RING_PIP),
            FingerType.Little to (LITTLE_TIP to LITTLE_PIP)
        ).forEach { (finger, points) ->
            val (tip, pip) = points
            if (landmarks[tip].y() < landmarks[pip].y()) {
                raised += finger
            }
        }

        return raised
    }

    companion object {
        private const val MODEL_NAME = "hand_landmarker.task"

        // Finger tips
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

        // MCP knuckles (used for dorsal detection)
        private const val INDEX_MCP = 5
        private const val MIDDLE_MCP = 9
        private const val RING_MCP = 13
        private const val LITTLE_MCP = 17

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
    val fingerCount: Int,
    val detectedFinger: FingerType?,
    val isDorsal: Boolean = false
)
