package com.gycss.app.ui.senior

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.gycss.app.R
import com.gycss.app.databinding.ActivityMedicalRecordsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MedicalRecordsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedicalRecordsBinding
    private val viewModel: MedicalRecordViewModel by viewModels()
    private lateinit var adapter: MedicalRecordAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicalRecordsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupObservers()
        viewModel.fetchRecords()
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = MedicalRecordAdapter { recordId ->
            showDeleteConfirmation(recordId)
        }

        binding.rvMedicalRecords.layoutManager = LinearLayoutManager(this)
        binding.rvMedicalRecords.adapter = adapter

        binding.fabAddRecord.setOnClickListener {
            showAddRecordDialog()
        }
    }

    private fun setupObservers() {
        viewModel.records.observe(this) { records ->
            adapter.submitList(records)
            binding.tvEmptyState.visibility = if (records.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.operationResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Operation successful", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAddRecordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_medical_record, null)
        val etDescription = dialogView.findViewById<EditText>(R.id.et_description)
        val etDoctorName = dialogView.findViewById<EditText>(R.id.et_doctor_name)

        AlertDialog.Builder(this)
            .setTitle("Add Medical Record")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val description = etDescription.text.toString().trim()
                val doctorName = etDoctorName.text.toString().trim()
                if (description.isNotEmpty() && doctorName.isNotEmpty()) {
                    viewModel.addRecord(description, doctorName)
                } else {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmation(recordId: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Record")
            .setMessage("Are you sure you want to delete this medical record?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteRecord(recordId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
