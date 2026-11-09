package com.ivanna.proyects.taskflow.tasks.data.repository

import com.ivanna.proyects.taskflow.core.network.executeApiCall
import com.ivanna.proyects.taskflow.tasks.data.datasource.TasksApiService
import com.ivanna.proyects.taskflow.tasks.data.dto.CreateTaskDto
import com.ivanna.proyects.taskflow.tasks.data.dto.TaskDto
import com.ivanna.proyects.taskflow.tasks.data.dto.UpdateTaskDto
import com.ivanna.proyects.taskflow.tasks.data.mapper.toDomain
import com.ivanna.proyects.taskflow.tasks.data.mapper.toDomainList
import com.ivanna.proyects.taskflow.tasks.domain.model.Task
import com.ivanna.proyects.taskflow.tasks.domain.model.TaskPriority
import com.ivanna.proyects.taskflow.tasks.domain.model.TaskStatus
import com.ivanna.proyects.taskflow.tasks.domain.repository.TasksRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import retrofit2.HttpException

class TasksRepositoryImpl(
    private val apiService: TasksApiService,
    private val json: Json = defaultJson
) : TasksRepository {

    override suspend fun getTasks(estado: TaskStatus?): Result<List<Task>> {
        return executeApiCall {
            apiService.getTasks(estado = estado?.apiValue).toDomainList()
        }
    }

    override suspend fun getNextDelivery(): Result<Task?> {
        return executeApiCall {
            val response = apiService.getNextDelivery()
            if (!response.isSuccessful) {
                throw HttpException(response)
            }
            val body = response.body()
            if (body == null || body is JsonNull) {
                null
            } else {
                json.decodeFromJsonElement(TaskDto.serializer(), body).toDomain()
            }
        }
    }

    override suspend fun getTaskById(id: Int): Result<Task> {
        return executeApiCall {
            apiService.getTaskById(id).toDomain()
        }
    }

    override suspend fun createTask(
        titulo: String,
        materia: String,
        descripcion: String?,
        fechaEntrega: String,
        prioridad: TaskPriority,
        estado: TaskStatus
    ): Result<Task> {
        return executeApiCall {
            val request = CreateTaskDto(
                titulo = titulo.trim(),
                materia = materia.trim(),
                descripcion = descripcion?.trim()?.takeIf { it.isNotEmpty() },
                fechaEntrega = fechaEntrega.trim(),
                prioridad = prioridad.level,
                estado = estado.apiValue
            )
            apiService.createTask(request).toDomain()
        }
    }

    override suspend fun updateTask(
        id: Int,
        titulo: String,
        materia: String,
        descripcion: String?,
        fechaEntrega: String,
        prioridad: TaskPriority,
        estado: TaskStatus
    ): Result<Task> {
        return executeApiCall {
            val request = UpdateTaskDto(
                titulo = titulo.trim(),
                materia = materia.trim(),
                descripcion = descripcion?.trim()?.takeIf { it.isNotEmpty() },
                fechaEntrega = fechaEntrega.trim(),
                prioridad = prioridad.level,
                estado = estado.apiValue
            )
            apiService.updateTask(id, request).toDomain()
        }
    }

    override suspend fun completeTask(id: Int): Result<Task> {
        return executeApiCall {
            apiService.completeTask(id).toDomain()
        }
    }

    override suspend fun deleteTask(id: Int): Result<Unit> {
        return executeApiCall {
            val response = apiService.deleteTask(id)
            if (!response.isSuccessful) {
                throw HttpException(response)
            }
        }
    }

    private companion object {
        val defaultJson = Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }
    }
}
