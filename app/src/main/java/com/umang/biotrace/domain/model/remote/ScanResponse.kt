package com.umang.biotrace.domain.model.remote

data class ScanResponse(
    val id: Long,
    val handSide: String,
    val palmImagePath: String?,
    val fingerImagePaths: String?,
    val brightnessScore: Float?,
    val blurScore: Float?,
    val focusDistance: Float?,
    val lightType: String?,
    val deviceId: String?,
    val capturedAt: String?
)