package com.ivanna.proyects.taskflow.auth.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivanna.proyects.taskflow.auth.domain.repository.AuthRepository
import com.ivanna.proyects.taskflow.auth.presentation.screens.LoginErrorKind
import com.ivanna.proyects.taskflow.auth.presentation.screens.LoginUiState
import com.ivanna.proyects.taskflow.core.network.ApiException
import com.upchiapas.taskflow.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    initialSessionExpired: Boolean = false
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        LoginUiState(isSessionExpiredNotice = initialSessionExpired)
    )
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update {
            it.copy(
                email = email,
                emailErrorRes = null,
                serverErrorRes = null,
                serverErrorMessage = null,
                serverErrorKind = null,
                isSessionExpiredNotice = false
            )
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                passwordErrorRes = null,
                serverErrorRes = null,
                serverErrorMessage = null,
                serverErrorKind = null,
                isSessionExpiredNotice = false
            )
        }
    }

    fun clearServerError() {
        _uiState.update {
            it.copy(
                serverErrorRes = null,
                serverErrorMessage = null,
                serverErrorKind = null,
                isSessionExpiredNotice = false
            )
        }
    }

    fun onLoginSubmit() {
        val currentState = _uiState.value
        if (currentState.isLoading) {
            return
        }

        val trimmedEmail = currentState.email.trim()
        val emailErrorRes = when {
            trimmedEmail.isEmpty() -> R.string.error_required_field
            !AuthValidation.isValidEmail(trimmedEmail) -> R.string.error_invalid_email
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
                    serverErrorRes = null,
                    serverErrorMessage = null,
                    serverErrorKind = null,
                    isSessionExpiredNotice = false
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                emailErrorRes = null,
                passwordErrorRes = null,
                serverErrorRes = null,
                serverErrorMessage = null,
                serverErrorKind = null,
                isSessionExpiredNotice = false,
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
                    val (errorRes, errorKind, errorDetail) = mapLoginFailure(throwable)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            serverErrorRes = errorRes,
                            serverErrorMessage = errorDetail,
                            serverErrorKind = errorKind
                        )
                    }
                }
            )
        }
    }

    private fun mapLoginFailure(throwable: Throwable): Triple<Int?, LoginErrorKind, String?> {
        return when (throwable) {
            is ApiException.InvalidCredentials ->
                Triple(R.string.error_invalid_credentials, LoginErrorKind.INVALID_CREDENTIALS, null)
            is ApiException.NoConnection ->
                Triple(R.string.error_no_connection, LoginErrorKind.CONNECTION, null)
            is ApiException.HttpError ->
                Triple(
                    if (throwable.detail == null) R.string.error_unexpected else null,
                    LoginErrorKind.GENERIC,
                    throwable.detail
                )
            else ->
                Triple(
                    R.string.error_unexpected,
                    LoginErrorKind.GENERIC,
                    throwable.message?.takeIf { it.isNotBlank() }
                )
        }
    }
}
