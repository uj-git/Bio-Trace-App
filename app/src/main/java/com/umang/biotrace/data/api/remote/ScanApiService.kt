package com.umang.biotrace.data.api.remote

import com.umang.biotrace.domain.model.remote.ScanResponse
import com.umang.biotrace.domain.model.remote.ScanUploadRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


interface ScanApiService {

    @POST("api/scans")
    suspend fun uploadScan(@Body request: ScanUploadRequest): Response<ScanResponse>

    @GET("api/scans")
    suspend fun getAllScans(): Response<List<ScanResponse>>

    @GET("api/scans/{id}")
    suspend fun getScanById(@Path("id") id: Long): Response<ScanResponse>
}