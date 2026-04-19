package com.gycss.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.gycss.app.R
import com.gycss.app.data.model.Role
import com.gycss.app.databinding.ActivityRoleSelectionBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RoleSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoleSelectionBinding
    private val viewModel: AuthViewModel by viewModels()
    private var selectedRole: Role? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoleSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupListeners()
        updateUI()
        
        viewModel.fetchUserCount()
    }

    private fun setupObservers() {
        viewModel.userCount.observe(this) { count ->
            binding.tvUserCount.text = "Join ${String.format("%,d", count)}+ members near you"
        }
    }

    private fun setupListeners() {
        binding.cardSenior.setOnClickListener {
            selectRole(Role.SENIOR)
        }

        binding.cardVolunteer.setOnClickListener {
            selectRole(Role.VOLUNTEER)
        }
        
        binding.btnGetStarted.setOnClickListener {
            selectedRole?.let { role ->
                navigateToLogin(role)
            }
        }
    }

    private fun selectRole(role: Role) {
        selectedRole = role
        updateUI()
    }

    private fun updateUI() {
        val primaryTeal = ContextCompat.getColor(this, R.color.teal_primary)
        val dividerSoft = ContextCompat.getColor(this, R.color.divider_soft)
        val tealLight = ContextCompat.getColor(this, R.color.teal_light)

        // Update Senior Card
        val isSenior = selectedRole == Role.SENIOR
        binding.cardSenior.strokeColor = if (isSenior) primaryTeal else dividerSoft
        binding.cardSenior.strokeWidth = if (isSenior) 6 else 2
        binding.ivCheckSenior.visibility = if (isSenior) View.VISIBLE else View.GONE
        binding.cardSenior.cardElevation = if (isSenior) 8f else 2f
        binding.cvSeniorIconBg.setCardBackgroundColor(if (isSenior) tealLight else dividerSoft)

        // Update Volunteer Card
        val isVolunteer = selectedRole == Role.VOLUNTEER
        binding.cardVolunteer.strokeColor = if (isVolunteer) primaryTeal else dividerSoft
        binding.cardVolunteer.strokeWidth = if (isVolunteer) 6 else 2
        binding.ivCheckVolunteer.visibility = if (isVolunteer) View.VISIBLE else View.GONE
        binding.cardVolunteer.cardElevation = if (isVolunteer) 8f else 2f
        binding.cvVolunteerIconBg.setCardBackgroundColor(if (isVolunteer) tealLight else dividerSoft)

        // Enable/Disable Continue Button
        binding.btnGetStarted.isEnabled = selectedRole != null
        binding.btnGetStarted.alpha = if (selectedRole != null) 1.0f else 0.5f
    }

    private fun navigateToLogin(role: Role) {
        val intent = Intent(this, LoginActivity::class.java).apply {
            putExtra("SELECTED_ROLE", role.name)
        }
        startActivity(intent)
    }
}
