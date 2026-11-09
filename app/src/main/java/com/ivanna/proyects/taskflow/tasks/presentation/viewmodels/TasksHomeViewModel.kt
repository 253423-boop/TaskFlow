package com.ivanna.proyects.taskflow.tasks.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivanna.proyects.taskflow.auth.domain.repository.AuthRepository
import com.ivanna.proyects.taskflow.core.network.ApiException
import com.ivanna.proyects.taskflow.core.network.isUnauthorizedError
import com.ivanna.proyects.taskflow.tasks.domain.repository.TasksRepository
import com.ivanna.proyects.taskflow.tasks.presentation.screens.TaskFilter
import com.ivanna.proyects.taskflow.tasks.presentation.screens.TasksHomeUiState
import com.upchiapas.taskflow.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TasksHomeViewModel(
    private val tasksRepository: TasksRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksHomeUiState())
    val uiState: StateFlow<TasksHomeUiState> = _uiState.asStateFlow()

    private var loadTasksJob: Job? = null

    init {
        loadTasks(showLoading = true)
    }

    fun onFilterSelected(filter: TaskFilter) {
        if (_uiState.value.selectedFilter == filter) {
            return
        }
        _uiState.update {
            it.copy(
                selectedFilter = filter,
                tasks = emptyList(),
                errorRes = null,
                errorDetail = null
            )
        }
        loadTasks(showLoading = true)
    }

    fun loadTasks(showLoading: Boolean = true, isRefresh: Boolean = false) {
        loadTasksJob?.cancel()
        loadTasksJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = (showLoading && !isRefresh) || it.isLoading,
                    isRefreshing = isRefresh,
                    errorRes = null,
                    errorDetail = null,
                    operationErrorRes = if (isRefresh) null else it.operationErrorRes,
                    operationErrorDetail = if (isRefresh) null else it.operationErrorDetail
                )
            }

            val filterStatus = _uiState.value.selectedFilter.status

            tasksRepository.getTasks(filterStatus).fold(
                onSuccess = { taskList ->
                    _uiState.update {
                        it.copy(
                            tasks = taskList,
                            isLoading = false,
                            isRefreshing = false,
                            hasLoadedOnce = true,
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
                                isRefreshing = false,
                                isSessionExpired = true
                            )
                        }
                    } else {
                        val (errRes, errDetail) = mapError(throwable)
                        _uiState.update {
                            val showBanner = it.tasks.isNotEmpty()
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                errorRes = errRes,
                                errorDetail = errDetail,
                                operationErrorRes = if (showBanner) errRes else it.operationErrorRes,
                                operationErrorDetail = if (showBanner) errDetail else it.operationErrorDetail
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

    fun logout() {
        loadTasksJob?.cancel()
        authRepository.logout()
    }

    private fun mapError(throwable: Throwable): Pair<Int?, String?> {
        return when (throwable) {
            is ApiException.NoConnection -> R.string.error_load_tasks_subtitle to null
            is ApiException.HttpError ->
                (if (throwable.detail == null) R.string.error_unexpected else null) to throwable.detail
            else ->
                R.string.error_unexpected to null
        }
    }
}
