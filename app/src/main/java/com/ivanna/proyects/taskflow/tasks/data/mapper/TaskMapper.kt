package com.ivanna.proyects.taskflow.tasks.data.mapper

import com.ivanna.proyects.taskflow.tasks.data.dto.TaskDto
import com.ivanna.proyects.taskflow.tasks.domain.model.Task
import com.ivanna.proyects.taskflow.tasks.domain.model.TaskPriority
import com.ivanna.proyects.taskflow.tasks.domain.model.TaskStatus

fun TaskDto.toDomain(): Task = Task(
    id = id,
    userId = userId,
    titulo = titulo,
    materia = materia,
    descripcion = descripcion?.takeIf { it.isNotBlank() },
    fechaEntrega = fechaEntrega,
    prioridad = TaskPriority.fromLevel(prioridad),
    estado = TaskStatus.fromApiValue(estado),
    vencida = vencida ?: false,
    createdAt = createdAt?.takeIf { it.isNotBlank() }
)

fun List<TaskDto>.toDomainList(): List<Task> = map { it.toDomain() }
