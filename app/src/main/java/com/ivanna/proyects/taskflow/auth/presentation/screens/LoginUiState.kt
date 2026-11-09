package com.ivanna.proyects.taskflow.auth.presentation.screens

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailErrorRes: Int? = null,
    val passwordErrorRes: Int? = null,
    val serverError: String? = null,
    val isLoading: Boolean = false,
    val isLoginSuccess: Boolean = false
)
