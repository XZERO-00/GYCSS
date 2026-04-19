package com.gycss.app.ui.auth

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.gycss.app.R
import com.gycss.app.data.local.PreferenceManager
import com.gycss.app.databinding.ActivityOnboardingPermissionsBinding
import com.gycss.app.ui.senior.LanguageSelectionActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingPermissionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingPermissionsBinding

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private var currentStep = 0
    private val steps = mutableListOf<PermissionStep>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingPermissionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSteps()
        setupListeners()
        updateUI()
    }

    private fun setupSteps() {
        steps.add(PermissionStep(
            title = getString(R.string.onboarding_title),
            desc = getString(R.string.onboarding_intro_desc),
            icon = R.drawable.ic_help,
            buttonText = getString(R.string.btn_continue),
            permission = null
        ))

        steps.add(PermissionStep(
            title = getString(R.string.onboarding_mic_title),
            desc = getString(R.string.onboarding_mic_desc),
            icon = R.drawable.ic_mic,
            buttonText = getString(R.string.btn_grant_permission),
            permission = Manifest.permission.RECORD_AUDIO
        ))

        steps.add(PermissionStep(
            title = getString(R.string.onboarding_loc_title),
            desc = getString(R.string.onboarding_loc_desc),
            icon = R.drawable.ic_location,
            buttonText = getString(R.string.btn_grant_permission),
            permission = Manifest.permission.ACCESS_FINE_LOCATION
        ))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            steps.add(PermissionStep(
                title = getString(R.string.onboarding_notif_title),
                desc = getString(R.string.onboarding_notif_desc),
                icon = R.drawable.ic_notifications,
                buttonText = getString(R.string.btn_grant_permission),
                permission = Manifest.permission.POST_NOTIFICATIONS
            ))
        }

        steps.add(PermissionStep(
            title = getString(R.string.onboarding_sms_title),
            desc = getString(R.string.onboarding_sms_desc),
            icon = R.drawable.ic_phone,
            buttonText = getString(R.string.btn_grant_permission),
            permission = Manifest.permission.SEND_SMS
        ))
    }

    private fun setupListeners() {
        binding.btnAction.setOnClickListener {
            handleAction()
        }
    }

    private fun handleAction() {
        val step = steps[currentStep]
        val permission = step.permission

        if (permission == null) {
            nextStep()
        } else {
            checkAndRequestPermission(permission)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            nextStep()
        } else {
            showDeniedState()
        }
    }

    private fun checkAndRequestPermission(permission: String) {
        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                nextStep()
            }
            shouldShowRequestPermissionRationale(permission) -> {
                showDeniedState()
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    private fun nextStep() {
        currentStep++
        if (currentStep < steps.size) {
            updateUI()
        } else {
            proceedToLanguageSelection()
        }
    }

    private fun updateUI() {
        val step = steps[currentStep]
        
        if (currentStep == 0) {
            binding.cvPermissionStep.visibility = View.GONE
            binding.ivOnboardingIllustration.visibility = View.VISIBLE
            binding.tvOnboardingTitle.text = step.title
            binding.tvOnboardingDesc.text = step.desc
        } else {
            binding.ivOnboardingIllustration.visibility = View.GONE
            binding.tvOnboardingTitle.visibility = View.GONE
            binding.tvOnboardingDesc.visibility = View.GONE
            
            binding.cvPermissionStep.visibility = View.VISIBLE
            binding.tvStepTitle.text = step.title
            binding.tvStepDesc.text = step.desc
            binding.ivStepIcon.setImageResource(step.icon)
        }
        
        binding.btnAction.text = step.buttonText
    }

    private fun showDeniedState() {
        binding.tvStepTitle.text = getString(R.string.permission_denied_title)
        binding.tvStepDesc.text = getString(R.string.permission_denied_desc)
        binding.btnAction.text = getString(R.string.btn_allow_again)
        
        binding.btnAction.setOnClickListener {
            val permission = steps[currentStep].permission
            if (permission != null) {
                if (shouldShowRequestPermissionRationale(permission)) {
                    requestPermissionLauncher.launch(permission)
                } else {
                    openAppSettings()
                }
            }
            setupListeners()
        }
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    private fun proceedToLanguageSelection() {
        // Mark first app open as completed
        preferenceManager.setFirstAppOpen(false)
        
        val intent = Intent(this, LanguageSelectionActivity::class.java).apply {
            putExtra("IS_ONBOARDING", true)
        }
        startActivity(intent)
        finish()
    }

    data class PermissionStep(
        val title: String,
        val desc: String,
        val icon: Int,
        val buttonText: String,
        val permission: String?
    )
}
