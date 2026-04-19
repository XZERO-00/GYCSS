package com.gycss.app.ui.volunteer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.gycss.app.R
import com.gycss.app.data.local.PreferenceManager
import com.gycss.app.databinding.FragmentVolunteerSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class VolunteerSettingsFragment : Fragment() {

    private var _binding: FragmentVolunteerSettingsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private var isInitialSelection = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVolunteerSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLanguageSpinner()
        setupThemeSpinner()
        setupVolunteerPreferences()
        setupSwitches()
        
        binding.btnLogout.setOnClickListener {
            (activity as? VolunteerDashboardActivity)?.logout()
        }
        
        binding.layoutPrivacy.setOnClickListener {
            Toast.makeText(requireContext(), "Privacy Policy coming soon", Toast.LENGTH_SHORT).show()
        }
        
        binding.layoutSupport.setOnClickListener {
            Toast.makeText(requireContext(), "Support contact: support@gycss.app", Toast.LENGTH_LONG).show()
        }

        binding.layoutQuietHours.setOnClickListener {
            Toast.makeText(requireContext(), "Quiet Hours feature coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.layoutChangePassword.setOnClickListener {
            Toast.makeText(requireContext(), "Change Password feature coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.layoutUpdatePhone.setOnClickListener {
            Toast.makeText(requireContext(), "Update Phone feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupLanguageSpinner() {
        val languages = arrayOf("English", "Hindi", "Marathi", "Kannada")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLanguage.adapter = adapter

        val currentLang = preferenceManager.getLanguage()
        val position = when (currentLang) {
            PreferenceManager.LANG_ENGLISH -> 0
            PreferenceManager.LANG_HINDI -> 1
            PreferenceManager.LANG_MARATHI -> 2
            PreferenceManager.LANG_KANNADA -> 3
            else -> 0
        }
        binding.spinnerLanguage.setSelection(position, false)

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
                    Toast.makeText(requireContext(), "Language changed to ${languages[position]}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupThemeSpinner() {
        val themes = arrayOf("System Default", "Light", "Dark")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, themes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTheme.adapter = adapter

        val currentTheme = preferenceManager.getThemeMode()
        binding.spinnerTheme.setSelection(currentTheme, false)

        binding.spinnerTheme.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position != preferenceManager.getThemeMode()) {
                    preferenceManager.setThemeMode(position)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupVolunteerPreferences() {
        // Radius Slider
        binding.sliderRadius.value = preferenceManager.getHelpRadius()
        binding.sliderRadius.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                preferenceManager.setHelpRadius(value)
            }
        }

        // Visibility Switch
        binding.swVisibility.isChecked = preferenceManager.isProfileVisible()
        binding.swVisibility.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            preferenceManager.setProfileVisibility(isChecked)
        }
    }

    private fun setupSwitches() {
        binding.swNotifications.isChecked = preferenceManager.areNotificationsEnabled()
        binding.swSosSound.isChecked = preferenceManager.isSosSoundEnabled()
        binding.swLocation.isChecked = preferenceManager.isLocationTrackingEnabled()

        binding.swNotifications.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            preferenceManager.setNotificationsEnabled(isChecked)
        }

        binding.swSosSound.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            preferenceManager.setSosSoundEnabled(isChecked)
        }

        binding.swLocation.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            preferenceManager.setLocationTrackingEnabled(isChecked)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
