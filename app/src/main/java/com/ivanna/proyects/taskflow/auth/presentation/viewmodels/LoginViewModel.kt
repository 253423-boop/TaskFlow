package com.ivanna.proyects.taskflow.auth.presentation.viewmodels

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivanna.proyects.taskflow.auth.domain.repository.AuthRepository
import com.ivanna.proyects.taskflow.auth.presentation.screens.LoginUiState
import com.upchiapas.taskflow.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update {
            it.copy(
                email = email,
                emailErrorRes = null,
                serverError = null
            )
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                passwordErrorRes = null,
                serverError = null
            )
        }
    }

    fun clearServerError() {
        _uiState.update { it.copy(serverError = null) }
    }

    fun onLoginSubmit() {
        val currentState = _uiState.value
        if (currentState.isLoading) {
            return
        }

        val trimmedEmail = currentState.email.trim()
        val emailErrorRes = when {
            trimmedEmail.isEmpty() -> R.string.error_required_field
            !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches() -> R.string.error_invalid_email
            else -> null
        }
        val passwordErrorRes = if (currentState.password.isEmpty()) {
            R.string.error_required_field
        } else {
            null
        }

        if (emailErrorRes != null || passwordErrorRes != null) {
            _uiState.update {
                it.copy(
                    emailErrorRes = emailErrorRes,
                    passwordErrorRes = passwordErrorRes,
                    serverError = null
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                emailErrorRes = null,
                passwordErrorRes = null,
                serverError = null,
                isLoading = true
            )
        }

        viewModelScope.launch {
            authRepository.login(trimmedEmail, currentState.password).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoginSuccess = true
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            serverError = throwable.message
                        )
                    }
                }
            )
        }
    }
}
