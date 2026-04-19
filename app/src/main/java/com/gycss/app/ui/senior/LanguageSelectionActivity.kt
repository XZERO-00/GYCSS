package com.gycss.app.ui.senior

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.gycss.app.data.local.PreferenceManager
import com.gycss.app.data.model.Role
import com.gycss.app.data.repository.AuthRepository
import com.gycss.app.databinding.ActivityLanguageSelectionBinding
import com.gycss.app.ui.auth.RoleSelectionActivity
import com.gycss.app.ui.volunteer.VolunteerDashboardActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LanguageSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLanguageSelectionBinding

    @Inject
    lateinit var preferenceManager: PreferenceManager

    @Inject
    lateinit var authRepository: AuthRepository

    private var selectedLanguage: String = PreferenceManager.LANG_ENGLISH
    private var isFromOnboarding = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguageSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isFromOnboarding = intent.getBooleanExtra("IS_ONBOARDING", false)

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        selectedLanguage = preferenceManager.getLanguage()
        updateSelectionUI()
        
        if (isFromOnboarding) {
            binding.ivBack.visibility = View.GONE
        }
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener { finish() }

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
            if (isFromOnboarding) {
                if (authRepository.isUserLoggedIn()) {
                    // User just logged in/registered and completed onboarding
                    preferenceManager.setFirstLogin(false)
                    
                    val role = preferenceManager.getUserRole()
                    val intent = when (role) {
                        Role.SENIOR -> Intent(this, SeniorDashboardActivity::class.java)
                        Role.VOLUNTEER -> Intent(this, VolunteerDashboardActivity::class.java)
                        else -> Intent(this, RoleSelectionActivity::class.java)
                    }
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    // First time app open flow, go to role selection
                    preferenceManager.setFirstAppOpen(false)
                    startActivity(Intent(this, RoleSelectionActivity::class.java))
                    finish()
                }
            } else {
                // User changed language from settings, go back to dashboard
                val role = preferenceManager.getUserRole()
                val intent = when (role) {
                    Role.VOLUNTEER -> Intent(this, VolunteerDashboardActivity::class.java)
                    else -> Intent(this, SeniorDashboardActivity::class.java)
                }
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    private fun changeLanguage(langCode: String) {
        if (selectedLanguage != langCode) {
            selectedLanguage = langCode
            preferenceManager.saveLanguage(langCode)
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
