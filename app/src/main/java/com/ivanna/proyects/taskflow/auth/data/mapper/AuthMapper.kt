package com.ivanna.proyects.taskflow.auth.data.mapper

import com.ivanna.proyects.taskflow.auth.data.dto.UserDto
import com.ivanna.proyects.taskflow.auth.domain.model.User

fun UserDto.toDomain(): User = User(
    id = id,
    nombre = nombre,
    correo = correo
)
