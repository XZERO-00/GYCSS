package com.gycss.app.ui.senior

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.gycss.app.R
import com.gycss.app.databinding.ActivityRequestAssistanceBinding
import com.gycss.app.ui.senior.help.HelpRequestViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RequestAssistanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRequestAssistanceBinding
    private val viewModel: HelpRequestViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequestAssistanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinner()
        setupSubmitButton()
        setupObservers()
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

            viewModel.createHelpRequest(
                title = type,
                description = description,
                category = type
            )
        }
    }

    private fun setupObservers() {
        viewModel.requestResult.observe(this) { result ->
            result.onSuccess {
                binding.progressBar.visibility = View.GONE
                binding.btnSubmitRequest.isEnabled = true
                Toast.makeText(this, "Request Submitted Successfully!", Toast.LENGTH_LONG).show()
                finish()
            }.onFailure {
                binding.progressBar.visibility = View.GONE
                binding.btnSubmitRequest.isEnabled = true
                Toast.makeText(this, "Failed to submit request: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Add loading state observer if needed
    }
}
