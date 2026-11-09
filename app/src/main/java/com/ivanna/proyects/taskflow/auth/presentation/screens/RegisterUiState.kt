package com.ivanna.proyects.taskflow.auth.presentation.screens

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nameErrorRes: Int? = null,
    val emailErrorRes: Int? = null,
    val passwordErrorRes: Int? = null,
    val confirmPasswordErrorRes: Int? = null,
    val serverErrorRes: Int? = null,
    val serverErrorMessage: String? = null,
    val isLoading: Boolean = false,
    val isRegisterSuccess: Boolean = false
)
