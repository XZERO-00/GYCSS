package com.gycss.app.ui.senior.help

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gycss.app.data.model.HelpRequest
import com.gycss.app.data.repository.AuthRepository
import com.gycss.app.data.repository.HelpRequestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HelpRequestViewModel @Inject constructor(
    private val helpRequestRepository: HelpRequestRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _requestResult = MutableLiveData<Result<Unit>>()
    val requestResult: LiveData<Result<Unit>> = _requestResult

    private val _myRequests = MutableLiveData<List<HelpRequest>>()
    val myRequests: LiveData<List<HelpRequest>> = _myRequests

    fun createHelpRequest(title: String, description: String, category: String) {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            val request = HelpRequest(
                seniorId = user.uid,
                seniorName = user.name,
                title = title,
                description = description,
                category = category,
                status = "Pending"
            )
            val result = helpRequestRepository.createHelpRequest(request)
            _requestResult.postValue(result)
        }
    }

    fun fetchMyRequests() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            helpRequestRepository.getSeniorRequests(user.uid).collectLatest {
                _myRequests.postValue(it)
            }
        }
    }

    fun confirmCompletion(requestId: String) {
        viewModelScope.launch {
            val result = helpRequestRepository.updateRequestStatusSecurely(
                requestId, 
                "CompletedByVolunteer", 
                "Completed"
            )
            _requestResult.postValue(result)
        }
    }
    
    fun rejectCompletion(requestId: String) {
        viewModelScope.launch {
            // If senior says it's not done, move it back to In Progress
            val result = helpRequestRepository.updateRequestStatusSecurely(
                requestId, 
                "CompletedByVolunteer", 
                "In Progress"
            )
            _requestResult.postValue(result)
        }
    }
}
