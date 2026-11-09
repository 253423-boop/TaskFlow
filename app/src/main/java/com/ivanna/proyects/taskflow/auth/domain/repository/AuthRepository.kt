package com.ivanna.proyects.taskflow.auth.domain.repository

import com.ivanna.proyects.taskflow.auth.domain.model.User

interface AuthRepository {
    suspend fun login(correo: String, password: String): Result<User>
    suspend fun register(
        nombre: String,
        correo: String,
        password: String,
        passwordConfirm: String
    ): Result<User>
    suspend fun getCurrentUser(): Result<User>
    fun logout()
    fun isUserLoggedIn(): Boolean
}
