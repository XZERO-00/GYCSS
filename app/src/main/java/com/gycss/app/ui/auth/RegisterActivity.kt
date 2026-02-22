package com.gycss.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.gycss.app.data.model.Role
import com.gycss.app.data.model.User
import com.gycss.app.databinding.ActivityRegisterBinding
import com.gycss.app.ui.senior.SeniorDashboardActivity
import com.gycss.app.ui.volunteer.VolunteerDashboardActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()
    private var selectedRole: Role? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val roleName = intent.getStringExtra("SELECTED_ROLE")
        selectedRole = if (roleName != null) Role.valueOf(roleName) else null

        setupUI()
        setupObservers()
        setupListeners()
    }

    private fun setupUI() {
        selectedRole?.let {
            binding.tvTitle.text = "Register as ${it.name.lowercase().capitalize()}"
            binding.toggleGroup.visibility = View.GONE
        }
    }

    private fun setupObservers() {
        viewModel.registrationResult.observe(this) { result ->
            when (result) {
                is AuthResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnRegister.isEnabled = true
                    Toast.makeText(this, "Welcome, ${result.user.name}!", Toast.LENGTH_SHORT).show()
                    if (result.user.role == Role.SENIOR) {
                        startActivity(Intent(this, SeniorDashboardActivity::class.java))
                    } else {
                        startActivity(Intent(this, VolunteerDashboardActivity::class.java))
                    }
                    finishAffinity()
                }
                is AuthResult.Failure -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnRegister.isEnabled = true
                    Toast.makeText(this, "Registration Failed: ${result.message}", Toast.LENGTH_LONG).show()
                }
                is AuthResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnRegister.isEnabled = false
                }
            }
        }
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener { finish() }

        binding.btnRegister.setOnClickListener {
            val name = binding.tilName.editText?.text.toString().trim()
            val email = binding.tilEmail.editText?.text.toString().trim()
            val password = binding.tilPassword.editText?.text.toString().trim()
            val phone = binding.tilPhone.editText?.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedRole == null) {
                Toast.makeText(this, "Role selection error. Please go back.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = User(
                name = name,
                email = email,
                phone = phone,
                role = selectedRole
            )

            viewModel.register(user, password)
        }
    }
}
