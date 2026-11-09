package com.ivanna.proyects.taskflow.auth.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestDto(
    @SerialName("nombre")
    val nombre: String,
    @SerialName("correo")
    val correo: String,
    @SerialName("password")
    val password: String,
    @SerialName("password_confirm")
    val passwordConfirm: String
)
