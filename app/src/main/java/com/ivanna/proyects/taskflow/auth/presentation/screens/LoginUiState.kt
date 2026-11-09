package com.ivanna.proyects.taskflow.auth.presentation.screens

enum class LoginErrorKind {
    INVALID_CREDENTIALS,
    CONNECTION,
    GENERIC
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailErrorRes: Int? = null,
    val passwordErrorRes: Int? = null,
    val serverErrorRes: Int? = null,
    val serverErrorMessage: String? = null,
    val serverErrorKind: LoginErrorKind? = null,
    val isSessionExpiredNotice: Boolean = false,
    val isLoading: Boolean = false,
    val isLoginSuccess: Boolean = false
)
