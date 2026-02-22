package com.gycss.app.ui.volunteer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.gycss.app.R
import com.gycss.app.databinding.ActivityVolunteerDashboardBinding
import com.gycss.app.ui.auth.LoginActivity
import com.gycss.app.ui.volunteer.help.AvailableRequestsActivity
import com.gycss.app.ui.volunteer.help.VolunteerTasksActivity
import com.gycss.app.ui.volunteer.profile.VolunteerProfileActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VolunteerDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVolunteerDashboardBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private val viewModel: VolunteerDashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVolunteerDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupDrawer()
        setupBottomNav()
        setupClickListeners()
        setupObservers()

        viewModel.fetchUserProfile()
        viewModel.listenForSOSAlerts()
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
                R.id.nav_profile -> startActivity(Intent(this, VolunteerProfileActivity::class.java))
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
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_profile -> {
                    startActivity(Intent(this, VolunteerProfileActivity::class.java))
                    true
                }
                else -> true
            }
        }
    }

    private fun setupClickListeners() {
        binding.swAvailability.setOnCheckedChangeListener { _, isChecked ->
            binding.tvStatus.text = if (isChecked) "Available" else "Unavailable"
        }

        binding.btnAcceptSos.setOnClickListener { 
            viewModel.sosAlerts.value?.firstOrNull()?.let { alert ->
                viewModel.acceptSOS(alert.alertId)
            }
        }

        binding.btnViewRequests.setOnClickListener {
            startActivity(Intent(this, AvailableRequestsActivity::class.java))
        }

        binding.btnEvents.setOnClickListener {
            startActivity(Intent(this, VolunteerTasksActivity::class.java))
        }
    }

    private fun setupObservers() {
        viewModel.userProfile.observe(this) { user ->
            user?.let {
                binding.tvWelcomeName.text = "Hello, ${it.name}"
                binding.tvHelpedCountStat.text = it.helpCount.toString()
                binding.tvRatingStat.text = String.format("%.1f", it.rating)
                if (!it.profileImageUrl.isNullOrEmpty()) {
                    Glide.with(this).load(it.profileImageUrl).into(binding.ivProfilePic)
                }
            }
        }

        viewModel.sosAlerts.observe(this) { alerts ->
            val latestAlert = alerts.firstOrNull()
            if (latestAlert != null && binding.swAvailability.isChecked) {
                binding.cvSosAlerts.visibility = View.VISIBLE
                binding.tvSosCount.text = "New SOS from ${latestAlert.seniorName}"
            } else {
                binding.cvSosAlerts.visibility = View.GONE
            }
        }

        viewModel.acceptResult.observe(this) { result ->
            result.onSuccess { 
                Toast.makeText(this, "SOS Accepted!", Toast.LENGTH_SHORT).show()
                val alert = viewModel.sosAlerts.value?.firstOrNull()
                alert?.location?.let {
                    openMaps(it.latitude, it.longitude)
                }
            }.onFailure {
                Toast.makeText(this, "Failed to accept SOS: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openMaps(latitude: Double, longitude: Double) {
        val gmmIntentUri = Uri.parse("google.navigation:q=$latitude,$longitude")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        if (mapIntent.resolveActivity(packageManager) != null) {
            startActivity(mapIntent)
        } else {
            Toast.makeText(this, "Google Maps not installed.", Toast.LENGTH_SHORT).show()
        }
    }
}
