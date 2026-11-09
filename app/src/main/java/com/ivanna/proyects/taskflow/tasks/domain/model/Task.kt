package com.ivanna.proyects.taskflow.tasks.domain.model

data class Task(
    val id: Int,
    val userId: Int? = null,
    val titulo: String,
    val materia: String,
    val descripcion: String?,
    val fechaEntrega: String,
    val prioridad: TaskPriority,
    val estado: TaskStatus,
    val vencida: Boolean,
    val createdAt: String? = null
)
