package com.gycss.app.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gycss.app.data.local.PreferenceManager
import com.gycss.app.data.model.User
import com.gycss.app.data.repository.AuthRepository
import com.gycss.app.data.repository.UserRepository
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
    private val userRepository: UserRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _loginResult = MutableLiveData<AuthResult>()
    val loginResult: LiveData<AuthResult> = _loginResult

    private val _registrationResult = MutableLiveData<AuthResult>()
    val registrationResult: LiveData<AuthResult> = _registrationResult

    private val _passwordResetResult = MutableLiveData<Result<Unit>>()
    val passwordResetResult: LiveData<Result<Unit>> = _passwordResetResult

    private val _userCount = MutableLiveData<Long>()
    val userCount: LiveData<Long> = _userCount

    fun fetchUserCount() {
        viewModelScope.launch {
            userRepository.getTotalUserCount().onSuccess {
                _userCount.value = it
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginResult.value = AuthResult.Loading
            val result = authRepository.loginUser(email, password)
            result.onSuccess {
                it.role?.let { role -> preferenceManager.saveUserRole(role) }
                _loginResult.value = AuthResult.Success(it)
            }.onFailure {
                val message = when(it.message) {
                    "WRONG_CREDENTIALS" -> "Invalid email or password"
                    "PROFILE_MISSING" -> "User profile not found. Please register."
                    "PROFILE_CORRUPTED" -> "Account error. Please contact support."
                    "NETWORK_ERROR" -> "No internet connection. Please try again."
                    "TOO_MANY_REQUESTS" -> "Too many attempts. Please try again later."
                    "USER_NOT_FOUND" -> "Account not found or disabled."
                    else -> it.message ?: "Login failed"
                }
                _loginResult.value = AuthResult.Failure(message)
            }
        }
    }

    fun register(user: User, password: String) {
        viewModelScope.launch {
            _registrationResult.value = AuthResult.Loading
            val result = authRepository.registerUser(user, password)
            result.onSuccess {
                it.role?.let { role -> preferenceManager.saveUserRole(role) }
                _registrationResult.value = AuthResult.Success(it)
            }.onFailure {
                val message = when(it.message) {
                    "EMAIL_EXISTS" -> "This email is already registered"
                    "WEAK_PASSWORD" -> "The password is too weak"
                    "INVALID_EMAIL" -> "The email address is badly formatted"
                    "NETWORK_ERROR" -> "No internet connection. Please try again."
                    "TOO_MANY_REQUESTS" -> "Too many attempts. Please try again later."
                    "DATABASE_ERROR" -> "Failed to create profile. Please try again."
                    else -> it.message ?: "Registration failed"
                }
                _registrationResult.value = AuthResult.Failure(message)
            }
        }
    }

    fun logout() {
        authRepository.logout()
        preferenceManager.clearSession()
    }

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            val result = authRepository.sendPasswordResetEmail(email)
            _passwordResetResult.value = result
        }
    }
}
