package com.gycss.app.ui.senior.settings

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gycss.app.databinding.ActivitySettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.swNotifications.setOnCheckedChangeListener { _, isChecked ->
            val msg = if(isChecked) "Notifications Enabled" else "Notifications Disabled"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        binding.swLocationTracking.setOnCheckedChangeListener { _, isChecked ->
            val msg = if(isChecked) "Location Tracking Enabled" else "Location Tracking Disabled"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        binding.btnPrivacy.setOnClickListener {
            Toast.makeText(this, "Privacy Policy clicked", Toast.LENGTH_SHORT).show()
        }

        binding.btnSupport.setOnClickListener {
            Toast.makeText(this, "Contact Support clicked", Toast.LENGTH_SHORT).show()
        }

        binding.btnLogout.setOnClickListener {
            Toast.makeText(this, "Logged Out Successfully", Toast.LENGTH_SHORT).show()
            // In real app: auth.signOut() and navigate to Login
            finishAffinity() 
        }
    }
}