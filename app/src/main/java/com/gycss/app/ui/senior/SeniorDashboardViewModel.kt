package com.gycss.app.ui.senior

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gycss.app.data.model.EmergencyAlert
import com.gycss.app.data.model.HelpRequest
import com.gycss.app.data.model.User
import com.gycss.app.data.repository.AuthRepository
import com.gycss.app.data.repository.EmergencyRepository
import com.gycss.app.data.repository.HelpRequestRepository
import com.gycss.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SeniorDashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val emergencyRepository: EmergencyRepository,
    private val helpRequestRepository: HelpRequestRepository
) : ViewModel() {

    private val _userProfile = MutableLiveData<User?>()
    val userProfile: LiveData<User?> = _userProfile

    private val _sosResult = MutableLiveData<Result<String>>()
    val sosResult: LiveData<Result<String>> = _sosResult

    private val _activeAlert = MutableLiveData<EmergencyAlert?>()
    val activeAlert: LiveData<EmergencyAlert?> = _activeAlert

    private val _activeRequests = MutableLiveData<List<HelpRequest>>()
    val activeRequests: LiveData<List<HelpRequest>> = _activeRequests

    private var sosJob: Job? = null
    private var requestsJob: Job? = null
    private var lastSosTimestamp: Long = 0

    fun fetchUserProfile() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            _userProfile.postValue(user)
            if (user != null) {
                observeActiveRequests(user.uid)
            }
        }
    }

    private fun observeActiveRequests(userId: String) {
        requestsJob?.cancel()
        requestsJob = viewModelScope.launch {
            helpRequestRepository.getSeniorRequests(userId).collectLatest { requests ->
                // Filter for requests that aren't completed or cancelled
                val active = requests.filter { it.status != "Completed" && it.status != "Cancelled" }
                _activeRequests.postValue(active)
            }
        }
    }

    fun triggerSOS(latitude: Double, longitude: Double) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSosTimestamp < 5000) return 
        lastSosTimestamp = currentTime

        viewModelScope.launch {
            val user = _userProfile.value ?: return@launch
            val alert = EmergencyAlert(
                seniorId = user.uid,
                seniorName = user.name,
                location = com.google.firebase.firestore.GeoPoint(latitude, longitude),
                status = "Pending",
                timestamp = currentTime
            )
            val result = emergencyRepository.sendEmergencyAlert(alert)
            _sosResult.postValue(result)
            
            result.onSuccess { alertId ->
                observeSOSAlert(alertId)
            }
        }
    }

    private fun observeSOSAlert(alertId: String) {
        sosJob?.cancel()
        sosJob = viewModelScope.launch {
            emergencyRepository.observeAlert(alertId).collectLatest { alert ->
                _activeAlert.postValue(alert)
                if (alert?.status == "Completed" || alert?.status == "Cancelled") {
                    sosJob?.cancel()
                }
            }
        }
    }

    fun cancelRequest(requestId: String) {
        viewModelScope.launch {
            helpRequestRepository.cancelHelpRequest(requestId)
        }
    }

    fun logout() {
        authRepository.logout()
    }

    override fun onCleared() {
        super.onCleared()
        sosJob?.cancel()
        requestsJob?.cancel()
    }
}
