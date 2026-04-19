package com.gycss.app.ui.senior

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.gycss.app.R
import com.gycss.app.databinding.ActivitySeniorDashboardBinding
import com.gycss.app.ui.auth.RoleSelectionActivity
import com.gycss.app.ui.common.VolunteerReviewActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SeniorDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeniorDashboardBinding
    private val viewModel: SeniorDashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeniorDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        setupBottomNav()
        setupObservers()

        viewModel.fetchUserProfile()
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
                R.id.navigation_sos -> {
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

    private fun setupObservers() {
        viewModel.sosResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, getString(R.string.sos_sent_msg), Toast.LENGTH_LONG).show()
            }.onFailure { 
                Toast.makeText(this, "${getString(R.string.sos_failed_msg)}: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }

        viewModel.activeAlert.observe(this) { alert ->
            alert?.let {
                if (it.status == "Assigned") {
                    showHelpOnTheWayDialog(it.assignedVolunteerName)
                }
            }
        }

        viewModel.requestAcceptedEvent.observe(this) { request ->
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.help_on_way_title))
                .setMessage(getString(R.string.help_on_way_message, request.volunteerName ?: "A volunteer"))
                .setPositiveButton(getString(R.string.btn_continue), null)
                .show()
        }

        viewModel.requestCompletedEvent.observe(this) { request ->
            AlertDialog.Builder(this)
                .setTitle("Request Completed")
                .setMessage("Your request '${request.title}' has been completed. Would you like to rate the volunteer?")
                .setPositiveButton("Rate Now") { _, _ ->
                    val intent = Intent(this, VolunteerReviewActivity::class.java).apply {
                        putExtra("VOLUNTEER_ID", request.volunteerId)
                        putExtra("VOLUNTEER_NAME", request.volunteerName)
                        putExtra("REQUEST_ID", request.requestId)
                    }
                    startActivity(intent)
                }
                .setNegativeButton("Later", null)
                .show()
        }
    }

    fun showCancelConfirmation(requestId: String) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.cancel_request_title))
            .setMessage(getString(R.string.cancel_request_msg))
            .setPositiveButton(getString(R.string.yes)) { _, _ -> viewModel.cancelRequest(requestId) }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    private fun showHelpOnTheWayDialog(volunteerName: String?) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.help_on_way_title))
            .setMessage(getString(R.string.help_on_way_message, volunteerName ?: "A volunteer"))
            .setPositiveButton("OK", null)
            .show()
    }

    private inner class DashboardPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 4
        override fun createFragment(position: Int) = when (position) {
            0 -> SeniorHomeFragment()
            1 -> SeniorSOSFragment()
            2 -> SeniorProfileFragment()
            3 -> SeniorSettingsFragment()
            else -> SeniorHomeFragment()
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
