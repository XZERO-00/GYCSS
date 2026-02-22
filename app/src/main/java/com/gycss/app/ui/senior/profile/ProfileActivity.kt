package com.gycss.app.ui.senior.profile

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.gycss.app.R
import com.gycss.app.data.local.PreferenceManager
import com.gycss.app.data.model.Role
import com.gycss.app.data.model.User
import com.gycss.app.databinding.ActivityProfileBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val viewModel: ProfileViewModel by viewModels()

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private var profileImageUri: Uri? = null
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupObservers()
        viewModel.fetchProfile()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnSave.setOnClickListener { saveProfile() }
        binding.fabEditPhoto.setOnClickListener { pickImage() }
        
        val role = preferenceManager.getUserRole()
        if (role == Role.VOLUNTEER) {
            binding.tilAge.visibility = View.GONE
            binding.tilBloodGroup.visibility = View.GONE
            binding.tilOccupation.visibility = View.VISIBLE
            binding.tilBio.visibility = View.VISIBLE
            binding.tilSkills.visibility = View.VISIBLE
        } else {
            binding.tilAge.visibility = View.VISIBLE
            binding.tilBloodGroup.visibility = View.VISIBLE
            binding.tilOccupation.visibility = View.GONE
            binding.tilBio.visibility = View.GONE
            binding.tilSkills.visibility = View.GONE
        }
    }

    private fun setupObservers() {
        viewModel.userProfile.observe(this) { user ->
            user?.let {
                currentUser = it
                binding.etName.setText(it.name)
                binding.etEmail.setText(it.email)
                binding.etPhone.setText(it.phone)
                binding.etAddress.setText(it.address)
                binding.etAge.setText(it.age.toString())
                binding.etBloodGroup.setText(it.bloodGroup)
                binding.etOccupation.setText(it.occupation)
                binding.etBio.setText(it.bio)
                binding.etSkills.setText(it.skills)
                binding.etEmergencyContact.setText(it.emergencyContacts.firstOrNull() ?: "")
                
                if (!it.profileImageUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(it.profileImageUrl)
                        .placeholder(R.drawable.ic_person)
                        .circleCrop()
                        .into(binding.ivProfilePic)
                }
            }
        }

        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSave.isEnabled = !isLoading
        }

        viewModel.updateResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                finish()
            }.onFailure {
                Toast.makeText(this, "Update Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pickImage() {
        imagePickerLauncher.launch("image/*")
    }

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            profileImageUri = uri
            binding.ivProfilePic.setImageURI(uri)
        }
    }

    private fun saveProfile() {
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val emergencyContact = binding.etEmergencyContact.text.toString().trim()
        
        if (name.isEmpty()) {
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedUser = currentUser?.copy(
            name = name,
            phone = phone,
            address = address,
            age = binding.etAge.text.toString().toIntOrNull() ?: 0,
            bloodGroup = binding.etBloodGroup.text.toString(),
            occupation = binding.etOccupation.text.toString(),
            bio = binding.etBio.text.toString(),
            skills = binding.etSkills.text.toString(),
            emergencyContacts = if (emergencyContact.isNotEmpty()) listOf(emergencyContact) else emptyList()
        ) ?: return

        viewModel.updateProfile(updatedUser, profileImageUri)
    }
}
