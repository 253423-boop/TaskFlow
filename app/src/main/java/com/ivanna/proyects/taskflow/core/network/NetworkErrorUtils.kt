package com.ivanna.proyects.taskflow.core.network

import java.io.IOException
import java.net.HttpURLConnection
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import retrofit2.HttpException

fun Throwable.isUnauthorizedError(): Boolean {
    var current: Throwable? = this
    while (current != null) {
        when (current) {
            is ApiException.Unauthorized -> return true
            is HttpException -> if (current.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                return true
            }
        }
        current = current.cause
    }
    return false
}

inline fun <T> executeApiCall(
    crossinline mapHttpError: (HttpException, String?) -> ApiException = ::defaultMapHttpError,
    block: () -> T
): Result<T> {
    return runCatching { block() }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { throwable ->
            if (throwable is CancellationException) {
                throw throwable
            }
            Result.failure(mapNetworkThrowable(throwable) { httpException, serverMessage ->
                mapHttpError(httpException, serverMessage)
            })
        }
    )
}

@PublishedApi
internal fun mapNetworkThrowable(
    throwable: Throwable,
    mapHttpError: (HttpException, String?) -> ApiException
): ApiException {
    return when (throwable) {
        is ApiException -> throwable
        is HttpException -> mapHttpError(throwable, extractServerMessage(throwable))
        is IOException -> ApiException.NoConnection(throwable)
        is SerializationException -> ApiException.Serialization(throwable)
        else -> ApiException.Unexpected(cause = throwable, detail = throwable.message)
    }
}

fun defaultMapHttpError(
    exception: HttpException,
    serverMessage: String?
): ApiException {
    return when (exception.code()) {
        HttpURLConnection.HTTP_UNAUTHORIZED ->
            ApiException.Unauthorized(cause = exception, detail = serverMessage)
        else ->
            ApiException.HttpError(
                cause = exception,
                detail = serverMessage
            )
    }
}

fun extractServerMessage(exception: HttpException): String? {
    val errorBody = runCatching {
        exception.response()?.errorBody()?.string()?.trim()
    }.getOrNull()
    if (errorBody.isNullOrEmpty()) {
        return null
    }
    return runCatching {
        val element = Json.parseToJsonElement(errorBody)
        if (element is JsonObject) {
            val detailMessage = when (val detail = element["detail"]) {
                is JsonPrimitive -> if (detail.isString) detail.content else null
                is JsonArray -> detail.firstOrNull()
                    ?.let { item -> (item as? JsonObject)?.get("msg") as? JsonPrimitive }
                    ?.takeIf { it.isString }
                    ?.content
                else -> null
            }
            detailMessage ?: ((element["message"] ?: element["error"]) as? JsonPrimitive)
                ?.takeIf { it.isString }
                ?.content
        } else {
            null
        }
    }.getOrNull()?.takeIf { it.isNotBlank() }
}
