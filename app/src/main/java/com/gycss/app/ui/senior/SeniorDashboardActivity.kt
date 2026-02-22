package com.gycss.app.ui.senior

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.gycss.app.R
import com.gycss.app.databinding.ActivitySeniorDashboardBinding
import com.gycss.app.ui.auth.LoginActivity
import com.gycss.app.ui.senior.profile.ProfileActivity
import com.gycss.app.ui.senior.settings.SettingsActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SeniorDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeniorDashboardBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var toggle: ActionBarDrawerToggle
    private val viewModel: SeniorDashboardViewModel by viewModels()
    private lateinit var requestAdapter: ActiveRequestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeniorDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupToolbar()
        setupDrawer()
        setupBottomNav()
        setupRecyclerView()
        setupClickListeners()
        setupObservers()

        viewModel.fetchUserProfile()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = ""
    }

    private fun setupDrawer() {
        toggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_logout -> {
                    viewModel.logout()
                    startActivity(Intent(this, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupBottomNav() {
        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> true
                R.id.navigation_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                R.id.navigation_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        requestAdapter = ActiveRequestAdapter { requestId ->
            showCancelConfirmation(requestId)
        }
        binding.rvActiveRequests.apply {
            layoutManager = LinearLayoutManager(this@SeniorDashboardActivity)
            adapter = requestAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnSos.setOnLongClickListener { 
            vibrate()
            checkLocationAndTriggerSOS()
            true
        }

        binding.btnMedicalRecords.setOnClickListener {
            startActivity(Intent(this, MedicalRecordsActivity::class.java))
        }

        binding.btnReminders.setOnClickListener {
            startActivity(Intent(this, MedicationRemindersActivity::class.java))
        }

        binding.btnGrocery.setOnClickListener { openRequestAssistance("Grocery") }
        binding.btnMedicine.setOnClickListener { openRequestAssistance("Medicine Pickup") }
        binding.btnUtilities.setOnClickListener { openRequestAssistance("Home Utilities") }
    }

    private fun openRequestAssistance(type: String) {
        val intent = Intent(this, RequestAssistanceActivity::class.java).apply {
            putExtra("REQUEST_TYPE", type)
        }
        startActivity(intent)
    }

    private fun setupObservers() {
        viewModel.userProfile.observe(this) { user ->
            user?.let { binding.tvWelcomeName.text = "Hello, ${it.name}" }
        }

        viewModel.activeRequests.observe(this) { requests ->
            requestAdapter.submitList(requests)
            binding.tvActiveRequestsLabel.visibility = if (requests.isNotEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.sosResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "SOS Alert Sent! Help is on the way.", Toast.LENGTH_LONG).show()
            }.onFailure { 
                Toast.makeText(this, "SOS Failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }

        viewModel.activeAlert.observe(this) { alert ->
            alert?.let {
                if (it.status == "Assigned") {
                    showHelpOnTheWayDialog(it.assignedVolunteerName)
                } else if (it.status == "Completed") {
                    Toast.makeText(this, "Emergency support completed.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkLocationAndTriggerSOS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            viewModel.triggerSOS(location?.latitude ?: 0.0, location?.longitude ?: 0.0)
        }
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
            checkLocationAndTriggerSOS()
        } else {
            Toast.makeText(this, "Location permission required for SOS.", Toast.LENGTH_LONG).show()
        }
    }

    private fun showCancelConfirmation(requestId: String) {
        AlertDialog.Builder(this)
            .setTitle("Cancel Request")
            .setMessage("Are you sure you want to cancel this request?")
            .setPositiveButton("Yes") { _, _ -> viewModel.cancelRequest(requestId) }
            .setNegativeButton("No", null)
            .show()
    }

    private fun showHelpOnTheWayDialog(volunteerName: String?) {
        AlertDialog.Builder(this)
            .setTitle("Help is on the way!")
            .setMessage("${volunteerName ?: "A volunteer"} is coming to help.")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}
