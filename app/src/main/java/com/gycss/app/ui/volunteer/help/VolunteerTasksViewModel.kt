package com.gycss.app.ui.volunteer.help

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gycss.app.data.model.HelpRequest
import com.gycss.app.data.repository.AuthRepository
import com.gycss.app.data.repository.ChatRepository
import com.gycss.app.data.repository.HelpRequestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VolunteerTasksViewModel @Inject constructor(
    private val helpRequestRepository: HelpRequestRepository,
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _tasks = MutableLiveData<List<HelpRequest>>()
    val tasks: LiveData<List<HelpRequest>> = _tasks

    private val _chatId = MutableLiveData<String>()
    val chatId: LiveData<String> = _chatId

    private val _operationResult = MutableLiveData<Result<Unit>>()
    val operationResult: LiveData<Result<Unit>> = _operationResult

    fun fetchTasks(statusList: List<String>) {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            helpRequestRepository.getVolunteerTasks(user.uid, statusList).collectLatest {
                _tasks.postValue(it)
            }
        }
    }

    fun startTask(requestId: String) {
        viewModelScope.launch {
            val result = helpRequestRepository.updateRequestStatusSecurely(requestId, "Accepted", "In Progress")
            _operationResult.postValue(result)
        }
    }

    fun completeTask(requestId: String) {
        viewModelScope.launch {
            // Check if it's In Progress or directly Accepted (if skipping In Progress was allowed before)
            // But we prefer strict: Accepted -> In Progress -> Completed
            // For now, allow both for robustness if In Progress is newly added
            val result = helpRequestRepository.updateRequestStatusSecurely(requestId, "In Progress", "Completed")
            _operationResult.postValue(result)
        }
    }

    fun getOrCreateChat(request: HelpRequest) {
        viewModelScope.launch {
            val volunteerId = authRepository.getCurrentUser()?.uid ?: return@launch
            val id = chatRepository.getOrCreateChat(request.seniorId, volunteerId)
            _chatId.postValue(id)
        }
    }
}
