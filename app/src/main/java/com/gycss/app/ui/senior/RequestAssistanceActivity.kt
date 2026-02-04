package com.gycss.app.ui.senior

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.gycss.app.R
import com.gycss.app.data.model.AssistanceRequest
import com.gycss.app.data.repository.FirestoreRepository
import com.gycss.app.databinding.ActivityRequestAssistanceBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RequestAssistanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRequestAssistanceBinding

    @Inject
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequestAssistanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinner()
        setupSubmitButton()
    }

    private fun setupSpinner() {
        val requestTypes = listOf("Grocery", "Medicine Pickup", "Home Utilities", "Transport", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, requestTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRequestType.adapter = adapter
        
        val preSelectedType = intent.getStringExtra("REQUEST_TYPE")
        if (preSelectedType != null) {
            val position = requestTypes.indexOf(preSelectedType)
            if (position >= 0) {
                binding.spinnerRequestType.setSelection(position)
            }
        }
    }

    private fun setupSubmitButton() {
        binding.btnSubmitRequest.setOnClickListener {
            val type = binding.spinnerRequestType.selectedItem.toString()
            val description = binding.etDescription.text.toString().trim()

            if (description.isBlank()) {
                binding.etDescription.error = "Please describe what you need"
                return@setOnClickListener
            }

            val user = auth.currentUser
            val request = AssistanceRequest(
                type = type,
                description = description,
                seniorId = user?.uid ?: "unknown",
                seniorName = user?.displayName ?: "Senior"
            )

            binding.btnSubmitRequest.isEnabled = false
            binding.btnSubmitRequest.text = "Submitting..."

            FirestoreRepository.requestAssistance(request, onSuccess = {
                runOnUiThread {
                    Toast.makeText(this, "Request Submitted Successfully!", Toast.LENGTH_LONG).show()
                    finish()
                }
            }, onFailure = {
                runOnUiThread {
                    binding.btnSubmitRequest.isEnabled = true
                    binding.btnSubmitRequest.text = "Submit Request"
                    Toast.makeText(this, "Failed to submit request", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
