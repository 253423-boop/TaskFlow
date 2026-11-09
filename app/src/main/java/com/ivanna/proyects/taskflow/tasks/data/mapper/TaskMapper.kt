package com.ivanna.proyects.taskflow.tasks.data.mapper

import com.ivanna.proyects.taskflow.tasks.data.dto.TaskDto
import com.ivanna.proyects.taskflow.tasks.domain.model.Task
import com.ivanna.proyects.taskflow.tasks.domain.model.TaskPriority
import com.ivanna.proyects.taskflow.tasks.domain.model.TaskStatus

fun TaskDto.toDomain(): Task = Task(
    id = id,
    titulo = titulo,
    materia = materia,
    descripcion = descripcion?.takeIf { it.isNotBlank() },
    fechaEntrega = fecha_entrega,
    prioridad = TaskPriority.fromLevel(prioridad),
    estado = TaskStatus.fromApiValue(estado),
    vencida = vencida ?: false
)

fun List<TaskDto>.toDomainList(): List<Task> = map { it.toDomain() }
