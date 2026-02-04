package com.gycss.app.data.local

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.gycss.app.data.model.UserType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceManager @Inject constructor(@ApplicationContext private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("gycss_prefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_DARK_MODE = "dark_mode_enabled"
        const val KEY_USER_TYPE = "user_type"
        const val KEY_LANGUAGE = "selected_language"
        
        const val LANG_ENGLISH = "en"
        const val LANG_MARATHI = "mr"
        const val LANG_HINDI = "hi"
        const val LANG_KANNADA = "kn"
    }

    fun isDarkModeEnabled(): Boolean = prefs.getBoolean(KEY_DARK_MODE, false)

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

    fun saveLanguage(langCode: String) {
        prefs.edit().putString(KEY_LANGUAGE, langCode).apply()
        applyLanguage(langCode)
    }

    fun getLanguage(): String = prefs.getString(KEY_LANGUAGE, LANG_ENGLISH) ?: LANG_ENGLISH

    fun applyLanguage(langCode: String) {
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(langCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    fun clearSession() {
        prefs.edit().remove(KEY_USER_TYPE).apply()
    }
}
