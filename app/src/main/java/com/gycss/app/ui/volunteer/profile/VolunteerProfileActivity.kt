package com.gycss.app.ui.volunteer.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.gycss.app.databinding.ActivityVolunteerProfileBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VolunteerProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVolunteerProfileBinding
    private val viewModel: VolunteerProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVolunteerProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        setupObservers()

        viewModel.fetchProfile()
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnUpdate.setOnClickListener {
            val name = binding.tilName.editText?.text.toString().trim()
            val bio = binding.tilBio.editText?.text.toString().trim()
            val skills = binding.tilSkills.editText?.text.toString().trim()

            if (name.isEmpty()) {
                binding.tilName.error = "Name cannot be empty"
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE
            binding.btnUpdate.isEnabled = false
            viewModel.updateProfile(name, bio, skills)
        }
    }

    private fun setupObservers() {
        viewModel.userProfile.observe(this) { user ->
            user?.let {
                binding.tilName.editText?.setText(it.name)
                binding.tilBio.editText?.setText(it.bio)
                binding.tilSkills.editText?.setText(it.skills)
                if (!it.profileImageUrl.isNullOrEmpty()) {
                    Glide.with(this).load(it.profileImageUrl).into(binding.ivProfile)
                }
            }
        }

        viewModel.updateResult.observe(this) { result ->
            binding.progressBar.visibility = View.GONE
            binding.btnUpdate.isEnabled = true
            result.onSuccess {
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(this, "Failed to update profile: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
