package com.umang.biotrace.domain.model.remote

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ScanUploadRequest(
    val handSide: String,
    val palmImagePath: String?,
    val fingerImagePaths: List<String>,
    val brightnessScore: Float,
    val blurScore: Float,
    val focusDistance: Float,
    val lightType: String,
    val deviceId: String
) {
    fun toScanResponseLocal() = ScanResponse(
            id = 1L,
            handSide = this.handSide,
            palmImagePath = this.palmImagePath,
            fingerImagePaths = this.fingerImagePaths.joinToString(),
            brightnessScore = this.brightnessScore,
            blurScore = this.blurScore,
            focusDistance = this.focusDistance,
            lightType = this.lightType,
            deviceId = this.deviceId,
            capturedAt = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault()).format(Date())
    )

}
