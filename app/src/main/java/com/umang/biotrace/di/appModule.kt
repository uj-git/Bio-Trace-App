package com.umang.biotrace.di

import com.umang.biotrace.data.CameraInfoProvider
import com.umang.biotrace.data.CameraMetricStore
import com.umang.biotrace.data.HandDetectionEngine
import com.umang.biotrace.data.LastScanStore
import com.umang.biotrace.data.repository.local.ImageStorageRepository
import com.umang.biotrace.data.repository.remote.ScanRepository
import com.umang.biotrace.presentation.capture.CaptureViewModel
import com.umang.biotrace.presentation.homescreen.HomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { ImageStorageRepository(androidContext()) }
    single { CameraInfoProvider(androidContext()) }
    single { CameraMetricStore(androidContext()) }
    single { HandDetectionEngine() }
    single { LastScanStore(androidContext()) }
    single { ScanRepository(get()) }
    viewModel { CaptureViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { HomeViewModel(get(), get()) }
}
