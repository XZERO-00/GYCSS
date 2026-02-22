package com.gycss.app.ui.volunteer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gycss.app.data.model.EmergencyAlert
import com.gycss.app.data.model.User
import com.gycss.app.data.repository.AuthRepository
import com.gycss.app.data.repository.EmergencyRepository
import com.gycss.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VolunteerDashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val emergencyRepository: EmergencyRepository
) : ViewModel() {

    private val _userProfile = MutableLiveData<User?>()
    val userProfile: LiveData<User?> = _userProfile

    private val _sosAlerts = MutableLiveData<List<EmergencyAlert>>()
    val sosAlerts: LiveData<List<EmergencyAlert>> = _sosAlerts

    private val _acceptResult = MutableLiveData<Result<Unit>>()
    val acceptResult: LiveData<Result<Unit>> = _acceptResult

    private val _completeResult = MutableLiveData<Result<Unit>>()
    val completeResult: LiveData<Result<Unit>> = _completeResult

    private val _availabilityUpdateResult = MutableLiveData<Result<Unit>>()
    val availabilityUpdateResult: LiveData<Result<Unit>> = _availabilityUpdateResult

    private var sosJob: Job? = null

    fun fetchUserProfile() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            _userProfile.postValue(user)
            if (user?.isAvailable == true) {
                listenForSOSAlerts()
            }
        }
    }

    fun updateAvailability(isAvailable: Boolean) {
        viewModelScope.launch {
            val currentUser = _userProfile.value ?: return@launch
            val updatedUser = currentUser.copy(isAvailable = isAvailable)
            val result = userRepository.updateProfile(updatedUser)
            _availabilityUpdateResult.postValue(result)
            if (result.isSuccess) {
                _userProfile.postValue(updatedUser)
                if (isAvailable) {
                    listenForSOSAlerts()
                } else {
                    stopListeningForSOSAlerts()
                }
            }
        }
    }

    fun listenForSOSAlerts() {
        sosJob?.cancel()
        sosJob = viewModelScope.launch {
            emergencyRepository.observePendingAlerts().collectLatest { alerts ->
                _sosAlerts.postValue(alerts)
            }
        }
    }

    fun stopListeningForSOSAlerts() {
        sosJob?.cancel()
        _sosAlerts.postValue(emptyList())
    }

    fun acceptSOS(alertId: String) {
        viewModelScope.launch {
            val user = _userProfile.value ?: return@launch
            if (!user.isAvailable) {
                _acceptResult.postValue(Result.failure(Exception("You must be available to accept SOS alerts")))
                return@launch
            }
            val result = emergencyRepository.acceptAlert(alertId, user)
            _acceptResult.postValue(result)
        }
    }

    fun completeSOS(alertId: String) {
        viewModelScope.launch {
            val result = emergencyRepository.completeAlert(alertId)
            _completeResult.postValue(result)
        }
    }

    fun logout() {
        authRepository.logout()
    }

    override fun onCleared() {
        super.onCleared()
        sosJob?.cancel()
    }
}
