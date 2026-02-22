package com.gycss.app.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.gycss.app.data.local.PreferenceManager
import com.gycss.app.data.model.Role
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class NavigationEvent {
    object ToRoleSelection : NavigationEvent()
    object ToSeniorDashboard : NavigationEvent()
    object ToVolunteerDashboard : NavigationEvent()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _navigationEvent = MutableLiveData<NavigationEvent>()
    val navigationEvent: LiveData<NavigationEvent> = _navigationEvent

    fun decideNextScreen(delayMillis: Long = 2500) {
        viewModelScope.launch {
            delay(delayMillis)

            val currentUser = auth.currentUser
            val savedRole = preferenceManager.getUserRole()

            if (currentUser != null && savedRole != null) {
                when (savedRole) {
                    Role.SENIOR -> _navigationEvent.postValue(NavigationEvent.ToSeniorDashboard)
                    Role.VOLUNTEER -> _navigationEvent.postValue(NavigationEvent.ToVolunteerDashboard)
                    else -> _navigationEvent.postValue(NavigationEvent.ToRoleSelection)
                }
            } else {
                _navigationEvent.postValue(NavigationEvent.ToRoleSelection)
            }
        }
    }
}
