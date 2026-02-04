package com.gycss.app.ui.senior

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gycss.app.data.local.PreferenceManager
import com.gycss.app.databinding.ActivityLanguageSelectionBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LanguageSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLanguageSelectionBinding

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private var selectedLanguage: String = PreferenceManager.LANG_ENGLISH

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguageSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        selectedLanguage = preferenceManager.getLanguage()
        updateSelectionUI()
        // No need to manually set text here if layout uses @string/ resources 
        // and we trigger a locale change that recreates the activity.
    }

    private fun setupListeners() {
        binding.cardEnglish.setOnClickListener {
            changeLanguage(PreferenceManager.LANG_ENGLISH)
        }

        binding.cardHindi.setOnClickListener {
            changeLanguage(PreferenceManager.LANG_HINDI)
        }

        binding.cardMarathi.setOnClickListener {
            changeLanguage(PreferenceManager.LANG_MARATHI)
        }

        binding.cardKannada.setOnClickListener {
            changeLanguage(PreferenceManager.LANG_KANNADA)
        }

        binding.btnConfirm.setOnClickListener {
            // Language is already applied, just navigate
            startActivity(Intent(this, SeniorDashboardActivity::class.java))
            finish()
        }
    }

    private fun changeLanguage(langCode: String) {
        if (selectedLanguage != langCode) {
            selectedLanguage = langCode
            preferenceManager.saveLanguage(langCode)
            // AppCompatDelegate.setApplicationLocales will recreate the activity 
            // automatically to apply the new resources.
            updateSelectionUI()
        }
    }

    private fun updateSelectionUI() {
        val cards = listOf(binding.cardEnglish, binding.cardHindi, binding.cardMarathi, binding.cardKannada)
        cards.forEach { it.strokeWidth = 0 }

        when (selectedLanguage) {
            PreferenceManager.LANG_ENGLISH -> binding.cardEnglish.strokeWidth = 4
            PreferenceManager.LANG_HINDI -> binding.cardHindi.strokeWidth = 4
            PreferenceManager.LANG_MARATHI -> binding.cardMarathi.strokeWidth = 4
            PreferenceManager.LANG_KANNADA -> binding.cardKannada.strokeWidth = 4
        }
    }
}
