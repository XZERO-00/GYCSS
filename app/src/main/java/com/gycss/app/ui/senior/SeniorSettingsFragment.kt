package com.gycss.app.ui.senior

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.gycss.app.R
import com.gycss.app.data.local.PreferenceManager
import com.gycss.app.databinding.FragmentSeniorSettingsBinding
import com.gycss.app.service.VoiceSOSService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SeniorSettingsFragment : Fragment() {

    private var _binding: FragmentSeniorSettingsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private var isInitialSelection = true

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startVoiceService()
        } else {
            binding.swVoiceSOS.isChecked = false
            preferenceManager.setVoiceSosEnabled(false)
            Toast.makeText(requireContext(), getString(R.string.mic_permission_required), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSeniorSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLanguageSpinner()
        setupAppearance()
        setupListeners()
        updateThemeSummary()
        
        // Set initial state of Voice SOS switch
        binding.swVoiceSOS.isChecked = preferenceManager.isVoiceSosEnabled()
    }

    private fun setupAppearance() {
        binding.layoutTheme.setOnClickListener {
            val themes = arrayOf("System Default", "Light", "Dark")
            val checkedItem = preferenceManager.getThemeMode()

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Choose Appearance")
                .setSingleChoiceItems(themes, checkedItem) { dialog, which ->
                    preferenceManager.setThemeMode(which)
                    updateThemeSummary()
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun updateThemeSummary() {
        val themeText = when (preferenceManager.getThemeMode()) {
            PreferenceManager.THEME_LIGHT -> "Light"
            PreferenceManager.THEME_DARK -> "Dark"
            else -> "System Default"
        }
        binding.tvCurrentTheme.text = themeText
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

    private fun setupListeners() {
        binding.swNotifications.setOnCheckedChangeListener { _, isChecked ->
            val msg = if (isChecked) "Notifications Enabled" else "Notifications Disabled"
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }

        binding.swVoiceSOS.setOnCheckedChangeListener { _, isChecked ->
            preferenceManager.setVoiceSosEnabled(isChecked)
            if (isChecked) {
                checkPermissionAndStartService()
            } else {
                stopVoiceService()
            }
        }

        binding.btnPrivacy.setOnClickListener {
            Toast.makeText(requireContext(), "Privacy Policy clicked", Toast.LENGTH_SHORT).show()
        }

        binding.btnSupport.setOnClickListener {
            Toast.makeText(requireContext(), "Contact Support clicked", Toast.LENGTH_SHORT).show()
        }

        binding.btnLogout.setOnClickListener {
            (activity as? SeniorDashboardActivity)?.logout()
        }
        
        binding.btnEmergencyContacts.setOnClickListener {
            Toast.makeText(requireContext(), "Manage Contacts coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermissionAndStartService() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startVoiceService()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startVoiceService() {
        val intent = Intent(requireContext(), VoiceSOSService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(intent)
        } else {
            requireContext().startService(intent)
        }
        Toast.makeText(requireContext(), getString(R.string.voice_sos_enabled), Toast.LENGTH_SHORT).show()
    }

    private fun stopVoiceService() {
        val intent = Intent(requireContext(), VoiceSOSService::class.java)
        requireContext().stopService(intent)
        Toast.makeText(requireContext(), getString(R.string.voice_sos_disabled), Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
