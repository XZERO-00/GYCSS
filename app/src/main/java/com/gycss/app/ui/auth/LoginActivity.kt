package com.gycss.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.gycss.app.data.model.Role
import com.gycss.app.databinding.ActivityLoginBinding
import com.gycss.app.ui.senior.SeniorDashboardActivity
import com.gycss.app.ui.volunteer.VolunteerDashboardActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()
    private var selectedRole: Role? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val roleName = intent.getStringExtra("SELECTED_ROLE")
        selectedRole = if (roleName != null) Role.valueOf(roleName) else null

        setupObservers()
        setupListeners()
        updateUIForRole()
    }

    private fun updateUIForRole() {
        selectedRole?.let {
            binding.tvTitle.text = "Login as ${it.name.lowercase().capitalize()}"
        }
    }

    private fun setupObservers() {
        viewModel.loginResult.observe(this) { result ->
            when (result) {
                is AuthResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    
                    val user = result.user
                    if (selectedRole != null && user.role != selectedRole) {
                        Toast.makeText(this, "Your account is not registered as a ${selectedRole?.name?.lowercase()}", Toast.LENGTH_LONG).show()
                        return@observe
                    }

                    Toast.makeText(this, "Welcome, ${user.name}!", Toast.LENGTH_SHORT).show()
                    if (user.role == Role.SENIOR) {
                        startActivity(Intent(this, SeniorDashboardActivity::class.java))
                    } else {
                        startActivity(Intent(this, VolunteerDashboardActivity::class.java))
                    }
                    finishAffinity()
                }
                is AuthResult.Failure -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this, "Login Failed: ${result.message}", Toast.LENGTH_LONG).show()
                }
                is AuthResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnLogin.isEnabled = false
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.tilEmail.editText?.text.toString().trim()
            val password = binding.tilPassword.editText?.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.login(email, password)
        }

        binding.tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java).apply {
                putExtra("SELECTED_ROLE", selectedRole?.name)
            }
            startActivity(intent)
        }

        binding.tvForgotPassword.setOnClickListener {
            // Implement password reset flow
        }
    }
}
