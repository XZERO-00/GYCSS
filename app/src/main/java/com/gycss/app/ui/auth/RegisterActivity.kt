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
import com.gycss.app.data.model.User
import com.gycss.app.databinding.ActivityRegisterBinding
import com.gycss.app.ui.senior.SeniorDashboardActivity
import com.gycss.app.ui.volunteer.VolunteerDashboardActivity
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()
    private var selectedRole: Role? = null

    @Inject
    lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val roleName = intent.getStringExtra("SELECTED_ROLE")
        selectedRole = if (roleName != null) {
            try { Role.valueOf(roleName) } catch (e: Exception) { null }
        } else null

        setupUI()
        setupObservers()
        setupListeners()
        setupValidationListeners()
    }

    private fun setupUI() {
        selectedRole?.let {
            val roleText = it.name.lowercase(Locale.ROOT).replaceFirstChar { char -> char.uppercase() }
            binding.tvTitle.text = "Register as $roleText"
            binding.toggleGroup.visibility = View.GONE
        }
    }

    private fun setupObservers() {
        viewModel.registrationResult.observe(this) { result ->
            when (result) {
                is AuthResult.Success -> {
                    hideLoading()
                    Toast.makeText(this, "Welcome, ${result.user.name}!", Toast.LENGTH_SHORT).show()
                    navigateToNextScreen(result.user.role)
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
                if (preferenceManager.isFirstLogin()) {
                    Intent(this, OnboardingPermissionsActivity::class.java)
                } else {
                    Intent(this, SeniorDashboardActivity::class.java)
                }
            }
            Role.VOLUNTEER -> Intent(this, VolunteerDashboardActivity::class.java)
            else -> {
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                Intent(this, RoleSelectionActivity::class.java)
            }
        }
        startActivity(intent)
        finishAffinity()
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener { finish() }

        binding.btnRegister.setOnClickListener {
            if (validateInputs()) {
                val name = binding.tilName.editText?.text.toString().trim()
                val email = binding.tilEmail.editText?.text.toString().trim()
                val password = binding.tilPassword.editText?.text.toString().trim()
                val phone = binding.tilPhone.editText?.text.toString().trim()

                val role = selectedRole ?: if (binding.btnSenior.isChecked) Role.SENIOR else Role.VOLUNTEER

                val user = User(
                    name = name,
                    email = email,
                    phone = phone,
                    role = role
                )

                viewModel.register(user, password)
            }
        }

        binding.btnGoogleRegister.setOnClickListener {
            Toast.makeText(this, "Google Registration coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.btnMobileRegister.setOnClickListener {
            Toast.makeText(this, "Mobile Registration coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupValidationListeners() {
        binding.tilName.editText?.addTextChangedListener { binding.tilName.error = null }
        binding.tilEmail.editText?.addTextChangedListener { binding.tilEmail.error = null }
        binding.tilPhone.editText?.addTextChangedListener { binding.tilPhone.error = null }
        binding.tilPassword.editText?.addTextChangedListener { binding.tilPassword.error = null }
        binding.tilConfirmPassword.editText?.addTextChangedListener { binding.tilConfirmPassword.error = null }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        val name = binding.tilName.editText?.text.toString().trim()
        val email = binding.tilEmail.editText?.text.toString().trim()
        val phone = binding.tilPhone.editText?.text.toString().trim()
        val password = binding.tilPassword.editText?.text.toString().trim()
        val confirmPassword = binding.tilConfirmPassword.editText?.text.toString().trim()

        if (name.isEmpty()) {
            binding.tilName.error = "Name cannot be empty"
            isValid = false
        }

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email cannot be empty"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Enter a valid email address"
            isValid = false
        }

        if (phone.isEmpty()) {
            binding.tilPhone.error = "Phone number cannot be empty"
            isValid = false
        } else if (phone.length < 10) {
            binding.tilPhone.error = "Enter a valid phone number"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password cannot be empty"
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            isValid = false
        }

        if (confirmPassword != password) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            isValid = false
        }

        return isValid
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnRegister.isEnabled = false
        hideKeyboard()
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnRegister.isEnabled = true
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}
