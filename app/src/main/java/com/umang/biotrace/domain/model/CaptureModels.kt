package com.umang.biotrace.domain.model

enum class HandSide(val label: String) {
    Left("Left Hand"),
    Right("Right Hand")
}

enum class FingerType(val label: String, val filePart: String) {
    Thumb("Thumb", "Thumb_Finger"),
    Index("Index", "Index_Finger"),
    Middle("Middle", "Middle_Finger"),
    Ring("Ring", "Ring_Finger"),
    Little("Little", "Little_Finger")
}

enum class LightType(val label: String) {
    Low("Low light"),
    Normal("Normal light"),
    Bright("Bright light")
}

data class FrameAnalysis(
    val brightnessScore: Float = 0f,
    val blurScore: Float = 0f,
    val lightType: LightType = LightType.Normal,
    val estimatedHandSide: HandSide = HandSide.Left,
    val dorsalDetected: Boolean = false
)

data class CameraMetrics(
    val deviceId: String,
    val brightnessScore: Float,
    val lightType: LightType,
    val cameraType: String,
    val focalLength: Float,
    val aperture: Float,
    val focusDistance: Float,
    val blurScore: Float
)

data class MinutiaeRecord(
    val handSide: HandSide,
    val fingerType: FingerType,
    val token: String
)

data class CaptureResult(
    val palmPath: String? = null,
    val fingerPaths: List<String> = emptyList(),
    val lastMetrics: CameraMetrics? = null
)
