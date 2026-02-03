package com.gycss.app.ui.senior

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gycss.app.databinding.ActivityMedicalRecordsBinding

class MedicalRecordsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedicalRecordsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicalRecordsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mock data
        val records = "1. General Checkup - Dr. Smith - 12/01/2023\n" +
                      "2. Blood Pressure - Dr. Doe - 15/02/2023\n" +
                      "3. Diabetes Test - Dr. Lee - 10/03/2023"
        
        binding.tvRecordsList.text = records
        
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}