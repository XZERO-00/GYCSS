package com.gycss.app.ui.volunteer.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gycss.app.data.model.User
import com.gycss.app.data.repository.AuthRepository
import com.gycss.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VolunteerProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _userProfile = MutableLiveData<User?>()
    val userProfile: LiveData<User?> = _userProfile

    private val _updateResult = MutableLiveData<Result<Unit>>()
    val updateResult: LiveData<Result<Unit>> = _updateResult

    fun fetchProfile() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            _userProfile.postValue(user)
        }
    }

    fun updateProfile(name: String, bio: String, skills: String) {
        viewModelScope.launch {
            val currentUser = _userProfile.value ?: return@launch
            val updatedUser = currentUser.copy(
                name = name,
                bio = bio,
                skills = skills
            )
            val result = userRepository.updateProfile(updatedUser)
            _updateResult.postValue(result)
            if (result.isSuccess) {
                _userProfile.postValue(updatedUser)
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }
}
