package com.gycss.app.ui.volunteer

import android.util.Log
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
import kotlinx.coroutines.flow.catch
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

    private val _rank = MutableLiveData<Int>()
    val rank: LiveData<Int> = _rank

    private var sosJob: Job? = null

    fun fetchUserProfile() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            _userProfile.postValue(user)
            if (user != null) {
                if (user.isAvailable) listenForSOSAlerts()
                calculateRank(user.uid)
            }
        }
    }

    private fun calculateRank(uid: String) {
        viewModelScope.launch {
            userRepository.getAllVolunteers().onSuccess { volunteers ->
                val sorted = volunteers.sortedByDescending { it.helpCount }
                val position = sorted.indexOfFirst { it.uid == uid }
                if (position != -1) {
                    _rank.postValue(position + 1)
                }
            }
        }
    }

    fun updateAvailability(isAvailable: Boolean) {
        viewModelScope.launch {
            val currentUser = _userProfile.value ?: return@launch
            val updatedUser = currentUser.copy(isAvailable = isAvailable)
            val result = userRepository.updateProfile(updatedUser)
            if (result.isSuccess) {
                _userProfile.postValue(updatedUser)
                if (isAvailable) listenForSOSAlerts() else stopListeningForSOSAlerts()
            }
        }
    }

    fun updateSettings(
        radius: Float? = null,
        notifs: Boolean? = null,
        sosSound: Boolean? = null,
        visible: Boolean? = null
    ) {
        viewModelScope.launch {
            val user = _userProfile.value ?: return@launch
            val updatedUser = user.copy(
                helpRadius = radius ?: user.helpRadius,
                notificationsEnabled = notifs ?: user.notificationsEnabled,
                sosSoundEnabled = sosSound ?: user.sosSoundEnabled,
                profileVisible = visible ?: user.profileVisible
            )
            userRepository.updateProfile(updatedUser).onSuccess {
                _userProfile.postValue(updatedUser)
            }
        }
    }

    fun listenForSOSAlerts() {
        sosJob?.cancel()
        sosJob = viewModelScope.launch {
            emergencyRepository.observePendingAlerts()
                .catch { e ->
                    Log.e("VolunteerVM", "Error observing alerts: ${e.message}")
                    _sosAlerts.postValue(emptyList())
                }
                .collectLatest { alerts ->
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

    fun logout() {
        authRepository.logout()
    }

    override fun onCleared() {
        super.onCleared()
        sosJob?.cancel()
    }
}
