package com.gycss.app.ui.senior

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gycss.app.R
import com.gycss.app.data.model.HealthVital
import com.gycss.app.databinding.ActivityHealthVitalsBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class HealthVitalsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHealthVitalsBinding
    private val viewModel: HealthVitalsViewModel by viewModels()
    private lateinit var adapter: HealthVitalsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHealthVitalsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupObservers()
        viewModel.fetchVitals()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = HealthVitalsAdapter()
        binding.rvVitalsHistory.layoutManager = LinearLayoutManager(this)
        binding.rvVitalsHistory.adapter = adapter

        binding.fabAddVital.setOnClickListener {
            showAddVitalDialog()
        }
    }

    private fun setupObservers() {
        viewModel.vitals.observe(this) { vitals ->
            adapter.submitList(vitals)
            val latest = vitals.firstOrNull()
            if (latest != null) {
                binding.tvLatestBp.text = "${latest.bloodPressureSys}/${latest.bloodPressureDia}"
                binding.tvLatestHeartRate.text = latest.heartRate.toString()
            }
        }
    }

    private fun showAddVitalDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_vital, null)
        val etSys = dialogView.findViewById<EditText>(R.id.et_sys)
        val etDia = dialogView.findViewById<EditText>(R.id.et_dia)
        val etHr = dialogView.findViewById<EditText>(R.id.et_heart_rate)

        AlertDialog.Builder(this)
            .setTitle("Add Daily Vitals")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val sys = etSys.text.toString().toIntOrNull() ?: 0
                val dia = etDia.text.toString().toIntOrNull() ?: 0
                val hr = etHr.text.toString().toIntOrNull() ?: 0
                
                if (sys > 0 && dia > 0 && hr > 0) {
                    viewModel.addVital(sys, dia, hr)
                } else {
                    Toast.makeText(this, "Please enter valid values", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    class HealthVitalsAdapter : RecyclerView.Adapter<HealthVitalsAdapter.ViewHolder>() {
        private var list = listOf<HealthVital>()
        private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

        fun submitList(newList: List<HealthVital>) {
            list = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.text1.text = "BP: ${item.bloodPressureSys}/${item.bloodPressureDia} | HR: ${item.heartRate} BPM"
            holder.text2.text = dateFormat.format(Date(item.timestamp))
        }

        override fun getItemCount() = list.size

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val text1: TextView = view.findViewById(android.R.id.text1)
            val text2: TextView = view.findViewById(android.R.id.text2)
        }
    }
}
