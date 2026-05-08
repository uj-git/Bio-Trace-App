package com.umang.biotrace.data

import com.umang.biotrace.domain.model.FingerType
import com.umang.biotrace.domain.model.FrameAnalysis
import com.umang.biotrace.domain.model.HandSide
import com.umang.biotrace.domain.model.MinutiaeRecord

class HandDetectionEngine {

    fun extractPalmRecords(handSide: HandSide, analysis: FrameAnalysis): List<MinutiaeRecord> {
        return FingerType.entries.map { finger ->
            MinutiaeRecord(
                handSide = handSide,
                fingerType = finger,
                token = "${handSide.name}_${finger.name}_${(analysis.brightnessScore * 100).toInt()}"
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

        val record = records.firstOrNull { it.handSide == handSide && it.fingerType == expectedFinger }
            ?: return FingerValidation.NoMatch

        if (analysis.estimatedHandSide != handSide) {
            return FingerValidation.IncorrectFinger
        }

        return FingerValidation.Match(record.fingerType)
    }
}

sealed interface FingerValidation {
    data class Match(val fingerType: FingerType) : FingerValidation
    data object IncorrectFinger : FingerValidation
    data object NoMatch : FingerValidation
    data object Dorsal : FingerValidation
}