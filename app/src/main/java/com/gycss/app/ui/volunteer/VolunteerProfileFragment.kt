package com.gycss.app.ui.volunteer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.gycss.app.R
import com.gycss.app.databinding.FragmentVolunteerProfileBinding
import com.gycss.app.ui.volunteer.profile.VolunteerProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VolunteerProfileFragment : Fragment() {

    private var _binding: FragmentVolunteerProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VolunteerProfileViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVolunteerProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Start in View Mode
        toggleEditMode(false)
        
        setupListeners()
        setupObservers()
        viewModel.fetchProfile()
    }

    private fun setupListeners() {
        binding.btnEdit.setOnClickListener {
            toggleEditMode(true)
        }

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
        
        binding.btnBack.setOnClickListener {
            if (binding.editModeContainer.visibility == View.VISIBLE) {
                toggleEditMode(false)
            } else {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        binding.layoutNotifications.setOnClickListener {
            // Navigate to settings tab
            (activity as? VolunteerDashboardActivity)?.let {
                it.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)?.selectedItemId = R.id.navigation_settings
            }
        }
        
        binding.btnLogout.setOnClickListener {
            (activity as? VolunteerDashboardActivity)?.logout()
        }
    }

    private fun toggleEditMode(isEditing: Boolean) {
        binding.viewModeContainer.visibility = if (isEditing) View.GONE else View.VISIBLE
        binding.editModeContainer.visibility = if (isEditing) View.VISIBLE else View.GONE
        binding.btnEdit.visibility = if (isEditing) View.GONE else View.VISIBLE
        binding.btnUpdate.visibility = if (isEditing) View.VISIBLE else View.GONE
        binding.tvProfileTitle.text = if (isEditing) "Edit Profile" else "Volunteer Profile"
    }

    private fun setupObservers() {
        viewModel.userProfile.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.etName.text = it.name
                binding.tilName.editText?.setText(it.name)
                binding.tilBio.editText?.setText(it.bio)
                binding.tilSkills.editText?.setText(it.skills)
                
                Glide.with(this)
                    .load(it.profileImageUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .circleCrop()
                    .into(binding.ivProfilePic)
            }
        }

        viewModel.updateResult.observe(viewLifecycleOwner) { result ->
            binding.progressBar.visibility = View.GONE
            binding.btnUpdate.isEnabled = true
            result.onSuccess {
                Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                toggleEditMode(false)
            }.onFailure {
                Toast.makeText(requireContext(), "Failed to update profile: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
