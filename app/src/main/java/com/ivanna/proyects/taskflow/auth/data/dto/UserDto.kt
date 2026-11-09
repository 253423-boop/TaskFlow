package com.ivanna.proyects.taskflow.auth.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    @SerialName("id")
    val id: Int,
    @SerialName("nombre")
    val nombre: String,
    @SerialName("correo")
    val correo: String
)
