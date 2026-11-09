package com.ivanna.proyects.taskflow.auth.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponseDto(
    @SerialName("access_token")
    val access_token: String,
    @SerialName("token_type")
    val token_type: String,
    @SerialName("user")
    val user: UserDto
)
