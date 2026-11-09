package com.ivanna.proyects.taskflow

import com.ivanna.proyects.taskflow.tasks.data.dto.TaskDto
import com.ivanna.proyects.taskflow.tasks.data.mapper.toDomain
import com.ivanna.proyects.taskflow.tasks.data.mapper.toDomainList
import com.ivanna.proyects.taskflow.tasks.domain.model.TaskPriority
import com.ivanna.proyects.taskflow.tasks.domain.model.TaskStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskMapperTest {

    @Test
    fun taskDtoToDomainMapsPriorityStatusOwnerCreatedAndBlankDescription() {
        val dto = TaskDto(
            id = 7,
            userId = 3,
            titulo = "Proyecto final",
            materia = "Móviles",
            descripcion = "   ",
            fechaEntrega = "2026-11-10T14:00:00",
            prioridad = 3,
            estado = "completada",
            vencida = true,
            createdAt = "2026-11-05T10:24:00"
        )

        val task = dto.toDomain()

        assertEquals(7, task.id)
        assertEquals(3, task.userId)
        assertEquals("Proyecto final", task.titulo)
        assertEquals("Móviles", task.materia)
        assertNull(task.descripcion)
        assertEquals("2026-11-10T14:00:00", task.fechaEntrega)
        assertEquals("2026-11-05T10:24:00", task.createdAt)
        assertEquals(TaskPriority.ALTA, task.prioridad)
        assertEquals(TaskStatus.COMPLETADA, task.estado)
        assertTrue(task.vencida)
    }

    @Test
    fun taskDtoListToDomainListUsesDefaultsForNullValues() {
        val list = listOf(
            TaskDto(
                id = 1,
                titulo = "Leer",
                materia = "Ética",
                descripcion = "Capítulo 3",
                fechaEntrega = "2026-11-12T09:30:00",
                prioridad = null,
                estado = "desconocido",
                vencida = null
            )
        )

        val result = list.toDomainList()

        assertEquals(1, result.size)
        assertEquals(TaskPriority.MEDIA, result.first().prioridad)
        assertEquals(TaskStatus.PENDIENTE, result.first().estado)
        assertEquals("Capítulo 3", result.first().descripcion)
        assertFalse(result.first().vencida)
        assertNull(result.first().userId)
        assertNull(result.first().createdAt)
    }
}
