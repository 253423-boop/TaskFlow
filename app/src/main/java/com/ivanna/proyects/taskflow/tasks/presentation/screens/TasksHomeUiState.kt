package com.ivanna.proyects.taskflow.tasks.presentation.screens

import com.ivanna.proyects.taskflow.tasks.domain.model.Task
import com.ivanna.proyects.taskflow.tasks.domain.model.TaskStatus
import com.upchiapas.taskflow.R

enum class TaskFilter(
    val status: TaskStatus?,
    val labelRes: Int
) {
    ALL(null, R.string.filter_all),
    PENDING(TaskStatus.PENDIENTE, R.string.filter_pending),
    COMPLETED(TaskStatus.COMPLETADA, R.string.filter_completed)
}

data class TasksHomeUiState(
    val tasks: List<Task> = emptyList(),
    val selectedFilter: TaskFilter = TaskFilter.ALL,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val hasLoadedOnce: Boolean = false,
    val errorRes: Int? = null,
    val errorDetail: String? = null,
    val operationSuccessRes: Int? = null,
    val operationErrorRes: Int? = null,
    val operationErrorDetail: String? = null,
    val isSessionExpired: Boolean = false
)
