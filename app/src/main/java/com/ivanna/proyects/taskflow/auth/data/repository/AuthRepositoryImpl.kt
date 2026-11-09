package com.ivanna.proyects.taskflow.auth.data.repository

import com.ivanna.proyects.taskflow.auth.data.datasource.AuthApiService
import com.ivanna.proyects.taskflow.auth.data.dto.LoginRequestDto
import com.ivanna.proyects.taskflow.auth.data.dto.RegisterRequestDto
import com.ivanna.proyects.taskflow.auth.data.mapper.toDomain
import com.ivanna.proyects.taskflow.auth.domain.model.User
import com.ivanna.proyects.taskflow.auth.domain.repository.AuthRepository
import com.ivanna.proyects.taskflow.core.network.ApiException
import com.ivanna.proyects.taskflow.core.network.defaultMapHttpError
import com.ivanna.proyects.taskflow.core.network.executeApiCall
import com.ivanna.proyects.taskflow.core.session.SessionManager
import java.net.HttpURLConnection
import retrofit2.HttpException

class AuthRepositoryImpl(
    private val apiService: AuthApiService,
    private val sessionManager: SessionManager
) : AuthRepository {

    override suspend fun login(correo: String, password: String): Result<User> {
        return executeApiCall(
            mapHttpError = { exception, serverMessage ->
                mapHttpException(exception, serverMessage, AuthCallContext.LOGIN)
            }
        ) {
            val response = apiService.login(
                LoginRequestDto(
                    correo = correo,
                    password = password
                )
            )
            sessionManager.saveToken(response.accessToken)
            sessionManager.saveUserName(response.user.nombre)
            sessionManager.saveUserId(response.user.id)
            response.user.toDomain()
        }
    }

    override suspend fun register(
        nombre: String,
        correo: String,
        password: String,
        passwordConfirm: String
    ): Result<User> {
        return executeApiCall(
            mapHttpError = { exception, serverMessage ->
                mapHttpException(exception, serverMessage, AuthCallContext.REGISTER)
            }
        ) {
            val response = apiService.register(
                RegisterRequestDto(
                    nombre = nombre,
                    correo = correo,
                    password = password,
                    passwordConfirm = passwordConfirm
                )
            )
            sessionManager.saveToken(response.accessToken)
            sessionManager.saveUserName(response.user.nombre)
            sessionManager.saveUserId(response.user.id)
            response.user.toDomain()
        }
    }

    override suspend fun getCurrentUser(): Result<User> {
        return executeApiCall(
            mapHttpError = { exception, serverMessage ->
                mapHttpException(exception, serverMessage, AuthCallContext.SESSION)
            }
        ) {
            apiService.getMe().toDomain()
        }
    }

    override fun logout() {
        sessionManager.clearSession()
    }

    override fun isUserLoggedIn(): Boolean {
        return sessionManager.hasActiveSession()
    }

    private fun mapHttpException(
        exception: HttpException,
        serverMessage: String?,
        context: AuthCallContext
    ): ApiException {
        val code = exception.code()
        val normalized = serverMessage?.lowercase().orEmpty()

        return when (context) {
            AuthCallContext.LOGIN -> when (code) {
                HttpURLConnection.HTTP_UNAUTHORIZED,
                HttpURLConnection.HTTP_FORBIDDEN ->
                    ApiException.InvalidCredentials(cause = exception, detail = serverMessage)
                else ->
                    defaultMapHttpError(exception, serverMessage)
            }
            AuthCallContext.REGISTER -> when {
                code == HttpURLConnection.HTTP_CONFLICT ->
                    ApiException.DuplicateEmail(cause = exception, detail = serverMessage)
                (code == HttpURLConnection.HTTP_BAD_REQUEST || code == HTTP_UNPROCESSABLE_ENTITY) &&
                    (normalized.contains("registrado") ||
                        normalized.contains("duplicate") ||
                        normalized.contains("already") ||
                        normalized.contains("existe")) ->
                    ApiException.DuplicateEmail(cause = exception, detail = serverMessage)
                code == HTTP_UNPROCESSABLE_ENTITY &&
                    (normalized.contains("8") ||
                        normalized.contains("password") ||
                        normalized.contains("contraseña") ||
                        normalized.contains("short") ||
                        normalized.contains("mínimo") ||
                        normalized.contains("minimo")) ->
                    ApiException.ShortPassword(cause = exception, detail = serverMessage)
                else ->
                    defaultMapHttpError(exception, serverMessage)
            }
            AuthCallContext.SESSION -> defaultMapHttpError(exception, serverMessage)
        }
    }

    private enum class AuthCallContext {
        LOGIN,
        REGISTER,
        SESSION
    }

    private companion object {
        const val HTTP_UNPROCESSABLE_ENTITY = 422
    }
}
