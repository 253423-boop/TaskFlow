package com.ivanna.proyects.taskflow.tasks.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivanna.proyects.taskflow.auth.domain.repository.AuthRepository
import com.ivanna.proyects.taskflow.core.network.ApiException
import com.ivanna.proyects.taskflow.core.network.isUnauthorizedError
import com.ivanna.proyects.taskflow.core.session.SessionManager
import com.ivanna.proyects.taskflow.tasks.domain.repository.TasksRepository
import com.ivanna.proyects.taskflow.tasks.presentation.screens.TaskDetailUiState
import com.upchiapas.taskflow.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TaskDetailViewModel(
    private val taskId: Int,
    private val tasksRepository: TasksRepository,
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        TaskDetailUiState(
            ownerName = sessionManager.getUserName().orEmpty(),
            ownerId = sessionManager.getUserId()
        )
    )
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    private var loadTaskJob: Job? = null

    init {
        loadTask()
    }

    fun loadTask() {
        loadTaskJob?.cancel()
        loadTaskJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = it.task == null,
                    errorRes = null,
                    errorDetail = null
                )
            }
            if (_uiState.value.ownerName.isBlank()) {
                authRepository.getCurrentUser().onSuccess { user ->
                    sessionManager.saveUserName(user.nombre)
                    sessionManager.saveUserId(user.id)
                    _uiState.update {
                        it.copy(
                            ownerName = user.nombre,
                            ownerId = user.id
                        )
                    }
                }
            }
            tasksRepository.getTaskById(taskId).fold(
                onSuccess = { loadedTask ->
                    _uiState.update {
                        it.copy(
                            task = loadedTask,
                            ownerName = sessionManager.getUserName().orEmpty().ifBlank { it.ownerName },
                            ownerId = loadedTask.userId ?: sessionManager.getUserId() ?: it.ownerId,
                            isLoading = false,
                            errorRes = null,
                            errorDetail = null
                        )
                    }
                },
                onFailure = { throwable ->
                    if (throwable.isUnauthorizedError()) {
                        authRepository.logout()
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isSessionExpired = true
                            )
                        }
                    } else {
                        val (errRes, errDetail) = mapError(throwable, R.string.error_load_task_detail)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorRes = errRes,
                                errorDetail = errDetail
                            )
                        }
                    }
                }
            )
        }
    }

    fun markAsCompleted() {
        if (_uiState.value.isActionLoading) {
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isActionLoading = true,
                    isCompleting = true,
                    operationErrorRes = null,
                    operationErrorDetail = null,
                    operationSuccessRes = null
                )
            }
            tasksRepository.completeTask(taskId).fold(
                onSuccess = { updatedTask ->
                    _uiState.update {
                        it.copy(
                            task = updatedTask,
                            isActionLoading = false,
                            isCompleting = false,
                            operationSuccessRes = R.string.msg_task_completed
                        )
                    }
                },
                onFailure = { throwable ->
                    if (throwable.isUnauthorizedError()) {
                        authRepository.logout()
                        _uiState.update {
                            it.copy(
                                isActionLoading = false,
                                isCompleting = false,
                                isSessionExpired = true
                            )
                        }
                    } else {
                        val (errRes, errDetail) = mapError(throwable, R.string.error_complete_task)
                        _uiState.update {
                            it.copy(
                                isActionLoading = false,
                                isCompleting = false,
                                operationErrorRes = errRes,
                                operationErrorDetail = errDetail
                            )
                        }
                    }
                }
            )
        }
    }

    fun onDeleteRequest() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun onDeleteDismiss() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun confirmDelete() {
        if (_uiState.value.isActionLoading) {
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isActionLoading = true,
                    operationErrorRes = null,
                    operationErrorDetail = null,
                    operationSuccessRes = null
                )
            }
            tasksRepository.deleteTask(taskId).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            showDeleteDialog = false,
                            isDeleted = true
                        )
                    }
                },
                onFailure = { throwable ->
                    if (throwable.isUnauthorizedError()) {
                        authRepository.logout()
                        _uiState.update {
                            it.copy(
                                isActionLoading = false,
                                showDeleteDialog = false,
                                isSessionExpired = true
                            )
                        }
                    } else {
                        val (errRes, errDetail) = mapError(throwable, R.string.error_delete_task)
                        _uiState.update {
                            it.copy(
                                isActionLoading = false,
                                showDeleteDialog = false,
                                operationErrorRes = errRes,
                                operationErrorDetail = errDetail
                            )
                        }
                    }
                }
            )
        }
    }

    fun consumeMessages() {
        _uiState.update {
            it.copy(
                operationSuccessRes = null,
                operationErrorRes = null,
                operationErrorDetail = null
            )
        }
    }

    private fun mapError(throwable: Throwable, fallbackRes: Int): Pair<Int?, String?> {
        return when (throwable) {
            is ApiException.NoConnection -> R.string.error_no_connection to null
            is ApiException.HttpError ->
                (if (throwable.detail == null) fallbackRes else null) to throwable.detail
            else -> fallbackRes to null
        }
    }
}
