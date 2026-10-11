package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.UserAccountEntity
import com.example.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    val currentUser: StateFlow<UserAccountEntity?> = authRepository.currentUser

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _authSuccessMessage = MutableStateFlow<String?>(null)
    val authSuccessMessage: StateFlow<String?> = _authSuccessMessage.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.autoLoginDefaultUser()
        }
    }

    fun login(email: String, pass: String) {
        _isLoading.value = true
        _authError.value = null
        viewModelScope.launch {
            val res = authRepository.login(email, pass)
            _isLoading.value = false
            res.fold(
                onSuccess = {
                    _authSuccessMessage.value = "Welcome back, ${it.username}!"
                },
                onFailure = {
                    _authError.value = it.message ?: "Authentication failed."
                }
            )
        }
    }

    fun register(name: String, email: String, pass: String) {
        _isLoading.value = true
        _authError.value = null
        viewModelScope.launch {
            val res = authRepository.register(name, email, pass)
            _isLoading.value = false
            res.fold(
                onSuccess = {
                    _authSuccessMessage.value = "Account created successfully!"
                },
                onFailure = {
                    _authError.value = it.message ?: "Registration failed."
                }
            )
        }
    }

    fun logout() {
        authRepository.logout()
    }

    fun clearError() {
        _authError.value = null
        _authSuccessMessage.value = null
    }
}
