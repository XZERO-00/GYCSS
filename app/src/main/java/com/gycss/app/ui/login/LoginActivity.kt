package com.gycss.app.ui.login

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.gycss.app.R
import com.gycss.app.data.model.UserType
import com.gycss.app.databinding.ActivityLoginBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var selectedRole: UserType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupWindowInsets()
        setupListeners()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupListeners() {
        binding.cardSenior.setOnClickListener {
            selectRole(UserType.SENIOR)
        }

        binding.cardVolunteer.setOnClickListener {
            selectRole(UserType.VOLUNTEER)
        }

        binding.btnContinue.setOnClickListener {
            selectedRole?.let { role ->
                val intent = when (role) {
                    UserType.SENIOR -> Intent(this, SeniorLoginActivity::class.java)
                    UserType.VOLUNTEER -> Intent(this, VolunteerLoginActivity::class.java)
                    else -> null
                }
                
                intent?.let {
                    startActivity(it)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                } ?: run {
                    Toast.makeText(this, "Selected role login not available", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Toast.makeText(this, "Please select a role to continue", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun selectRole(role: UserType) {
        if (selectedRole == role) return
        
        selectedRole = role
        updateRoleUI()
        
        // Animate the selected card
        val selectedCard = if (role == UserType.SENIOR) binding.cardSenior else binding.cardVolunteer
        val otherCard = if (role == UserType.SENIOR) binding.cardVolunteer else binding.cardSenior
        
        selectedCard.animate().scaleX(1.05f).scaleY(1.05f).setDuration(200).start()
        otherCard.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start()

        // Enable and show continue button if it was disabled
        if (!binding.btnContinue.isEnabled) {
            binding.btnContinue.isEnabled = true
            binding.btnContinue.animate().alpha(1.0f).setDuration(300).start()
        }
    }

    private fun updateRoleUI() {
        // Update Senior Card
        val isSenior = selectedRole == UserType.SENIOR
        binding.cardSenior.strokeWidth = if (isSenior) 6 else 0
        binding.ivSeniorCheck.visibility = if (isSenior) View.VISIBLE else View.GONE
        
        // Update Volunteer Card
        val isVolunteer = selectedRole == UserType.VOLUNTEER
        binding.cardVolunteer.strokeWidth = if (isVolunteer) 6 else 0
        binding.ivVolunteerCheck.visibility = if (isVolunteer) View.VISIBLE else View.GONE
    }
}
