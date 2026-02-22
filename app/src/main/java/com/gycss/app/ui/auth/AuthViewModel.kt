package com.gycss.app.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gycss.app.data.local.PreferenceManager
import com.gycss.app.data.model.Role
import com.gycss.app.data.model.User
import com.gycss.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Failure(val message: String) : AuthResult()
    object Loading : AuthResult()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _loginResult = MutableLiveData<AuthResult>()
    val loginResult: LiveData<AuthResult> = _loginResult

    private val _registrationResult = MutableLiveData<AuthResult>()
    val registrationResult: LiveData<AuthResult> = _registrationResult

    private val _passwordResetResult = MutableLiveData<Result<Unit>>()
    val passwordResetResult: LiveData<Result<Unit>> = _passwordResetResult

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginResult.value = AuthResult.Loading
            val result = authRepository.loginUser(email, password)
            result.onSuccess {
                preferenceManager.saveUserRole(it.role ?: Role.SENIOR) // Default to SENIOR if role is null
                _loginResult.value = AuthResult.Success(it)
            }.onFailure {
                _loginResult.value = AuthResult.Failure(it.message ?: "An unknown error occurred")
            }
        }
    }

    fun register(user: User, password: String) {
        viewModelScope.launch {
            _registrationResult.value = AuthResult.Loading
            val result = authRepository.registerUser(user, password)
            result.onSuccess {
                preferenceManager.saveUserRole(it.role ?: Role.SENIOR)
                _registrationResult.value = AuthResult.Success(it)
            }.onFailure {
                _registrationResult.value = AuthResult.Failure(it.message ?: "An unknown error occurred")
            }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            val result = authRepository.sendPasswordResetEmail(email)
            _passwordResetResult.value = result
        }
    }
}
