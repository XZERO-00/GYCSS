package com.gycss.app.ui.senior

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.gycss.app.R
import com.gycss.app.databinding.FragmentSeniorHomeBinding
import com.gycss.app.ui.common.VolunteersListActivity
import com.gycss.app.ui.senior.help.HelpRequestViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SeniorHomeFragment : Fragment() {

    private var _binding: FragmentSeniorHomeBinding? = null
    private val binding get() = _binding!!
    private val dashboardViewModel: SeniorDashboardViewModel by viewModels({ requireActivity() })
    private val helpRequestViewModel: HelpRequestViewModel by viewModels()
    private lateinit var requestAdapter: ActiveRequestAdapter

    private var sosHandler = Handler(Looper.getMainLooper())
    private var sosProgress = 0
    private val SOS_HOLD_DURATION = 2000L // 2 seconds
    private val PROGRESS_UPDATE_INTERVAL = 20L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSeniorHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        setupSOSLogic()
        setupObservers()
    }

    private fun setupRecyclerView() {
        requestAdapter = ActiveRequestAdapter(
            onCancelClick = { requestId ->
                (activity as? SeniorDashboardActivity)?.showCancelConfirmation(requestId)
            },
            onConfirmClick = { requestId ->
                helpRequestViewModel.confirmCompletion(requestId)
            },
            onRejectClick = { requestId ->
                helpRequestViewModel.rejectCompletion(requestId)
            }
        )
        binding.rvActiveRequests.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = requestAdapter
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupSOSLogic() {
        val sosRunnable = object : Runnable {
            override fun run() {
                sosProgress += PROGRESS_UPDATE_INTERVAL.toInt()
                binding.pbSosHold.progress = sosProgress
                
                if (sosProgress >= SOS_HOLD_DURATION) {
                    triggerSOS()
                    resetSOSProgress()
                } else {
                    sosHandler.postDelayed(this, PROGRESS_UPDATE_INTERVAL)
                }
            }
        }

        binding.btnSos.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    sosProgress = 0
                    binding.pbSosHold.visibility = View.VISIBLE
                    sosHandler.post(sosRunnable)
                    vibrate(100)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    sosHandler.removeCallbacks(sosRunnable)
                    resetSOSProgress()
                }
            }
            true
        }
    }

    private fun resetSOSProgress() {
        sosProgress = 0
        binding.pbSosHold.progress = 0
        binding.pbSosHold.visibility = View.INVISIBLE
    }

    private fun triggerSOS() {
        vibrate(500)
        checkLocationAndTriggerSOS()
    }

    private fun setupClickListeners() {
        binding.btnHealthVitals.setOnClickListener {
            try {
                val intent = Intent(requireContext(), Class.forName("com.gycss.app.ui.senior.HealthVitalsActivity"))
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Coming Soon", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnMedicalRecords.setOnClickListener {
            startActivity(Intent(requireContext(), MedicalRecordsActivity::class.java))
        }

        binding.btnReminders.setOnClickListener {
            startActivity(Intent(requireContext(), MedicationRemindersActivity::class.java))
        }

        binding.btnGrocery.setOnClickListener { openRequestAssistance("Grocery") }

        binding.btnViewVolunteers.setOnClickListener {
            val intent = Intent(requireContext(), VolunteersListActivity::class.java)
            intent.putExtra("LEADERBOARD_MODE", false)
            startActivity(intent)
        }

        binding.btnLeaderboard.setOnClickListener {
            val intent = Intent(requireContext(), VolunteersListActivity::class.java)
            intent.putExtra("LEADERBOARD_MODE", true)
            startActivity(intent)
        }
    }

    private fun openRequestAssistance(type: String) {
        val intent = Intent(requireContext(), RequestAssistanceActivity::class.java).apply {
            putExtra("REQUEST_TYPE", type)
        }
        startActivity(intent)
    }

    private fun setupObservers() {
        dashboardViewModel.userProfile.observe(viewLifecycleOwner) { user ->
            user?.let { 
                binding.tvWelcomeName.text = it.name
                binding.tvAvatarLetter.text = it.name.firstOrNull()?.uppercase() ?: "U"
            }
        }

        dashboardViewModel.activeRequests.observe(viewLifecycleOwner) { requests ->
            requestAdapter.submitList(requests)
            binding.tvActiveRequestsLabel.visibility = if (requests.isNotEmpty()) View.VISIBLE else View.GONE
        }
        
        dashboardViewModel.sosResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { 
                binding.tvSosBtnText.text = getString(R.string.sos_sent_short)
                binding.btnSos.isEnabled = false
                Toast.makeText(requireContext(), getString(R.string.sos_sent_msg), Toast.LENGTH_LONG).show()
            }
        }

        helpRequestViewModel.requestResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(requireContext(), "Update successful", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(requireContext(), "Update failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkLocationAndTriggerSOS() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
            return
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    dashboardViewModel.triggerSOS(location.latitude, location.longitude)
                } else {
                    fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                        dashboardViewModel.triggerSOS(lastLoc?.latitude ?: 0.0, lastLoc?.longitude ?: 0.0)
                    }
                }
            }
            .addOnFailureListener {
                fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                    dashboardViewModel.triggerSOS(lastLoc?.latitude ?: 0.0, lastLoc?.longitude ?: 0.0)
                }
            }
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
            checkLocationAndTriggerSOS()
        } else {
            Toast.makeText(requireContext(), getString(R.string.location_permission_needed), Toast.LENGTH_LONG).show()
        }
    }

    private fun vibrate(duration: Long) {
        val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
