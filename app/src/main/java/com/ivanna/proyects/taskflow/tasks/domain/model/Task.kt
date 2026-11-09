package com.ivanna.proyects.taskflow.tasks.domain.model

data class Task(
    val id: Int,
    val titulo: String,
    val materia: String,
    val descripcion: String?,
    val fechaEntrega: String,
    val prioridad: TaskPriority,
    val estado: TaskStatus,
    val vencida: Boolean
)
