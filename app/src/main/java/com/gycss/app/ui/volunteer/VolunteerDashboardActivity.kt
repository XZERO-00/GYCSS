package com.gycss.app.ui.volunteer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.gycss.app.R
import com.gycss.app.databinding.ActivityVolunteerDashboardBinding
import com.gycss.app.service.MyFirebaseMessagingService
import com.gycss.app.ui.auth.RoleSelectionActivity
import com.gycss.app.ui.volunteer.help.VolunteerTasksFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VolunteerDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVolunteerDashboardBinding
    private val viewModel: VolunteerDashboardViewModel by viewModels()

    private val sosReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val title = intent.getStringExtra("title") ?: "Emergency SOS"
            val body = intent.getStringExtra("body") ?: "A senior needs help!"
            showSosDialog(title, body)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVolunteerDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        setupBottomNav()

        viewModel.fetchUserProfile()
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val type = intent?.getStringExtra("type")
        if (type == "SOS") {
            // Switch to Home or Tasks tab to show the alert card
            binding.viewPager.currentItem = 0 
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            sosReceiver, IntentFilter(MyFirebaseMessagingService.ACTION_SOS_RECEIVED)
        )
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(sosReceiver)
    }

    private fun showSosDialog(title: String, body: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(body)
            .setPositiveButton("View Details") { _, _ ->
                binding.viewPager.currentItem = 0
            }
            .setNegativeButton("Dismiss", null)
            .setIcon(R.drawable.ic_sos)
            .show()
    }

    private fun setupViewPager() {
        val adapter = DashboardPagerAdapter(this)
        binding.viewPager.adapter = adapter
        binding.viewPager.isUserInputEnabled = false

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.bottomNavigation.menu.getItem(position).isChecked = true
            }
        })
    }

    private fun setupBottomNav() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    binding.viewPager.currentItem = 0
                    true
                }
                R.id.navigation_tasks -> {
                    binding.viewPager.currentItem = 1
                    true
                }
                R.id.navigation_profile -> {
                    binding.viewPager.currentItem = 2
                    true
                }
                R.id.navigation_settings -> {
                    binding.viewPager.currentItem = 3
                    true
                }
                else -> false
            }
        }
    }

    private inner class DashboardPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 4
        override fun createFragment(position: Int) = when (position) {
            0 -> VolunteerHomeFragment()
            1 -> VolunteerTasksFragment()
            2 -> VolunteerProfileFragment()
            3 -> VolunteerSettingsFragment()
            else -> VolunteerHomeFragment()
        }
    }

    fun logout() {
        viewModel.logout()
        startActivity(Intent(this, RoleSelectionActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
