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

        setupUI()
        setupDropdown()
        setupObservers()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { finish() }
        
        binding.btnSubmit.setOnClickListener {
            val category = binding.actvCategory.text.toString()
            val title = binding.etTitle.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()

            if (title.isBlank()) {
                binding.etTitle.error = "Please enter a title"
                return@setOnClickListener
            }

            if (description.isBlank()) {
                binding.etDescription.error = "Please describe what you need"
                return@setOnClickListener
            }

            binding.btnSubmit.isEnabled = false
            viewModel.createHelpRequest(
                title = title,
                description = description,
                category = category
            )
        }
    }

    private fun setupDropdown() {
        val categories = listOf("Grocery Delivery", "Medicine Pickup", "Home Utilities", "Transport", "Tech Support", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categories)
        binding.actvCategory.setAdapter(adapter)
        
        val preSelectedType = intent.getStringExtra("REQUEST_TYPE")
        if (preSelectedType != null) {
            binding.actvCategory.setText(preSelectedType, false)
        }
    }

    private fun setupObservers() {
        viewModel.requestResult.observe(this) { result ->
            binding.btnSubmit.isEnabled = true
            result.onSuccess {
                Toast.makeText(this, "Request Submitted Successfully!", Toast.LENGTH_LONG).show()
                finish()
            }.onFailure {
                Toast.makeText(this, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
