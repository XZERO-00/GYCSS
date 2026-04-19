package com.gycss.app.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gycss.app.data.local.PreferenceManager
import com.gycss.app.data.model.Role
import com.gycss.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class NavigationEvent {
    object ToOnboarding : NavigationEvent()
    object ToRoleSelection : NavigationEvent()
    object ToSeniorDashboard : NavigationEvent()
    object ToVolunteerDashboard : NavigationEvent()
    object ToAdminDashboard : NavigationEvent()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _navigationEvent = MutableLiveData<NavigationEvent>()
    val navigationEvent: LiveData<NavigationEvent> = _navigationEvent

    fun decideNextScreen(delayMillis: Long = 2000) {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            
            try {
                // Check if this is the very first time the app is opened
                if (preferenceManager.isFirstAppOpen()) {
                    enforceSplashDelay(startTime, delayMillis)
                    _navigationEvent.postValue(NavigationEvent.ToOnboarding)
                    return@launch
                }

                if (authRepository.isUserLoggedIn()) {
                    val user = authRepository.getCurrentUser()
                    val role = user?.role ?: preferenceManager.getUserRole()
                    
                    enforceSplashDelay(startTime, delayMillis)

                    if (role != null) {
                        preferenceManager.saveUserRole(role)
                        when (role) {
                            Role.SENIOR -> _navigationEvent.postValue(NavigationEvent.ToSeniorDashboard)
                            Role.VOLUNTEER -> _navigationEvent.postValue(NavigationEvent.ToVolunteerDashboard)
                            Role.ADMIN -> _navigationEvent.postValue(NavigationEvent.ToAdminDashboard)
                        }
                    } else {
                        _navigationEvent.postValue(NavigationEvent.ToRoleSelection)
                    }
                } else {
                    enforceSplashDelay(startTime, delayMillis)
                    _navigationEvent.postValue(NavigationEvent.ToRoleSelection)
                }
            } catch (e: Exception) {
                enforceSplashDelay(startTime, delayMillis)
                _navigationEvent.postValue(NavigationEvent.ToRoleSelection)
            }
        }
    }

    private suspend fun enforceSplashDelay(startTime: Long, delayMillis: Long) {
        val elapsed = System.currentTimeMillis() - startTime
        if (elapsed < delayMillis) delay(delayMillis - elapsed)
    }
}
