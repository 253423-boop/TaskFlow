package com.ivanna.proyects.taskflow.core.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivanna.proyects.taskflow.auth.domain.repository.AuthRepository
import com.ivanna.proyects.taskflow.core.network.isUnauthorizedError
import com.ivanna.proyects.taskflow.core.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SessionDestination {
    CHECKING,
    LOGIN,
    LOGIN_EXPIRED,
    TASKS_HOME
}

class SessionViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _destination = MutableStateFlow(SessionDestination.CHECKING)
    val destination: StateFlow<SessionDestination> = _destination.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        if (!authRepository.isUserLoggedIn()) {
            _destination.value = SessionDestination.LOGIN
            return
        }

        viewModelScope.launch {
            authRepository.getCurrentUser().fold(
                onSuccess = { user ->
                    sessionManager.saveUserName(user.nombre)
                    sessionManager.saveUserId(user.id)
                    _destination.value = SessionDestination.TASKS_HOME
                },
                onFailure = { throwable ->
                    if (throwable.isUnauthorizedError()) {
                        authRepository.logout()
                        _destination.value = SessionDestination.LOGIN_EXPIRED
                    } else {
                        _destination.value = SessionDestination.TASKS_HOME
                    }
                }
            )
        }
    }
}
