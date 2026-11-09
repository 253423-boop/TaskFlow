package com.ivanna.proyects.taskflow.tasks.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivanna.proyects.taskflow.auth.domain.repository.AuthRepository
import com.ivanna.proyects.taskflow.core.network.ApiException
import com.ivanna.proyects.taskflow.core.network.isUnauthorizedError
import com.ivanna.proyects.taskflow.tasks.domain.model.TaskPriority
import com.ivanna.proyects.taskflow.tasks.domain.model.TaskStatus
import com.ivanna.proyects.taskflow.tasks.domain.repository.TasksRepository
import com.ivanna.proyects.taskflow.tasks.presentation.screens.TaskFormUiState
import com.ivanna.proyects.taskflow.tasks.presentation.util.TaskDateFormatter
import com.upchiapas.taskflow.R
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TaskFormViewModel(
    private val taskId: Int?,
    private val tasksRepository: TasksRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        TaskFormUiState(
            taskId = taskId,
            isInitialLoading = taskId != null
        )
    )
    val uiState: StateFlow<TaskFormUiState> = _uiState.asStateFlow()

    private var loadTaskJob: Job? = null

    init {
        if (taskId != null) {
            loadExistingTask(taskId)
        }
    }

    fun retryLoad() {
        if (taskId != null) {
            loadExistingTask(taskId)
        }
    }

    private fun loadExistingTask(id: Int) {
        loadTaskJob?.cancel()
        loadTaskJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isInitialLoading = true,
                    loadErrorRes = null,
                    loadErrorDetail = null
                )
            }
            tasksRepository.getTaskById(id).fold(
                onSuccess = { task ->
                    val parsedDateTime = TaskDateFormatter.parseApiDateTime(task.fechaEntrega)
                    _uiState.update {
                        it.copy(
                            titulo = task.titulo,
                            materia = task.materia,
                            descripcion = task.descripcion.orEmpty(),
                            selectedDate = parsedDateTime?.toLocalDate(),
                            selectedTime = parsedDateTime?.toLocalTime(),
                            prioridad = task.prioridad,
                            estado = task.estado,
                            isInitialLoading = false,
                            loadErrorRes = null,
                            loadErrorDetail = null
                        )
                    }
                },
                onFailure = { throwable ->
                    if (throwable.isUnauthorizedError()) {
                        authRepository.logout()
                        _uiState.update {
                            it.copy(
                                isInitialLoading = false,
                                isSessionExpired = true
                            )
                        }
                    } else {
                        val (errRes, errDetail) = mapError(throwable, R.string.error_load_task_detail)
                        _uiState.update {
                            it.copy(
                                isInitialLoading = false,
                                loadErrorRes = errRes,
                                loadErrorDetail = errDetail
                            )
                        }
                    }
                }
            )
        }
    }

    fun onTituloChange(value: String) {
        val lengthError = if (value.trim().length > MAX_TITLE_LENGTH) {
            R.string.error_title_max_length
        } else {
            null
        }
        _uiState.update {
            it.copy(
                titulo = value,
                tituloErrorRes = lengthError,
                submitErrorRes = null,
                submitErrorDetail = null
            )
        }
    }

    fun onMateriaChange(value: String) {
        _uiState.update {
            it.copy(
                materia = value,
                materiaErrorRes = null,
                submitErrorRes = null,
                submitErrorDetail = null
            )
        }
    }

    fun onDescripcionChange(value: String) {
        _uiState.update {
            it.copy(
                descripcion = value,
                submitErrorRes = null,
                submitErrorDetail = null
            )
        }
    }

    fun onDateTimeSelected(date: LocalDate, time: LocalTime) {
        _uiState.update {
            it.copy(
                selectedDate = date,
                selectedTime = time,
                fechaErrorRes = null,
                submitErrorRes = null,
                submitErrorDetail = null
            )
        }
    }

    fun onPrioridadSelected(prioridad: TaskPriority) {
        _uiState.update {
            it.copy(
                prioridad = prioridad,
                submitErrorRes = null,
                submitErrorDetail = null
            )
        }
    }

    fun onEstadoSelected(estado: TaskStatus) {
        _uiState.update {
            it.copy(
                estado = estado,
                submitErrorRes = null,
                submitErrorDetail = null
            )
        }
    }

    fun onSubmit() {
        val currentState = _uiState.value
        if (currentState.isSaving) {
            return
        }

        val trimmedTitulo = currentState.titulo.trim()
        val trimmedMateria = currentState.materia.trim()
        val date = currentState.selectedDate
        val time = currentState.selectedTime

        val tituloErrorRes = when {
            trimmedTitulo.isBlank() -> R.string.error_title_required
            trimmedTitulo.length > MAX_TITLE_LENGTH -> R.string.error_title_max_length
            else -> null
        }
        val materiaErrorRes = if (trimmedMateria.isBlank()) R.string.error_subject_required else null

        if (date == null || time == null || tituloErrorRes != null || materiaErrorRes != null) {
            val fechaErrorRes = if (date == null || time == null) {
                R.string.error_due_date_required
            } else {
                null
            }
            _uiState.update {
                it.copy(
                    tituloErrorRes = tituloErrorRes,
                    materiaErrorRes = materiaErrorRes,
                    fechaErrorRes = fechaErrorRes,
                    submitErrorRes = null,
                    submitErrorDetail = null
                )
            }
            return
        }

        val apiDateTime = TaskDateFormatter.formatToApi(date, time)
        val trimmedDescription = currentState.descripcion.trim().takeIf { it.isNotEmpty() }

        _uiState.update {
            it.copy(
                tituloErrorRes = null,
                materiaErrorRes = null,
                fechaErrorRes = null,
                submitErrorRes = null,
                submitErrorDetail = null,
                isSaving = true
            )
        }

        viewModelScope.launch {
            val result = if (currentState.taskId == null) {
                tasksRepository.createTask(
                    titulo = trimmedTitulo,
                    materia = trimmedMateria,
                    descripcion = trimmedDescription,
                    fechaEntrega = apiDateTime,
                    prioridad = currentState.prioridad,
                    estado = currentState.estado
                )
            } else {
                tasksRepository.updateTask(
                    id = currentState.taskId,
                    titulo = trimmedTitulo,
                    materia = trimmedMateria,
                    descripcion = trimmedDescription,
                    fechaEntrega = apiDateTime,
                    prioridad = currentState.prioridad,
                    estado = currentState.estado
                )
            }

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isSaveSuccess = true
                        )
                    }
                },
                onFailure = { throwable ->
                    if (throwable.isUnauthorizedError()) {
                        authRepository.logout()
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                isSessionExpired = true
                            )
                        }
                    } else {
                        val (errRes, errDetail) = mapError(throwable, R.string.error_save_task)
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                submitErrorRes = errRes,
                                submitErrorDetail = errDetail
                            )
                        }
                    }
                }
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

    companion object {
        const val MAX_TITLE_LENGTH = 100
    }
}
