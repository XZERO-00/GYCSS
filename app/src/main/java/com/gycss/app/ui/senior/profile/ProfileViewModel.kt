package com.gycss.app.ui.senior.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gycss.app.data.model.User
import com.gycss.app.data.repository.AuthRepository
import com.gycss.app.data.repository.UserRepository
import com.gycss.app.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _userProfile = MutableLiveData<User?>()
    val userProfile: LiveData<User?> = _userProfile

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _updateResult = MutableLiveData<Result<String>>()
    val updateResult: LiveData<Result<String>> = _updateResult

    fun fetchProfile() {
        _loading.value = true
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            _userProfile.postValue(user)
            _loading.postValue(false)
        }
    }

    fun updateProfile(updatedUser: User, imageUri: Uri?) {
        _loading.value = true
        viewModelScope.launch {
            if (imageUri != null) {
                FirestoreRepository.uploadProfileImage(updatedUser.uid, imageUri, { url ->
                    saveProfileData(updatedUser.copy(profileImageUrl = url))
                }, {
                    _loading.postValue(false)
                    _updateResult.postValue(Result.failure(it))
                })
            } else {
                saveProfileData(updatedUser)
            }
        }
    }

    private fun saveProfileData(user: User) {
        viewModelScope.launch {
            val result = userRepository.updateProfile(user)
            _loading.postValue(false)
            if (result.isSuccess) {
                _userProfile.postValue(user)
                _updateResult.postValue(Result.success("Profile updated successfully"))
            } else {
                _updateResult.postValue(Result.failure(result.exceptionOrNull() ?: Exception("Unknown error")))
            }
        }
    }
}
