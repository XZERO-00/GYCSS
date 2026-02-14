package com.gycss.app.ui.volunteer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.gycss.app.R
import com.gycss.app.data.local.PreferenceManager
import com.gycss.app.data.model.SOSAlert
import com.gycss.app.data.model.Volunteer
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
    
    @Inject
    lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVolunteerDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupListeners()
        setupBottomNavigation()
        setupDrawerNavigation()
        
        loadVolunteerProfile()
        setupSOSListener()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        toggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun loadVolunteerProfile() {
        val userId = auth.currentUser?.uid ?: return
        FirestoreRepository.getVolunteerProfile(userId) { volunteer ->
            runOnUiThread {
                volunteer?.let { updateProfileUI(it) }
            }
        }
    }

    private fun updateProfileUI(volunteer: Volunteer) {
        binding.tvWelcomeName.text = "Hello, ${volunteer.name}"
        binding.tvHelpedCountStat.text = volunteer.helpCount.toString()
        binding.tvRatingStat.text = "%.1f".format(volunteer.rating)
        
        if (volunteer.profileImageUrl.isNotEmpty()) {
            Glide.with(this).load(volunteer.profileImageUrl).placeholder(R.mipmap.ic_launcher_round).into(binding.ivProfilePic)
        }
    }

    private fun setupSOSListener() {
        sosListener = FirestoreRepository.listenForSOSAlerts { alert ->
            runOnUiThread {
                if (binding.swAvailability.isChecked) { showSOSAlert(alert) }
            }
        }
    }

    private fun showSOSAlert(alert: SOSAlert) {
        currentActiveAlert = alert
        binding.tvSosCount.text = getString(R.string.volunteer_sos_notif, alert.seniorName)
        binding.cvSosAlerts.visibility = View.VISIBLE
        binding.layoutSosActions.visibility = View.VISIBLE
        binding.btnViewSos.visibility = View.GONE
    }

    private fun setupListeners() {
        binding.swAvailability.setOnCheckedChangeListener { _, isChecked ->
            binding.tvStatus.text = "Status: ${if (isChecked) "Available" else "Unavailable"}"
            if (!isChecked) binding.cvSosAlerts.visibility = View.GONE
        }

        binding.btnAcceptSos.setOnClickListener {
            currentActiveAlert?.let { acceptSOS(it) }
        }

        binding.btnEvents.setOnClickListener {
            startActivity(Intent(this, VolunteerEventsActivity::class.java))
        }

        binding.btnHelpHistory.setOnClickListener {
            Toast.makeText(this, "Feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun acceptSOS(alert: SOSAlert) {
        val user = auth.currentUser
        FirestoreRepository.acceptSOS(alert.id, user?.uid ?: "", user?.displayName ?: "Volunteer", onSuccess = {
            runOnUiThread {
                binding.layoutSosActions.visibility = View.GONE
                binding.btnViewSos.visibility = View.VISIBLE
                binding.btnViewSos.setOnClickListener { openMaps(alert.latitude, alert.longitude) }
                openMaps(alert.latitude, alert.longitude)
            }
        }, onFailure = {
            Toast.makeText(this, "Failed to accept SOS", Toast.LENGTH_SHORT).show()
        })
    }

    private fun openMaps(lat: Double, lon: Double) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$lat,$lon"))
        intent.setPackage("com.google.android.apps.maps")
        try { startActivity(intent) } catch (e: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lon")))
        }
    }

    private fun setupDrawerNavigation() {
        binding.navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_logout -> {
                    auth.signOut()
                    preferenceManager.clearSession()
                    startActivity(Intent(this, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                }
                R.id.nav_profile -> startActivity(Intent(this, ProfileActivity::class.java))
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_profile -> { startActivity(Intent(this, ProfileActivity::class.java)); false }
                else -> true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sosListener?.remove()
    }
}
