package com.ivanna.proyects.taskflow.core.network

import com.ivanna.proyects.taskflow.core.session.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = sessionManager.getToken()

        val requestToProceed = if (!token.isNullOrBlank()) {
            originalRequest.newBuilder()
                .header(HEADER_AUTHORIZATION, "$BEARER_PREFIX$token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(requestToProceed)
    }

    private companion object {
        const val HEADER_AUTHORIZATION = "Authorization"
        const val BEARER_PREFIX = "Bearer "
    }
}
