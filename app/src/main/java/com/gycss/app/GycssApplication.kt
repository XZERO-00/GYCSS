package com.gycss.app

import android.app.Application
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GycssApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Firebase is typically auto-initialized, but we can do it manually if needed
        // FirebaseApp.initializeApp(this)
    }
}