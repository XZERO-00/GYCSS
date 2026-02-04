package com.gycss.app.ui.senior.settings

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gycss.app.data.local.PreferenceManager
import com.gycss.app.databinding.ActivitySettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private var isInitialSelection = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupLanguageSpinner()
        setupListeners()
    }

    private fun setupLanguageSpinner() {
        val currentLang = preferenceManager.getLanguage()
        val position = when (currentLang) {
            PreferenceManager.LANG_ENGLISH -> 0
            PreferenceManager.LANG_HINDI -> 1
            PreferenceManager.LANG_MARATHI -> 2
            PreferenceManager.LANG_KANNADA -> 3
            else -> 0
        }
        binding.spinnerLanguage.setSelection(position)

        binding.spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isInitialSelection) {
                    isInitialSelection = false
                    return
                }

                val langCode = when (position) {
                    0 -> PreferenceManager.LANG_ENGLISH
                    1 -> PreferenceManager.LANG_HINDI
                    2 -> PreferenceManager.LANG_MARATHI
                    3 -> PreferenceManager.LANG_KANNADA
                    else -> PreferenceManager.LANG_ENGLISH
                }

                if (langCode != preferenceManager.getLanguage()) {
                    preferenceManager.saveLanguage(langCode)
                    // The activity will be recreated automatically by AppCompatDelegate.setApplicationLocales
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.swNotifications.setOnCheckedChangeListener { _, isChecked ->
            val msg = if (isChecked) "Notifications Enabled" else "Notifications Disabled"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        binding.swLocationTracking.setOnCheckedChangeListener { _, isChecked ->
            val msg = if (isChecked) "Location Tracking Enabled" else "Location Tracking Disabled"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        binding.btnPrivacy.setOnClickListener {
            Toast.makeText(this, "Privacy Policy clicked", Toast.LENGTH_SHORT).show()
        }

        binding.btnSupport.setOnClickListener {
            Toast.makeText(this, "Contact Support clicked", Toast.LENGTH_SHORT).show()
        }

        binding.btnLogout.setOnClickListener {
            Toast.makeText(this, "Logged Out Successfully", Toast.LENGTH_SHORT).show()
            // In real app: auth.signOut() and navigate to Login
            finishAffinity()
        }
    }
}
