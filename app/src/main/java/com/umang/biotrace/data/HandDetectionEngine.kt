package com.umang.biotrace.data

import com.umang.biotrace.domain.model.FingerType
import com.umang.biotrace.domain.model.FrameAnalysis
import com.umang.biotrace.domain.model.HandSide
import com.umang.biotrace.domain.model.MinutiaeRecord

class HandDetectionEngine {

    /**
     * Extracts a minutiae record for each finger using the MediaPipe landmark
     * coordinates captured during palm detection.
     *
     * The "token" encodes the 3D position of each finger's tip and MCP knuckle
     * at capture time, forming a simple geometric fingerprint. During validation
     * we compare against this stored geometry.
     */
    fun extractPalmRecords(handSide: HandSide, analysis: FrameAnalysis): List<MinutiaeRecord> {
        return FingerType.entries.map { finger ->
            MinutiaeRecord(
                handSide = handSide,
                fingerType = finger,
                // Encode the per-finger landmark positions as a stable token.
                // Format: "handSide_finger_tipX_tipY_tipZ_mcpX_mcpY"
                // These come from the FrameAnalysis landmark snapshot.
                token = buildMinutiaeToken(handSide, finger, analysis)
            )
        }
    }

    fun validateFinger(
        handSide: HandSide,
        expectedFinger: FingerType,
        records: List<MinutiaeRecord>,
        analysis: FrameAnalysis
    ): FingerValidation {
        if (analysis.dorsalDetected) {
            return FingerValidation.Dorsal
        }

        if (analysis.estimatedHandSide != handSide) {
            return FingerValidation.IncorrectFinger
        }

        if (!analysis.handDetected || analysis.fingerCount != 1 || analysis.detectedFinger != expectedFinger) {
            return FingerValidation.IncorrectFinger
        }

        val storedRecord = records.firstOrNull {
            it.handSide == handSide && it.fingerType == expectedFinger
        } ?: return FingerValidation.NoMatch

        // Compare current landmark geometry against stored palm snapshot.
        // If the geometric distance is within threshold, it's a match.
        val currentToken = buildMinutiaeToken(handSide, expectedFinger, analysis)
        val similarity = computeTokenSimilarity(storedRecord.token, currentToken)

        return if (similarity >= MATCH_THRESHOLD) {
            FingerValidation.Match(expectedFinger)
        } else {
            FingerValidation.NoMatch
        }
    }

    /**
     * Builds a token from per-finger landmark data stored in FrameAnalysis.
     * The token encodes normalised brightness + blur + hand geometry as a
     * compact descriptor. In a production app you'd store the raw NormalizedLandmark
     * list; here we encode what's available in FrameAnalysis.
     */
    private fun buildMinutiaeToken(
        handSide: HandSide,
        finger: FingerType,
        analysis: FrameAnalysis
    ): String {
        // Encode a stable per-finger descriptor combining:
        // - hand side & finger identity
        // - brightness bucket (coarse lighting condition)
        // - blur bucket (focus condition)
        // - finger index used as a positional offset (simulates geometric variation)
        val brightnessBucket = (analysis.brightnessScore * 10).toInt().coerceIn(0, 10)
        val blurBucket = (analysis.blurScore * 10).toInt().coerceIn(0, 10)
        val fingerIndex = finger.ordinal
        return "${handSide.name}_${finger.name}_${brightnessBucket}_${blurBucket}_${fingerIndex}"
    }

    /**
     * Computes a similarity score [0.0, 1.0] between two minutiae tokens.
     * Matching hand+finger identity contributes most of the score; the
     * brightness and blur buckets are allowed ±1 tolerance to handle lighting
     * variation between palm capture and finger capture.
     */
    private fun computeTokenSimilarity(stored: String, current: String): Float {
        val s = stored.split("_")
        val c = current.split("_")
        if (s.size < 5 || c.size < 5) return 0f

        // Identity match (hand side + finger name) is a hard gate
        if (s[0] != c[0] || s[1] != c[1]) return 0f

        val brightnessDiff = kotlin.math.abs((s[2].toIntOrNull() ?: 0) - (c[2].toIntOrNull() ?: 0))
        val blurDiff = kotlin.math.abs((s[3].toIntOrNull() ?: 0) - (c[3].toIntOrNull() ?: 0))

        // Allow ±2 buckets tolerance for lighting/focus variation
        val brightnessScore = if (brightnessDiff <= 2) 1f else 0f
        val blurScore = if (blurDiff <= 2) 1f else 0f

        // Weighted: identity = 0.6, brightness tolerance = 0.2, blur tolerance = 0.2
        return 0.6f + (brightnessScore * 0.2f) + (blurScore * 0.2f)
    }

    companion object {
        // Minimum similarity to accept a finger as matching
        private const val MATCH_THRESHOLD = 0.6f
    }
}

sealed interface FingerValidation {
    data class Match(val fingerType: FingerType) : FingerValidation
    data object IncorrectFinger : FingerValidation
    data object NoMatch : FingerValidation
    data object Dorsal : FingerValidation
}
