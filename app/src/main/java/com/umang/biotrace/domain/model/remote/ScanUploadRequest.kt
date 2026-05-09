package com.umang.biotrace.domain.model.remote

data class ScanUploadRequest(
    val handSide: String,
    val palmImagePath: String?,
    val fingerImagePaths: List<String>,
    val brightnessScore: Float,
    val blurScore: Float,
    val focusDistance: Float,
    val lightType: String,
    val deviceId: String
)
