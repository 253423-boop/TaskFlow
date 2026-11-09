package com.ivanna.proyects.taskflow.auth.di

import com.ivanna.proyects.taskflow.auth.data.datasource.AuthApiService
import com.ivanna.proyects.taskflow.auth.data.repository.AuthRepositoryImpl
import com.ivanna.proyects.taskflow.auth.domain.repository.AuthRepository
import com.ivanna.proyects.taskflow.auth.presentation.viewmodels.LoginViewModelFactory
import com.ivanna.proyects.taskflow.auth.presentation.viewmodels.RegisterViewModelFactory
import com.ivanna.proyects.taskflow.core.di.CoreModule
import com.ivanna.proyects.taskflow.core.navigation.SessionViewModelFactory

class AuthModule(
    private val coreModule: CoreModule
) {
    private val apiService: AuthApiService by lazy {
        coreModule.retrofit.create(AuthApiService::class.java)
    }

    val repository: AuthRepository by lazy {
        AuthRepositoryImpl(
            apiService = apiService,
            sessionManager = coreModule.sessionManager
        )
    }

    val factorySession: SessionViewModelFactory by lazy {
        SessionViewModelFactory(
            authRepository = repository,
            sessionManager = coreModule.sessionManager
        )
    }

    val factoryRegister: RegisterViewModelFactory by lazy {
        RegisterViewModelFactory(repository)
    }

    fun createLoginFactory(sessionExpired: Boolean = false): LoginViewModelFactory {
        return LoginViewModelFactory(
            authRepository = repository,
            initialSessionExpired = sessionExpired
        )
    }
}
