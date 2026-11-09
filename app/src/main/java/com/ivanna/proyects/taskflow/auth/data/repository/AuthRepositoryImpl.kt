package com.ivanna.proyects.taskflow.auth.data.repository

import com.ivanna.proyects.taskflow.auth.data.datasource.AuthApiService
import com.ivanna.proyects.taskflow.auth.data.dto.LoginRequestDto
import com.ivanna.proyects.taskflow.auth.data.dto.RegisterRequestDto
import com.ivanna.proyects.taskflow.auth.data.mapper.toDomain
import com.ivanna.proyects.taskflow.auth.domain.model.User
import com.ivanna.proyects.taskflow.auth.domain.repository.AuthRepository
import com.ivanna.proyects.taskflow.core.session.SessionManager
import java.io.IOException
import java.net.HttpURLConnection
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import retrofit2.HttpException

class AuthRepositoryImpl(
    private val apiService: AuthApiService,
    private val sessionManager: SessionManager
) : AuthRepository {

    override suspend fun login(correo: String, password: String): Result<User> {
        return executeApiCall {
            val response = apiService.login(
                LoginRequestDto(
                    correo = correo,
                    password = password
                )
            )
            sessionManager.saveToken(response.access_token)
            sessionManager.saveUserName(response.user.nombre)
            response.user.toDomain()
        }
    }

    override suspend fun register(
        nombre: String,
        correo: String,
        password: String,
        passwordConfirm: String
    ): Result<User> {
        return executeApiCall {
            val response = apiService.register(
                RegisterRequestDto(
                    nombre = nombre,
                    correo = correo,
                    password = password,
                    password_confirm = passwordConfirm
                )
            )
            sessionManager.saveToken(response.access_token)
            sessionManager.saveUserName(response.user.nombre)
            response.user.toDomain()
        }
    }

    override suspend fun getCurrentUser(): Result<User> {
        return executeApiCall {
            apiService.getMe().toDomain()
        }
    }

    override fun logout() {
        sessionManager.clearSession()
    }

    override fun isUserLoggedIn(): Boolean {
        return sessionManager.hasActiveSession()
    }

    private inline fun <T> executeApiCall(block: () -> T): Result<T> {
        return runCatching { block() }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { throwable ->
                if (throwable is CancellationException) {
                    throw throwable
                }
                Result.failure(mapException(throwable))
            }
        )
    }

    private fun mapException(throwable: Throwable): Exception {
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
                serverMessage ?: "Solicitud inválida. Verifica los datos ingresados."
            HttpURLConnection.HTTP_UNAUTHORIZED ->
                serverMessage ?: "Credenciales inválidas o sesión expirada."
            HttpURLConnection.HTTP_FORBIDDEN ->
                serverMessage ?: "No tienes permiso para acceder a este recurso."
            HttpURLConnection.HTTP_NOT_FOUND ->
                serverMessage ?: "Recurso no encontrado."
            HttpURLConnection.HTTP_CONFLICT ->
                serverMessage ?: "El correo electrónico ya se encuentra registrado."
            HTTP_UNPROCESSABLE_ENTITY ->
                serverMessage ?: "Los datos enviados no son válidos."
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
