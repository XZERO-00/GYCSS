package com.gycss.app.ui.senior.profile

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gycss.app.databinding.ActivityProfileBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadProfile()
        setupButtons()
    }

    private fun loadProfile() {
        // Mock data loading with Indian names
        binding.etName.setText("Rajesh Kumar")
        binding.etAge.setText("68")
        binding.etBloodGroup.setText("O+")
        binding.etPhone.setText("+91 98765 43210")
        binding.etAddress.setText("123, Gandhi Nagar, New Delhi")
        binding.etEmergencyContact.setText("+91 99887 76655 (Son)")
    }

    private fun setupButtons() {
        binding.btnSave.setOnClickListener {
            // Save logic here
            Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show()
            finish()
        }
        
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}