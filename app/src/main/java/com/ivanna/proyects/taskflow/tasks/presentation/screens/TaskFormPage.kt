package com.ivanna.proyects.taskflow.tasks.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ivanna.proyects.taskflow.TaskFlowApplication
import com.ivanna.proyects.taskflow.tasks.domain.model.TaskPriority
import com.ivanna.proyects.taskflow.tasks.domain.model.TaskStatus
import com.ivanna.proyects.taskflow.tasks.presentation.components.ErrorContent
import com.ivanna.proyects.taskflow.tasks.presentation.components.LoadingContent
import com.ivanna.proyects.taskflow.tasks.presentation.components.PrioritySelector
import com.ivanna.proyects.taskflow.tasks.presentation.components.TaskDatePickerDialog
import com.ivanna.proyects.taskflow.tasks.presentation.components.TaskStatusSelector
import com.ivanna.proyects.taskflow.tasks.presentation.components.TaskTimePickerDialog
import com.ivanna.proyects.taskflow.tasks.presentation.util.TaskDateFormatter
import com.ivanna.proyects.taskflow.tasks.presentation.viewmodels.TaskFormViewModel
import com.ivanna.proyects.taskflow.ui.components.MessageType
import com.ivanna.proyects.taskflow.ui.components.OperationMessage
import com.ivanna.proyects.taskflow.ui.components.TaskFlowHeader
import com.ivanna.proyects.taskflow.ui.components.TaskFlowPrimaryButton
import com.ivanna.proyects.taskflow.ui.components.TaskFlowTextField
import com.ivanna.proyects.taskflow.ui.theme.TaskFlowTextStyles
import com.ivanna.proyects.taskflow.ui.theme.TaskFlowTheme
import com.upchiapas.taskflow.R
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun TaskFormPage(
    taskId: Int? = null,
    viewModel: TaskFormViewModel = viewModel(
        factory = (LocalContext.current.applicationContext as TaskFlowApplication)
            .tasksModule
            .createTaskFormFactory(taskId)
    ),
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    onSessionExpired: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var pendingDate by remember { mutableStateOf<LocalDate?>(null) }

    LaunchedEffect(uiState.isSaveSuccess) {
        if (uiState.isSaveSuccess) {
            onSaveSuccess()
        }
    }

    LaunchedEffect(uiState.isSessionExpired) {
        if (uiState.isSessionExpired) {
            onSessionExpired()
        }
    }

    if (showDatePicker) {
        TaskDatePickerDialog(
            initialDate = uiState.selectedDate,
            onDateConfirmed = { chosenDate ->
                pendingDate = chosenDate
                showDatePicker = false
                showTimePicker = true
            },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showTimePicker) {
        TaskTimePickerDialog(
            initialTime = uiState.selectedTime,
            onTimeConfirmed = { chosenTime ->
                val date = pendingDate ?: uiState.selectedDate ?: LocalDate.now()
                viewModel.onDateTimeSelected(date, chosenTime)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }

    TaskFormContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onRetryLoad = viewModel::retryLoad,
        onTituloChange = viewModel::onTituloChange,
        onMateriaChange = viewModel::onMateriaChange,
        onDescripcionChange = viewModel::onDescripcionChange,
        onDateFieldClick = { showDatePicker = true },
        onPrioridadSelected = viewModel::onPrioridadSelected,
        onEstadoSelected = viewModel::onEstadoSelected,
        onSubmit = viewModel::onSubmit
    )
}

@Composable
fun TaskFormContent(
    uiState: TaskFormUiState,
    onNavigateBack: () -> Unit,
    onRetryLoad: () -> Unit,
    onTituloChange: (String) -> Unit,
    onMateriaChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onDateFieldClick: () -> Unit,
    onPrioridadSelected: (TaskPriority) -> Unit,
    onEstadoSelected: (TaskStatus) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val headerTitle = stringResource(
        id = if (uiState.isEditMode) {
            R.string.form_edit_task_title
        } else {
            R.string.form_new_task_title
        }
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(top = 12.dp, bottom = 24.dp)
    ) {
        TaskFlowHeader(
            title = headerTitle,
            showBack = true,
            backEnabled = !uiState.isSaving,
            onBack = onNavigateBack
        )

        when {
            uiState.isInitialLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingContent(
                        message = stringResource(id = R.string.loading_tasks),
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }
            }
            uiState.loadErrorRes != null || !uiState.loadErrorDetail.isNullOrBlank() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    ErrorContent(
                        title = stringResource(id = R.string.error_load_task_detail),
                        subtitle = uiState.loadErrorDetail ?: uiState.loadErrorRes?.let {
                            stringResource(id = it)
                        } ?: stringResource(id = R.string.error_load_tasks_subtitle),
                        retryLabel = stringResource(id = R.string.action_retry),
                        onRetry = onRetryLoad,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }
            }
            else -> {
                val formattedDueDate = if (uiState.selectedDate != null && uiState.selectedTime != null) {
                    TaskDateFormatter.formatDateTimeForField(uiState.selectedDate, uiState.selectedTime)
                } else {
                    ""
                }
                val titleCounter = if (
                    uiState.titulo.length >= 80 ||
                    uiState.tituloErrorRes == R.string.error_title_max_length
                ) {
                    stringResource(id = R.string.title_counter_format, uiState.titulo.length)
                } else {
                    null
                }
                val submitError = uiState.submitErrorDetail
                    ?: uiState.submitErrorRes?.let { stringResource(id = it) }

                Spacer(modifier = Modifier.height(22.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 27.6.dp)
                ) {
                    TaskFlowTextField(
                        value = uiState.titulo,
                        onValueChange = onTituloChange,
                        label = stringResource(id = R.string.field_title_required),
                        placeholder = stringResource(id = R.string.placeholder_task_title),
                        errorMessage = uiState.tituloErrorRes?.let { stringResource(id = it) },
                        errorTextStyle = TaskFlowTextStyles.taskFieldError,
                        helperEndText = titleCounter,
                        enabled = !uiState.isSaving,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next
                        )
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    TaskFlowTextField(
                        value = uiState.materia,
                        onValueChange = onMateriaChange,
                        label = stringResource(id = R.string.field_subject_required),
                        placeholder = stringResource(id = R.string.placeholder_task_subject),
                        errorMessage = uiState.materiaErrorRes?.let { stringResource(id = it) },
                        errorTextStyle = TaskFlowTextStyles.taskFieldError,
                        enabled = !uiState.isSaving,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next
                        )
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    TaskFlowTextField(
                        value = uiState.descripcion,
                        onValueChange = onDescripcionChange,
                        label = stringResource(id = R.string.field_description),
                        placeholder = stringResource(id = R.string.placeholder_task_description),
                        enabled = !uiState.isSaving,
                        singleLine = false,
                        minLines = 1,
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Default
                        )
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    TaskFlowTextField(
                        value = formattedDueDate,
                        onValueChange = {},
                        label = stringResource(id = R.string.field_due_date_required),
                        placeholder = stringResource(id = R.string.placeholder_due_date),
                        errorMessage = uiState.fechaErrorRes?.let { stringResource(id = it) },
                        errorTextStyle = TaskFlowTextStyles.taskFieldError,
                        enabled = !uiState.isSaving,
                        onFieldClick = {
                            focusManager.clearFocus()
                            onDateFieldClick()
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    PrioritySelector(
                        selectedPriority = uiState.prioridad,
                        onPrioritySelected = onPrioridadSelected,
                        enabled = !uiState.isSaving
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    TaskStatusSelector(
                        selectedStatus = uiState.estado,
                        onStatusSelected = onEstadoSelected,
                        enabled = !uiState.isSaving
                    )

                    if (!submitError.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OperationMessage(
                            type = MessageType.ERROR,
                            text = submitError,
                            compact = true
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    TaskFlowPrimaryButton(
                        text = stringResource(
                            id = if (uiState.isEditMode) {
                                R.string.action_save_changes
                            } else {
                                R.string.action_save
                            }
                        ),
                        onClick = {
                            focusManager.clearFocus()
                            onSubmit()
                        },
                        enabled = !uiState.isSaving,
                        loading = uiState.isSaving,
                        loadingText = stringResource(id = R.string.action_saving)
                    )
                }
            }
        }
    }
}

@Preview(name = "08 · Crear tarea", showBackground = true, widthDp = 414, heightDp = 771)
@Composable
private fun Preview08CreateTask() {
    TaskFlowTheme {
        TaskFormContent(
            uiState = TaskFormUiState(
                prioridad = TaskPriority.MEDIA,
                estado = TaskStatus.PENDIENTE
            ),
            onNavigateBack = {},
            onRetryLoad = {},
            onTituloChange = {},
            onMateriaChange = {},
            onDescripcionChange = {},
            onDateFieldClick = {},
            onPrioridadSelected = {},
            onEstadoSelected = {},
            onSubmit = {}
        )
    }
}

@Preview(name = "09 · Editar tarea", showBackground = true, widthDp = 414, heightDp = 771)
@Composable
private fun Preview09EditTask() {
    TaskFlowTheme {
        TaskFormContent(
            uiState = TaskFormUiState(
                taskId = 15,
                titulo = stringResource(id = R.string.placeholder_task_title),
                materia = stringResource(id = R.string.placeholder_task_subject),
                descripcion = stringResource(id = R.string.placeholder_task_description),
                selectedDate = LocalDate.of(2026, 11, 14),
                selectedTime = LocalTime.of(23, 59),
                prioridad = TaskPriority.MEDIA,
                estado = TaskStatus.PENDIENTE
            ),
            onNavigateBack = {},
            onRetryLoad = {},
            onTituloChange = {},
            onMateriaChange = {},
            onDescripcionChange = {},
            onDateFieldClick = {},
            onPrioridadSelected = {},
            onEstadoSelected = {},
            onSubmit = {}
        )
    }
}

@Preview(name = "10 · Validar tarea", showBackground = true, widthDp = 414, heightDp = 771)
@Composable
private fun Preview10ValidateTask() {
    TaskFlowTheme {
        TaskFormContent(
            uiState = TaskFormUiState(
                descripcion = stringResource(id = R.string.placeholder_task_description),
                tituloErrorRes = R.string.error_title_required,
                materiaErrorRes = R.string.error_subject_required,
                fechaErrorRes = R.string.error_due_date_required,
                prioridad = TaskPriority.MEDIA,
                estado = TaskStatus.PENDIENTE
            ),
            onNavigateBack = {},
            onRetryLoad = {},
            onTituloChange = {},
            onMateriaChange = {},
            onDescripcionChange = {},
            onDateFieldClick = {},
            onPrioridadSelected = {},
            onEstadoSelected = {},
            onSubmit = {}
        )
    }
}

@Preview(name = "16 · Guardando tarea", showBackground = true, widthDp = 414, heightDp = 771)
@Composable
private fun Preview16SavingTask() {
    TaskFlowTheme {
        TaskFormContent(
            uiState = TaskFormUiState(
                selectedDate = LocalDate.of(2026, 11, 14),
                selectedTime = LocalTime.of(23, 59),
                prioridad = TaskPriority.MEDIA,
                estado = TaskStatus.PENDIENTE,
                isSaving = true
            ),
            onNavigateBack = {},
            onRetryLoad = {},
            onTituloChange = {},
            onMateriaChange = {},
            onDescripcionChange = {},
            onDateFieldClick = {},
            onPrioridadSelected = {},
            onEstadoSelected = {},
            onSubmit = {}
        )
    }
}
