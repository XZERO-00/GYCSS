package com.gycss.app.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.gycss.app.data.model.Role
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceManager @Inject constructor(@ApplicationContext private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("gycss_prefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_THEME_MODE = "theme_mode" // 0: System, 1: Light, 2: Dark
        const val KEY_USER_ROLE = "user_role"
        const val KEY_LANGUAGE = "selected_language"
        
        const val KEY_HELP_RADIUS = "help_radius"
        const val KEY_PROFILE_VISIBILITY = "profile_visibility"
        const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val KEY_SOS_SOUND_ENABLED = "sos_sound_enabled"
        const val KEY_LOCATION_TRACKING_ENABLED = "location_tracking_enabled"
        const val KEY_VOICE_SOS_ENABLED = "voice_sos_enabled"
        const val KEY_IS_FIRST_LOGIN = "is_first_login"
        const val KEY_IS_FIRST_APP_OPEN = "is_first_app_open"
        
        const val THEME_SYSTEM = 0
        const val THEME_LIGHT = 1
        const val THEME_DARK = 2

        const val LANG_ENGLISH = "en"
        const val LANG_MARATHI = "mr"
        const val LANG_HINDI = "hi"
        const val LANG_KANNADA = "kn"
    }

    fun getThemeMode(): Int = prefs.getInt(KEY_THEME_MODE, THEME_SYSTEM)

    fun setThemeMode(mode: Int) {
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply()
        applyTheme(mode)
    }

    fun applyTheme(mode: Int) {
        when (mode) {
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    fun saveUserRole(role: Role) {
        prefs.edit().putString(KEY_USER_ROLE, role.name).apply()
    }

    fun getUserRole(): Role? {
        val name = prefs.getString(KEY_USER_ROLE, null)
        return if (name != null) try { Role.valueOf(name) } catch (e: Exception) { null } else null
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

    // App Lifecycle Flags
    fun isFirstAppOpen(): Boolean = prefs.getBoolean(KEY_IS_FIRST_APP_OPEN, true)
    fun setFirstAppOpen(isFirst: Boolean) = prefs.edit().putBoolean(KEY_IS_FIRST_APP_OPEN, isFirst).apply()

    fun isFirstLogin(): Boolean = prefs.getBoolean(KEY_IS_FIRST_LOGIN, true)
    fun setFirstLogin(isFirst: Boolean) = prefs.edit().putBoolean(KEY_IS_FIRST_LOGIN, isFirst).apply()

    // Voice SOS Settings
    fun isVoiceSosEnabled(): Boolean = prefs.getBoolean(KEY_VOICE_SOS_ENABLED, false)
    fun setVoiceSosEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_VOICE_SOS_ENABLED, enabled).apply()

    // Volunteer Preferences
    fun getHelpRadius(): Float = prefs.getFloat(KEY_HELP_RADIUS, 5.0f)
    fun setHelpRadius(radius: Float) = prefs.edit().putFloat(KEY_HELP_RADIUS, radius).apply()

    fun isProfileVisible(): Boolean = prefs.getBoolean(KEY_PROFILE_VISIBILITY, true)
    fun setProfileVisibility(visible: Boolean) = prefs.edit().putBoolean(KEY_PROFILE_VISIBILITY, visible).apply()

    // Notification & Privacy Settings
    fun areNotificationsEnabled(): Boolean = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    fun setNotificationsEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()

    fun isSosSoundEnabled(): Boolean = prefs.getBoolean(KEY_SOS_SOUND_ENABLED, true)
    fun setSosSoundEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_SOS_SOUND_ENABLED, enabled).apply()

    fun isLocationTrackingEnabled(): Boolean = prefs.getBoolean(KEY_LOCATION_TRACKING_ENABLED, true)
    fun setLocationTrackingEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_LOCATION_TRACKING_ENABLED, enabled).apply()

    fun clearSession() {
        prefs.edit().remove(KEY_USER_ROLE).apply()
    }
}
