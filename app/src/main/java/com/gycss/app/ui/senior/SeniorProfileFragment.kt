package com.gycss.app.ui.senior

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.gycss.app.R
import com.gycss.app.data.model.User
import com.gycss.app.databinding.FragmentSeniorProfileBinding
import com.gycss.app.ui.senior.profile.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SeniorProfileFragment : Fragment() {

    private var _binding: FragmentSeniorProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    private var profileImageUri: Uri? = null
    private var currentUser: User? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSeniorProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupObservers()
        viewModel.fetchProfile()
    }

    private fun setupUI() {
        binding.btnSave.setOnClickListener { saveProfile() }
        binding.fabEditPhoto.setOnClickListener { pickImage() }
    }

    private fun setupObservers() {
        viewModel.userProfile.observe(viewLifecycleOwner) { user ->
            user?.let {
                currentUser = it
                binding.etName.setText(it.name)
                binding.etEmail.setText(it.email)
                binding.etPhone.setText(it.phone)
                binding.etAddress.setText(it.address)
                binding.etAge.setText(it.age.toString())
                binding.etBloodGroup.setText(it.bloodGroup)
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

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSave.isEnabled = !isLoading
        }

        viewModel.updateResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(requireContext(), "Update Failed: ${it.message}", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(requireContext(), "Name is required", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedUser = currentUser?.copy(
            name = name,
            phone = phone,
            address = address,
            age = binding.etAge.text.toString().toIntOrNull() ?: 0,
            bloodGroup = binding.etBloodGroup.text.toString(),
            emergencyContacts = if (emergencyContact.isNotEmpty()) listOf(emergencyContact) else emptyList()
        ) ?: return

        viewModel.updateProfile(updatedUser, profileImageUri)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
