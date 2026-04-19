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

    private val _acceptResult = MutableLiveData<Result<String>>() // Returns chatId on success
    val acceptResult: LiveData<Result<String>> = _acceptResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun fetchAvailableRequests() {
        viewModelScope.launch {
            helpRequestRepository.getAvailableRequests().collectLatest { requests ->
                _availableRequests.postValue(requests)
            }
        }
    }

    fun acceptRequest(request: HelpRequest) {
        if (_isLoading.value == true) return
        
        viewModelScope.launch {
            _isLoading.value = true
            val user = authRepository.getCurrentUser() ?: run {
                _isLoading.value = false
                return@launch
            }
            
            val result = helpRequestRepository.acceptRequestSecurely(request.requestId, user)
            
            result.onSuccess {
                // Generate deterministic Chat ID: seniorId_volunteerId_requestId
                val chatId = "${request.seniorId}_${user.uid}_${request.requestId}"
                _acceptResult.postValue(Result.success(chatId))
            }.onFailure {
                _acceptResult.postValue(Result.failure(it))
            }
            _isLoading.value = false
        }
    }
}
