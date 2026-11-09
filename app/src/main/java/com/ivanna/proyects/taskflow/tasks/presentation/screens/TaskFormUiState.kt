package com.ivanna.proyects.taskflow.tasks.presentation.screens

import com.ivanna.proyects.taskflow.tasks.domain.model.TaskPriority
import com.ivanna.proyects.taskflow.tasks.domain.model.TaskStatus
import java.time.LocalDate
import java.time.LocalTime

data class TaskFormUiState(
    val taskId: Int? = null,
    val titulo: String = "",
    val materia: String = "",
    val descripcion: String = "",
    val selectedDate: LocalDate? = null,
    val selectedTime: LocalTime? = null,
    val prioridad: TaskPriority = TaskPriority.MEDIA,
    val estado: TaskStatus = TaskStatus.PENDIENTE,
    val tituloErrorRes: Int? = null,
    val materiaErrorRes: Int? = null,
    val fechaErrorRes: Int? = null,
    val isInitialLoading: Boolean = false,
    val isSaving: Boolean = false,
    val loadErrorRes: Int? = null,
    val loadErrorDetail: String? = null,
    val submitErrorRes: Int? = null,
    val submitErrorDetail: String? = null,
    val isSaveSuccess: Boolean = false,
    val isSessionExpired: Boolean = false
) {
    val isEditMode: Boolean
        get() = taskId != null
}
