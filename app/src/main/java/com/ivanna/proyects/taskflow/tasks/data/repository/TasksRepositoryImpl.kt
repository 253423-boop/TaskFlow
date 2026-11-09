package com.ivanna.proyects.taskflow.tasks.data.repository

import com.ivanna.proyects.taskflow.tasks.data.datasource.TasksApiService
import com.ivanna.proyects.taskflow.tasks.data.dto.CreateTaskDto
import com.ivanna.proyects.taskflow.tasks.data.dto.UpdateTaskDto
import com.ivanna.proyects.taskflow.tasks.data.mapper.toDomain
import com.ivanna.proyects.taskflow.tasks.data.mapper.toDomainList
import com.ivanna.proyects.taskflow.tasks.domain.model.Task
import com.ivanna.proyects.taskflow.tasks.domain.model.TaskPriority
import com.ivanna.proyects.taskflow.tasks.domain.model.TaskStatus
import com.ivanna.proyects.taskflow.tasks.domain.repository.TasksRepository
import java.io.IOException
import java.net.HttpURLConnection
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import retrofit2.HttpException

class TasksRepositoryImpl(
    private val apiService: TasksApiService
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
            response.body()?.toDomain()
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
                fecha_entrega = fechaEntrega.trim(),
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
                fecha_entrega = fechaEntrega.trim(),
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

    private inline fun <T> executeApiCall(block: () -> T): Result<T> {
        return runCatching { block() }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { throwable ->
                if (throwable is CancellationException) {
                    throw throwable
                }
                Result.failure(mapThrowable(throwable))
            }
        )
    }

    private fun mapThrowable(throwable: Throwable): Throwable {
        return when (throwable) {
            is HttpException -> mapHttpException(throwable)
            is IOException -> Exception("Error de conexión. Verifica tu acceso a Internet.", throwable)
            is SerializationException -> Exception("Error al procesar la respuesta del servidor.", throwable)
            is Exception -> throwable
            else -> Exception(throwable.message ?: "Ocurrió un error inesperado.", throwable)
        }
    }

    private fun mapHttpException(exception: HttpException): Exception {
        val serverMessage = extractServerMessage(exception)
        val message = when (exception.code()) {
            HttpURLConnection.HTTP_BAD_REQUEST ->
                serverMessage ?: "Solicitud inválida. Verifica los datos de la tarea."
            HttpURLConnection.HTTP_UNAUTHORIZED ->
                serverMessage ?: "Sesión expirada o no autorizada. Inicia sesión nuevamente."
            HttpURLConnection.HTTP_FORBIDDEN ->
                serverMessage ?: "No tienes permiso para acceder a esta tarea."
            HttpURLConnection.HTTP_NOT_FOUND ->
                serverMessage ?: "La tarea solicitada no fue encontrada."
            HTTP_UNPROCESSABLE_ENTITY ->
                serverMessage ?: "Los datos enviados no son válidos. Revisa los campos e intenta de nuevo."
            in HTTP_SERVER_ERROR_START..HTTP_SERVER_ERROR_END ->
                serverMessage ?: "Error del servidor. Intenta nuevamente más tarde."
            else ->
                serverMessage ?: "Error en la solicitud (código ${exception.code()})."
        }
        return Exception(message, exception)
    }

    private fun extractServerMessage(exception: HttpException): String? {
        val errorBody = exception.response()?.errorBody()?.string()?.trim()
        if (errorBody.isNullOrEmpty()) {
            return null
        }
        return runCatching {
            val element = Json.parseToJsonElement(errorBody)
            if (element is JsonObject) {
                val detail = element["detail"]
                when {
                    detail is JsonPrimitive && detail.isString -> detail.content
                    detail is JsonArray -> detail.firstOrNull()
                        ?.let { item -> (item as? JsonObject)?.get("msg") as? JsonPrimitive }
                        ?.takeIf { it.isString }
                        ?.content
                    else -> {
                        val fallback = element["message"] ?: element["error"]
                        (fallback as? JsonPrimitive)?.takeIf { it.isString }?.content
                    }
                }
            } else {
                null
            }
        }.getOrNull()?.takeIf { it.isNotBlank() }
    }

    private companion object {
        const val HTTP_UNPROCESSABLE_ENTITY = 422
        const val HTTP_SERVER_ERROR_START = 500
        const val HTTP_SERVER_ERROR_END = 599
    }
}
