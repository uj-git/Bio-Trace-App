package com.umang.biotrace.data.repository.remote

import com.umang.biotrace.data.api.remote.ScanApiService
import com.umang.biotrace.domain.model.remote.ScanResponse
import com.umang.biotrace.domain.model.remote.ScanUploadRequest


sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Error(val message: String) : ApiResult<Nothing>
}

class ScanRepository(private val api: ScanApiService) {

    // Upload a completed scan session to the backend
    suspend fun uploadScan(request: ScanUploadRequest): ApiResult<ScanResponse> {
        return try {
            val response = api.uploadScan(request)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Upload failed: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.message}")
        }
    }

    // Fetch the list of all scans
    suspend fun getAllScans(): ApiResult<List<ScanResponse>> {
        return try {
            val response = api.getAllScans()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Fetch failed: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.message}")
        }
    }

    // Fetch details of a single scan by id
    suspend fun getScanById(id: Long): ApiResult<ScanResponse> {
        return try {
            val response = api.getScanById(id)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Not found: ${response.code()}")
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.message}")
        }
    }
}