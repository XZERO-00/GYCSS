package com.gycss.app.ui.volunteer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.gycss.app.R
import com.gycss.app.databinding.FragmentVolunteerHomeBinding
import com.gycss.app.ui.common.VolunteersListActivity
import com.gycss.app.ui.volunteer.help.AvailableRequestsActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VolunteerHomeFragment : Fragment() {

    private var _binding: FragmentVolunteerHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VolunteerDashboardViewModel by viewModels({ requireActivity() })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVolunteerHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        setupObservers()
    }

    private fun setupClickListeners() {
        binding.swAvailability.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateAvailability(isChecked)
            binding.tvStatus.text = if (isChecked) getString(R.string.online_status) else getString(R.string.offline_status)
        }

        binding.btnAcceptSos.setOnClickListener { 
            viewModel.sosAlerts.value?.firstOrNull()?.let { alert ->
                viewModel.acceptSOS(alert.alertId)
            }
        }

        binding.btnViewRequests.setOnClickListener {
            startActivity(Intent(requireContext(), AvailableRequestsActivity::class.java))
        }

        binding.btnEvents.setOnClickListener {
            // Switch to Tasks tab (Help History)
            (activity as? VolunteerDashboardActivity)?.let {
                it.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)?.selectedItemId = R.id.navigation_tasks
            }
        }
        
        binding.btnLeaderboard.setOnClickListener {
            val intent = Intent(requireContext(), VolunteersListActivity::class.java)
            intent.putExtra("LEADERBOARD_MODE", true)
            startActivity(intent)
        }
        
        binding.btnProfile.setOnClickListener {
            (activity as? VolunteerDashboardActivity)?.let {
                it.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)?.selectedItemId = R.id.navigation_profile
            }
        }
    }

    private fun setupObservers() {
        viewModel.userProfile.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvWelcomeName.text = getString(R.string.hello_user, it.name)
                binding.swAvailability.isChecked = it.isAvailable
                binding.tvStatus.text = if (it.isAvailable) getString(R.string.online_status) else getString(R.string.offline_status)
                binding.tvHelpedCountStat.text = it.helpCount.toString()
                binding.tvRatingStat.text = String.format("%.1f", it.rating)
            }
        }

        viewModel.rank.observe(viewLifecycleOwner) { rank ->
            binding.tvRankStat.text = "#$rank"
        }

        viewModel.sosAlerts.observe(viewLifecycleOwner) { alerts ->
            val latestAlert = alerts.firstOrNull()
            if (latestAlert != null && binding.swAvailability.isChecked) {
                binding.cvSosAlerts.visibility = View.VISIBLE
                binding.tvSosCount.text = alerts.size.toString()
                binding.tvSosCount.visibility = if (alerts.size > 1) View.VISIBLE else View.GONE
                binding.tvSosDetails.text = latestAlert.seniorName
            } else {
                binding.cvSosAlerts.visibility = View.GONE
            }
        }

        viewModel.acceptResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(requireContext(), "SOS Accepted! Opening Maps...", Toast.LENGTH_SHORT).show()
                viewModel.sosAlerts.value?.firstOrNull()?.location?.let { geoPoint ->
                    val uri = "google.navigation:q=${geoPoint.latitude},${geoPoint.longitude}"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                    intent.setPackage("com.google.android.apps.maps")
                    startActivity(intent)
                }
            }.onFailure {
                Toast.makeText(requireContext(), "Failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
