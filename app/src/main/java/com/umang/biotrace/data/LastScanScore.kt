package com.umang.biotrace.data

import android.content.Context
import com.umang.biotrace.domain.model.remote.ScanResponse
import org.json.JSONObject

class LastScanStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun save(scan: ScanResponse) {
        prefs.edit().putString(
            KEY, JSONObject()
                .put("id",              scan.id)
                .put("handSide",        scan.handSide)
                .put("palmImagePath",   scan.palmImagePath ?: "")
                .put("brightnessScore", scan.brightnessScore ?: 0f)
                .put("blurScore",       scan.blurScore ?: 0f)
                .put("focusDistance",   scan.focusDistance ?: 0f)
                .put("lightType",       scan.lightType ?: "")
                .put("deviceId",        scan.deviceId ?: "")
                .put("capturedAt",      scan.capturedAt ?: "")
                .put("fingerCount",     scan.fingerImagePaths
                    ?.split(",")
                    ?.filter { it.isNotBlank() }
                    ?.size ?: 0)
                .toString()
        ).apply()
    }

    fun load(): LastScanSummary? {
        val json = prefs.getString(KEY, null) ?: return null
        return runCatching {
            val obj = JSONObject(json)
            LastScanSummary(
                id              = obj.getLong("id"),
                handSide        = obj.getString("handSide"),
                brightnessScore = obj.getDouble("brightnessScore").toFloat(),
                blurScore       = obj.getDouble("blurScore").toFloat(),
                focusDistance   = obj.getDouble("focusDistance").toFloat(),
                lightType       = obj.getString("lightType"),
                deviceId        = obj.getString("deviceId"),
                capturedAt      = obj.getString("capturedAt"),
                fingerCount     = obj.getInt("fingerCount")
            )
        }.getOrNull()
    }

    fun clear() = prefs.edit().remove(KEY).apply()

    companion object {
        private const val PREF_NAME = "last_scan_store"
        private const val KEY       = "last_scan"
    }
}

data class LastScanSummary(
    val id: Long,
    val handSide: String,
    val brightnessScore: Float,
    val blurScore: Float,
    val focusDistance: Float,
    val lightType: String,
    val deviceId: String,
    val capturedAt: String,
    val fingerCount: Int
)