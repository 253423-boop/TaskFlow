package com.ivanna.proyects.taskflow

import com.ivanna.proyects.taskflow.auth.data.dto.UserDto
import com.ivanna.proyects.taskflow.auth.data.mapper.toDomain
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthMapperTest {

    @Test
    fun userDtoToDomainMapsAllFields() {
        val dto = UserDto(
            id = 42,
            nombre = "Ivanna",
            correo = "ivanna@upchiapas.edu.mx"
        )

        val user = dto.toDomain()

        assertEquals(42, user.id)
        assertEquals("Ivanna", user.nombre)
        assertEquals("ivanna@upchiapas.edu.mx", user.correo)
    }
}
