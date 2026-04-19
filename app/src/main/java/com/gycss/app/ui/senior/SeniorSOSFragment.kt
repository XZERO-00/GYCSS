package com.gycss.app.ui.senior

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
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
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.gycss.app.R
import com.gycss.app.databinding.FragmentSeniorHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SeniorSOSFragment : Fragment() {

    private var _binding: FragmentSeniorHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SeniorDashboardViewModel by viewModels({ requireActivity() })

    private var sosHandler = Handler(Looper.getMainLooper())
    private var sosProgress = 0
    private val SOS_HOLD_DURATION = 2000L
    private val PROGRESS_UPDATE_INTERVAL = 20L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSeniorHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Hide other elements to focus on SOS specifically for this fragment
        // Updated to match the IDs in the redesigned fragment_senior_home.xml
        binding.headerBg.visibility = View.GONE
        binding.tvGreetingLabel.visibility = View.GONE
        binding.tvWelcomeName.visibility = View.GONE
        binding.cvProfileAvatar.visibility = View.GONE
        binding.tvActiveRequestsLabel.visibility = View.GONE
        binding.rvActiveRequests.visibility = View.GONE
        
        // Hiding the new service and community elements
        binding.btnHealthVitals.visibility = View.GONE
        binding.btnMedicalRecords.visibility = View.GONE
        binding.btnReminders.visibility = View.GONE
        binding.btnGrocery.visibility = View.GONE
        binding.btnViewVolunteers.visibility = View.GONE
        binding.btnLeaderboard.visibility = View.GONE

        // Make the SOS card take more prominence
        val params = binding.cvSosCard.layoutParams as ViewGroup.MarginLayoutParams
        params.height = ViewGroup.LayoutParams.MATCH_PARENT
        binding.cvSosCard.layoutParams = params

        setupSOSLogic()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupSOSLogic() {
        val sosRunnable = object : Runnable {
            override fun run() {
                sosProgress += PROGRESS_UPDATE_INTERVAL.toInt()
                binding.pbSosHold.progress = sosProgress
                
                if (sosProgress >= SOS_HOLD_DURATION.toInt()) {
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

    private fun checkLocationAndTriggerSOS() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
            return
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                viewModel.triggerSOS(location?.latitude ?: 0.0, location?.longitude ?: 0.0)
                Toast.makeText(requireContext(), "Emergency SOS Sent!", Toast.LENGTH_LONG).show()
            }
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
            checkLocationAndTriggerSOS()
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
