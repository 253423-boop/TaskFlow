package com.ivanna.proyects.taskflow.tasks.presentation.screens

import com.ivanna.proyects.taskflow.tasks.domain.model.Task

data class TaskDetailUiState(
    val task: Task? = null,
    val ownerName: String = "",
    val ownerId: Int? = null,
    val isLoading: Boolean = true,
    val isActionLoading: Boolean = false,
    val isCompleting: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val errorRes: Int? = null,
    val errorDetail: String? = null,
    val operationSuccessRes: Int? = null,
    val operationErrorRes: Int? = null,
    val operationErrorDetail: String? = null,
    val isDeleted: Boolean = false,
    val isSessionExpired: Boolean = false
)
