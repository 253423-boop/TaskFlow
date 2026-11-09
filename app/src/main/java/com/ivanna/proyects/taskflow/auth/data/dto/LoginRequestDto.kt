package com.ivanna.proyects.taskflow.auth.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    @SerialName("correo")
    val correo: String,
    @SerialName("password")
    val password: String
)
