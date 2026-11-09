package com.ivanna.proyects.taskflow.auth.presentation.viewmodels

object AuthValidation {
    private val emailRegex = Regex(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$"
    )

    fun isValidEmail(email: String): Boolean {
        return emailRegex.matches(email.trim())
    }
}
