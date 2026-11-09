package com.ivanna.proyects.taskflow.tasks.domain.repository

import com.ivanna.proyects.taskflow.tasks.domain.model.Task
import com.ivanna.proyects.taskflow.tasks.domain.model.TaskPriority
import com.ivanna.proyects.taskflow.tasks.domain.model.TaskStatus

interface TasksRepository {
    suspend fun getTasks(estado: TaskStatus? = null): Result<List<Task>>
    suspend fun getNextDelivery(): Result<Task?>
    suspend fun getTaskById(id: Int): Result<Task>
    suspend fun createTask(
        titulo: String,
        materia: String,
        descripcion: String?,
        fechaEntrega: String,
        prioridad: TaskPriority,
        estado: TaskStatus = TaskStatus.PENDIENTE
    ): Result<Task>
    suspend fun updateTask(
        id: Int,
        titulo: String,
        materia: String,
        descripcion: String?,
        fechaEntrega: String,
        prioridad: TaskPriority,
        estado: TaskStatus
    ): Result<Task>
    suspend fun completeTask(id: Int): Result<Task>
    suspend fun deleteTask(id: Int): Result<Unit>
}
