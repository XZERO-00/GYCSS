package com.gycss.app.ui.senior.profile

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.gycss.app.R
import com.gycss.app.data.local.PreferenceManager
import com.gycss.app.data.model.Senior
import com.gycss.app.data.model.UserType
import com.gycss.app.data.model.Volunteer
import com.gycss.app.data.repository.FirestoreRepository
import com.gycss.app.databinding.ActivityProfileBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private var selectedImageUri: Uri? = null
    private var currentProfileImageUrl: String = ""

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivProfilePic.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        loadProfile()
        setupButtons()
    }

    private fun setupUI() {
        val userType = preferenceManager.getUserType()
        if (userType == UserType.VOLUNTEER) {
            binding.tilAge.visibility = View.GONE
            binding.tilBloodGroup.visibility = View.GONE
            binding.tilEmergencyContact.visibility = View.GONE
            binding.tilAddress.hint = "Occupation / Work"
            binding.tilBio.visibility = View.VISIBLE
            binding.tilSkills.visibility = View.VISIBLE
        } else {
            binding.tilBio.visibility = View.GONE
            binding.tilSkills.visibility = View.GONE
        }
    }

    private fun loadProfile() {
        val userId = auth.currentUser?.uid ?: return
        val userType = preferenceManager.getUserType()

        if (userType == UserType.SENIOR) {
            FirestoreRepository.getSeniorProfile(userId) { senior ->
                runOnUiThread {
                    if (senior != null) {
                        binding.etName.setText(senior.name)
                        binding.etAge.setText(senior.age.toString())
                        binding.etPhone.setText(senior.phone)
                        binding.etAddress.setText(senior.address)
                        binding.etBloodGroup.setText(senior.bloodGroup)
                        currentProfileImageUrl = senior.profileImageUrl
                        loadProfileImage(senior.profileImageUrl)
                        if (senior.emergencyContacts.isNotEmpty()) {
                            binding.etEmergencyContact.setText(senior.emergencyContacts.first())
                        }
                    } else {
                        binding.etName.setText(auth.currentUser?.displayName)
                    }
                    binding.etEmail.setText(auth.currentUser?.email)
                }
            }
        } else if (userType == UserType.VOLUNTEER) {
            FirestoreRepository.getVolunteerProfile(userId) { volunteer ->
                runOnUiThread {
                    if (volunteer != null) {
                        binding.etName.setText(volunteer.name)
                        binding.etPhone.setText(volunteer.phone)
                        binding.etAddress.setText(volunteer.occupation)
                        binding.etBio.setText(volunteer.bio)
                        binding.etSkills.setText(volunteer.skills)
                        currentProfileImageUrl = volunteer.profileImageUrl
                        loadProfileImage(volunteer.profileImageUrl)
                    } else {
                        binding.etName.setText(auth.currentUser?.displayName)
                    }
                    binding.etEmail.setText(auth.currentUser?.email)
                }
            }
        }
        
        binding.etEmail.setText(auth.currentUser?.email)
        binding.etEmail.isEnabled = false
    }

    private fun loadProfileImage(url: String) {
        if (url.isNotEmpty()) {
            Glide.with(this)
                .load(url)
                .placeholder(R.mipmap.ic_launcher_round)
                .into(binding.ivProfilePic)
        }
    }

    private fun setupButtons() {
        binding.btnSave.setOnClickListener {
            if (selectedImageUri != null) {
                uploadImageAndSaveProfile()
            } else {
                saveProfile(currentProfileImageUrl)
            }
        }
        
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.fabEditPhoto.setOnClickListener {
            getContent.launch("image/*")
        }
    }

    private fun uploadImageAndSaveProfile() {
        val userId = auth.currentUser?.uid ?: return
        val uri = selectedImageUri ?: return

        binding.btnSave.isEnabled = false
        binding.btnSave.text = "Uploading Image..."

        FirestoreRepository.uploadProfileImage(userId, uri, onSuccess = { url ->
            saveProfile(url)
        }, onFailure = {
            binding.btnSave.isEnabled = true
            binding.btnSave.text = "Save Changes"
            Toast.makeText(this, "Image Upload Failed: ${it.message}", Toast.LENGTH_SHORT).show()
        })
    }

    private fun saveProfile(imageUrl: String) {
        val userId = auth.currentUser?.uid ?: return
        val userType = preferenceManager.getUserType()
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Name and Phone are required", Toast.LENGTH_SHORT).show()
            binding.btnSave.isEnabled = true
            binding.btnSave.text = "Save Changes"
            return
        }

        binding.btnSave.isEnabled = false
        binding.btnSave.text = "Saving Profile..."

        if (userType == UserType.SENIOR) {
            val age = binding.etAge.text.toString().toIntOrNull() ?: 0
            val address = binding.etAddress.text.toString().trim()
            val bloodGroup = binding.etBloodGroup.text.toString().trim()
            val emergencyContact = binding.etEmergencyContact.text.toString().trim()
            
            val senior = Senior(
                id = userId,
                name = name,
                age = age,
                phone = phone,
                email = auth.currentUser?.email ?: "",
                address = address,
                bloodGroup = bloodGroup,
                profileImageUrl = imageUrl,
                emergencyContacts = if (emergencyContact.isNotEmpty()) listOf(emergencyContact) else emptyList()
            )
            
            FirestoreRepository.saveSeniorProfile(senior, onSuccess = {
                runOnUiThread {
                    Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }, onFailure = {
                runOnUiThread {
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = "Save Changes"
                    Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else if (userType == UserType.VOLUNTEER) {
            val occupation = binding.etAddress.text.toString().trim()
            val bio = binding.etBio.text.toString().trim()
            val skills = binding.etSkills.text.toString().trim()
            
            val volunteer = Volunteer(
                id = userId,
                name = name,
                phone = phone,
                email = auth.currentUser?.email ?: "",
                occupation = occupation,
                bio = bio,
                skills = skills,
                profileImageUrl = imageUrl,
                isAvailable = true
            )
            
            FirestoreRepository.saveVolunteerProfile(volunteer, onSuccess = {
                runOnUiThread {
                    Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }, onFailure = {
                runOnUiThread {
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = "Save Changes"
                    Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
