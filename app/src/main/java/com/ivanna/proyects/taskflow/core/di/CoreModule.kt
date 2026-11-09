package com.ivanna.proyects.taskflow.core.di

import android.content.Context
import com.ivanna.proyects.taskflow.core.network.AuthInterceptor
import com.ivanna.proyects.taskflow.core.network.RetrofitClient
import com.ivanna.proyects.taskflow.core.session.SessionManager
import com.ivanna.proyects.taskflow.core.session.SharedPrefsSessionManager
import kotlinx.serialization.json.Json
import retrofit2.Retrofit

class CoreModule(
    context: Context
) {
    val sessionManager: SessionManager by lazy {
        SharedPrefsSessionManager(context)
    }

    private val authInterceptor: AuthInterceptor by lazy {
        AuthInterceptor(sessionManager)
    }

    private val retrofitClient: RetrofitClient by lazy {
        RetrofitClient(authInterceptor)
    }

    val json: Json
        get() = retrofitClient.json

    val retrofit: Retrofit
        get() = retrofitClient.retrofit
}
