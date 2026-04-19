package com.gycss.app

import android.app.Application
import com.gycss.app.data.local.PreferenceManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class GycssApplication : Application() {

    @Inject
    lateinit var preferenceManager: PreferenceManager

    override fun onCreate() {
        super.onCreate()
        // Apply saved theme preference on app start
        preferenceManager.applyTheme(preferenceManager.getThemeMode())
        // Apply saved language preference on app start
        preferenceManager.applyLanguage(preferenceManager.getLanguage())
    }
}
