package com.gycss.app.ui.senior.settings

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gycss.app.R
import com.gycss.app.data.local.PreferenceManager
import com.gycss.app.data.model.Role
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
        setupDarkMode()
        setupRoleSpecificSettings()
        setupListeners()
    }

    private fun setupDarkMode() {
        binding.swDarkMode.isChecked = preferenceManager.isDarkModeEnabled()
        binding.swDarkMode.setOnCheckedChangeListener { _, isChecked ->
            preferenceManager.setDarkMode(isChecked)
        }
    }

    private fun setupRoleSpecificSettings() {
        val role = preferenceManager.getUserRole()
        if (role == Role.SENIOR) {
            binding.llSeniorSettings.visibility = View.VISIBLE
            binding.llVolunteerSettings.visibility = View.GONE
        } else if (role == Role.VOLUNTEER) {
            binding.llSeniorSettings.visibility = View.GONE
            binding.llVolunteerSettings.visibility = View.VISIBLE
        }
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
            val msg = if (isChecked) getString(R.string.notif_enabled) else getString(R.string.notif_disabled)
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        binding.btnPrivacy.setOnClickListener {
            Toast.makeText(this, "Privacy Policy clicked", Toast.LENGTH_SHORT).show()
        }

        binding.btnSupport.setOnClickListener {
            Toast.makeText(this, "Contact Support clicked", Toast.LENGTH_SHORT).show()
        }

        binding.btnLogout.setOnClickListener {
            Toast.makeText(this, getString(R.string.logged_out_msg), Toast.LENGTH_SHORT).show()
            // sign out logic would go here
            finishAffinity()
        }
        
        binding.btnEmergencyContacts.setOnClickListener {
            Toast.makeText(this, "Manage Contacts coming soon", Toast.LENGTH_SHORT).show()
        }
        
        binding.btnVolunteerStats.setOnClickListener {
            Toast.makeText(this, "Detailed Analytics coming soon", Toast.LENGTH_SHORT).show()
        }
    }
}
