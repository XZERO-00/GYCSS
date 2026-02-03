package com.gycss.app.ui.volunteer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.gycss.app.R
import com.gycss.app.data.model.SOSAlert
import com.gycss.app.data.repository.FirestoreRepository
import com.gycss.app.databinding.ActivityVolunteerDashboardBinding
import com.gycss.app.ui.common.VolunteersListActivity
import com.gycss.app.ui.login.LoginActivity
import com.gycss.app.ui.senior.profile.ProfileActivity
import com.gycss.app.ui.senior.settings.SettingsActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class VolunteerDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVolunteerDashboardBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private var sosListener: ListenerRegistration? = null
    private var currentActiveAlert: SOSAlert? = null

    @Inject
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVolunteerDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupListeners()
        setupBottomNavigation()
        setupDrawerNavigation()
        setupSOSListener()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun setupSOSListener() {
        sosListener = FirestoreRepository.listenForSOSAlerts { alert ->
            runOnUiThread {
                showSOSAlert(alert)
            }
        }
    }

    private fun showSOSAlert(alert: SOSAlert) {
        currentActiveAlert = alert
        binding.tvSosCount.text = "${alert.seniorName} needs immediate help!"
        binding.cvSosAlerts.visibility = View.VISIBLE
        binding.layoutSosActions.visibility = View.VISIBLE
        binding.btnViewSos.visibility = View.GONE
        
        Toast.makeText(this, "🚨 EMERGENCY: ${alert.seniorName} requested help!", Toast.LENGTH_LONG).show()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.menu.clear()
        binding.bottomNavigation.inflateMenu(R.menu.bottom_nav_menu_volunteer)
        
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> true
                R.id.navigation_leaderboard -> {
                    val intent = Intent(this, VolunteersListActivity::class.java)
                    intent.putExtra("LEADERBOARD_MODE", true)
                    startActivity(intent)
                    false
                }
                R.id.navigation_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    false 
                }
                R.id.navigation_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    false
                }
                else -> false
            }
        }
    }

    private fun setupDrawerNavigation() {
        binding.navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> binding.drawerLayout.closeDrawer(GravityCompat.START)
                R.id.nav_leaderboard -> {
                    val intent = Intent(this, VolunteersListActivity::class.java)
                    intent.putExtra("LEADERBOARD_MODE", true)
                    startActivity(intent)
                }
                R.id.nav_profile -> startActivity(Intent(this, ProfileActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
                R.id.nav_logout -> {
                    auth.signOut()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupListeners() {
        binding.swAvailability.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) "Available" else "Unavailable"
            binding.tvStatus.text = "Status: $status"
            if (!isChecked) {
                binding.cvSosAlerts.visibility = View.GONE
            }
        }
        
        binding.btnAcceptSos.setOnClickListener {
            currentActiveAlert?.let { alert ->
                acceptSOS(alert)
            }
        }
        
        binding.btnRejectSos.setOnClickListener {
            binding.cvSosAlerts.visibility = View.GONE
        }
        
        binding.btnNearbyMap.setOnClickListener {
            Toast.makeText(this, "Opening Help Finder...", Toast.LENGTH_SHORT).show()
        }
        
        binding.btnHelpHistory.setOnClickListener {
            Toast.makeText(this, "Loading History...", Toast.LENGTH_SHORT).show()
        }
        
        binding.btnCommunity.setOnClickListener {
            Toast.makeText(this, "Opening Community Forum...", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun acceptSOS(alert: SOSAlert) {
        val user = auth.currentUser
        val volunteerId = user?.uid ?: "demo_volunteer"
        val volunteerName = user?.displayName ?: "A volunteer"
        
        FirestoreRepository.acceptSOS(alert.id, volunteerId, volunteerName, onSuccess = {
            runOnUiThread {
                binding.layoutSosActions.visibility = View.GONE
                binding.btnViewSos.visibility = View.VISIBLE
                binding.tvSosCount.text = "You are on your way to ${alert.seniorName}!"
                openGoogleMapsDirections(alert.latitude, alert.longitude)
            }
        }, onFailure = {
            Toast.makeText(this, "Failed to accept SOS: ${it.message}", Toast.LENGTH_SHORT).show()
        })
    }
    
    private fun openGoogleMapsDirections(lat: Double, lon: Double) {
        val gmmIntentUri = Uri.parse("google.navigation:q=$lat,$lon")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        
        try {
            startActivity(mapIntent)
        } catch (e: Exception) {
            val browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lon")
            val browserIntent = Intent(Intent.ACTION_VIEW, browserUri)
            startActivity(browserIntent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sosListener?.remove()
    }
}
