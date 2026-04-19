package com.gycss.app.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.gycss.app.data.local.PreferenceManager
import com.gycss.app.data.model.Role
import com.gycss.app.databinding.ActivityLoginBinding
import com.gycss.app.ui.senior.SeniorDashboardActivity
import com.gycss.app.ui.volunteer.VolunteerDashboardActivity
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()
    private var selectedRole: Role? = null

    @Inject
    lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val roleName = intent.getStringExtra("SELECTED_ROLE")
        selectedRole = if (roleName != null) {
            try { Role.valueOf(roleName) } catch (e: Exception) { null }
        } else null

        setupObservers()
        setupListeners()
        setupValidationListeners()
        updateUIForRole()
    }

    private fun updateUIForRole() {
        selectedRole?.let {
            val roleText = it.name.lowercase(Locale.ROOT).replaceFirstChar { char -> char.uppercase() }
            binding.tvTitle.text = "Login as $roleText"
        } ?: run {
            binding.tvTitle.text = "Login to GYCSS"
        }
    }

    private fun setupObservers() {
        viewModel.loginResult.observe(this) { result ->
            when (result) {
                is AuthResult.Success -> {
                    hideLoading()
                    val user = result.user
                    
                    // Verify if the account role matches the UI selection (if any)
                    if (selectedRole != null && user.role != selectedRole) {
                        val expected = selectedRole?.name?.lowercase()
                        val actual = user.role?.name?.lowercase()
                        Toast.makeText(this, "This account is a $actual account, but you selected $expected.", Toast.LENGTH_LONG).show()
                        viewModel.logout()
                        return@observe
                    }

                    Toast.makeText(this, "Welcome back, ${user.name}!", Toast.LENGTH_SHORT).show()
                    navigateToNextScreen(user.role)
                }
                is AuthResult.Failure -> {
                    hideLoading()
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }
                is AuthResult.Loading -> {
                    showLoading()
                }
            }
        }
    }

    private fun navigateToNextScreen(role: Role?) {
        val intent = when (role) {
            Role.SENIOR -> {
                // Check if they need onboarding
                if (preferenceManager.isFirstLogin()) {
                    Intent(this, OnboardingPermissionsActivity::class.java)
                } else {
                    Intent(this, SeniorDashboardActivity::class.java)
                }
            }
            Role.VOLUNTEER -> Intent(this, VolunteerDashboardActivity::class.java)
            else -> {
                Toast.makeText(this, "No valid role found for this account.", Toast.LENGTH_SHORT).show()
                return
            }
        }
        startActivity(intent)
        finishAffinity()
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false
        hideKeyboard()
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnLogin.isEnabled = true
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener { finish() }

        binding.btnLogin.setOnClickListener {
            if (validateInputs()) {
                val email = binding.tilEmail.editText?.text.toString().trim()
                val password = binding.tilPassword.editText?.text.toString().trim()
                viewModel.login(email, password)
            }
        }

        binding.tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java).apply {
                putExtra("SELECTED_ROLE", selectedRole?.name)
            }
            startActivity(intent)
        }

        binding.tvForgotPassword.setOnClickListener {
            val email = binding.tilEmail.editText?.text.toString().trim()
            if (email.isEmpty()) {
                binding.tilEmail.error = "Enter your email to reset password"
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilEmail.error = "Enter a valid email address"
            } else {
                viewModel.sendPasswordResetEmail(email)
                Toast.makeText(this, "Password reset email sent if account exists", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupValidationListeners() {
        binding.tilEmail.editText?.addTextChangedListener { binding.tilEmail.error = null }
        binding.tilPassword.editText?.addTextChangedListener { binding.tilPassword.error = null }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        val email = binding.tilEmail.editText?.text.toString().trim()
        val password = binding.tilPassword.editText?.text.toString().trim()

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email cannot be empty"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Enter a valid email address"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password cannot be empty"
            isValid = false
        }

        return isValid
    }
}
