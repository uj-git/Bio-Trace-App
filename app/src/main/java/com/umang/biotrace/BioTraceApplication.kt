package com.umang.biotrace

import android.app.Application
import com.umang.biotrace.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BioTraceApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BioTraceApplication)
            modules(appModule)
        }
    }
}
