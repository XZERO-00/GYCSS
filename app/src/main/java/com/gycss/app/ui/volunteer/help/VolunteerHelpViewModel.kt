package com.gycss.app.ui.volunteer.help

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gycss.app.data.model.HelpRequest
import com.gycss.app.data.repository.AuthRepository
import com.gycss.app.data.repository.HelpRequestRepository
import com.gycss.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VolunteerHelpViewModel @Inject constructor(
    private val helpRequestRepository: HelpRequestRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _availableRequests = MutableLiveData<List<HelpRequest>>()
    val availableRequests: LiveData<List<HelpRequest>> = _availableRequests

    private val _acceptResult = MutableLiveData<Result<Unit>>()
    val acceptResult: LiveData<Result<Unit>> = _acceptResult

    fun fetchAvailableRequests() {
        viewModelScope.launch {
            helpRequestRepository.getAvailableRequests().collectLatest { requests ->
                _availableRequests.postValue(requests)
            }
        }
    }

    fun acceptRequest(requestId: String) {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            val result = helpRequestRepository.acceptRequestSecurely(requestId, user)
            _acceptResult.postValue(result)
        }
    }
}
