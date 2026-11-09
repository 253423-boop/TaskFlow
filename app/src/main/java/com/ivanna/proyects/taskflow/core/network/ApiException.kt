package com.ivanna.proyects.taskflow.core.network

sealed class ApiException(
    cause: Throwable? = null,
    val detail: String? = null
) : Exception(detail, cause) {

    class NoConnection(cause: Throwable? = null) : ApiException(cause = cause)

    class Unauthorized(cause: Throwable? = null, detail: String? = null) :
        ApiException(cause = cause, detail = detail)

    class InvalidCredentials(cause: Throwable? = null, detail: String? = null) :
        ApiException(cause = cause, detail = detail)

    class DuplicateEmail(cause: Throwable? = null, detail: String? = null) :
        ApiException(cause = cause, detail = detail)

    class ShortPassword(cause: Throwable? = null, detail: String? = null) :
        ApiException(cause = cause, detail = detail)

    class Serialization(cause: Throwable? = null) : ApiException(cause = cause)

    class HttpError(
        cause: Throwable? = null,
        detail: String? = null
    ) : ApiException(cause = cause, detail = detail)

    class Unexpected(cause: Throwable? = null, detail: String? = null) :
        ApiException(cause = cause, detail = detail)
}
