package com.gycss.app.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gycss.app.R
import com.gycss.app.data.model.UserType
import com.gycss.app.databinding.ActivityLoginBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var selectedRole: UserType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupListeners()
    }

    private fun setupListeners() {
        binding.cardSenior.setOnClickListener {
            selectedRole = UserType.SENIOR
            updateRoleUI()
        }

        binding.cardVolunteer.setOnClickListener {
            selectedRole = UserType.VOLUNTEER
            updateRoleUI()
        }

        binding.btnContinue.setOnClickListener {
            when (selectedRole) {
                UserType.SENIOR -> {
                    startActivity(Intent(this, SeniorLoginActivity::class.java))
                }
                UserType.VOLUNTEER -> {
                    startActivity(Intent(this, VolunteerLoginActivity::class.java))
                }
                null -> {
                    Toast.makeText(this, "Please select a role to continue", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    private fun updateRoleUI() {
        // Reset both - using Material 3 card attributes
        binding.cardSenior.strokeWidth = 0
        binding.cardVolunteer.strokeWidth = 0
        
        // Highlight selection
        if (selectedRole == UserType.SENIOR) {
            binding.cardSenior.strokeWidth = 4
            binding.cardSenior.setStrokeColor(getColorStateList(R.color.primary_teal))
        } else if (selectedRole == UserType.VOLUNTEER) {
            binding.cardVolunteer.strokeWidth = 4
            binding.cardVolunteer.setStrokeColor(getColorStateList(R.color.primary_teal))
        }
    }
}
