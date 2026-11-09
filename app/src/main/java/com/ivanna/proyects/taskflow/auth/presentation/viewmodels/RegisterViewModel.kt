package com.ivanna.proyects.taskflow.auth.presentation.viewmodels

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivanna.proyects.taskflow.auth.domain.repository.AuthRepository
import com.ivanna.proyects.taskflow.auth.presentation.screens.RegisterUiState
import com.upchiapas.taskflow.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.update {
            it.copy(
                name = name,
                nameErrorRes = null,
                serverError = null
            )
        }
    }

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

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update {
            it.copy(
                confirmPassword = confirmPassword,
                confirmPasswordErrorRes = null,
                serverError = null
            )
        }
    }

    fun clearServerError() {
        _uiState.update { it.copy(serverError = null) }
    }

    fun onRegisterSubmit() {
        val currentState = _uiState.value
        if (currentState.isLoading) {
            return
        }

        val trimmedName = currentState.name.trim()
        val trimmedEmail = currentState.email.trim()

        val nameErrorRes = if (trimmedName.isBlank()) {
            R.string.error_required_field
        } else {
            null
        }

        val emailErrorRes = if (
            trimmedEmail.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()
        ) {
            R.string.error_invalid_email
        } else {
            null
        }

        val passwordErrorRes = if (currentState.password.length < MIN_PASSWORD_LENGTH) {
            R.string.error_short_password
        } else {
            null
        }

        val confirmPasswordErrorRes = if (
            currentState.confirmPassword.isEmpty() || currentState.confirmPassword != currentState.password
        ) {
            R.string.error_passwords_mismatch
        } else {
            null
        }

        if (
            nameErrorRes != null ||
            emailErrorRes != null ||
            passwordErrorRes != null ||
            confirmPasswordErrorRes != null
        ) {
            _uiState.update {
                it.copy(
                    nameErrorRes = nameErrorRes,
                    emailErrorRes = emailErrorRes,
                    passwordErrorRes = passwordErrorRes,
                    confirmPasswordErrorRes = confirmPasswordErrorRes,
                    serverError = null
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                nameErrorRes = null,
                emailErrorRes = null,
                passwordErrorRes = null,
                confirmPasswordErrorRes = null,
                serverError = null,
                isLoading = true
            )
        }

        viewModelScope.launch {
            authRepository.register(
                nombre = trimmedName,
                correo = trimmedEmail,
                password = currentState.password,
                passwordConfirm = currentState.confirmPassword
            ).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRegisterSuccess = true
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

    private companion object {
        const val MIN_PASSWORD_LENGTH = 8
    }
}
