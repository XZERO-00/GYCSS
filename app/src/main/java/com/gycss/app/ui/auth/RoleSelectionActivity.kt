package com.gycss.app.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gycss.app.data.model.Role
import com.gycss.app.databinding.ActivityRoleSelectionBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RoleSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoleSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoleSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.cardSenior.setOnClickListener {
            navigateToLogin(Role.SENIOR)
        }

        binding.cardVolunteer.setOnClickListener {
            navigateToLogin(Role.VOLUNTEER)
        }
    }

    private fun navigateToLogin(role: Role) {
        val intent = Intent(this, LoginActivity::class.java).apply {
            putExtra("SELECTED_ROLE", role.name)
        }
        startActivity(intent)
    }
}
