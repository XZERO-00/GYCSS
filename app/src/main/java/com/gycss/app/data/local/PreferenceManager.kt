package com.gycss.app.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.gycss.app.data.model.UserType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceManager @Inject constructor(@ApplicationContext context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("gycss_prefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_DARK_MODE = "dark_mode_enabled"
        const val KEY_USER_TYPE = "user_type"
    }

    fun isDarkModeEnabled(): Boolean {
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
        applyTheme(enabled)
    }

    fun applyTheme(isDarkMode: Boolean) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    fun saveUserType(type: UserType) {
        prefs.edit().putString(KEY_USER_TYPE, type.name).apply()
    }

    fun getUserType(): UserType? {
        val name = prefs.getString(KEY_USER_TYPE, null)
        return if (name != null) UserType.valueOf(name) else null
    }

    fun clearSession() {
        prefs.edit().remove(KEY_USER_TYPE).apply()
    }
}