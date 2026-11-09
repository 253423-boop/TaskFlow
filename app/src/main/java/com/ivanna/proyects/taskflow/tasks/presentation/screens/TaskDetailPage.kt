package com.ivanna.proyects.taskflow.tasks.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ivanna.proyects.taskflow.TaskFlowApplication
import com.ivanna.proyects.taskflow.tasks.domain.model.Task
import com.ivanna.proyects.taskflow.tasks.domain.model.TaskPriority
import com.ivanna.proyects.taskflow.tasks.domain.model.TaskStatus
import com.ivanna.proyects.taskflow.tasks.presentation.components.DeleteTaskDialog
import com.ivanna.proyects.taskflow.tasks.presentation.components.ErrorContent
import com.ivanna.proyects.taskflow.tasks.presentation.components.LoadingContent
import com.ivanna.proyects.taskflow.tasks.presentation.util.TaskDateFormatter
import com.ivanna.proyects.taskflow.tasks.presentation.viewmodels.TaskDetailViewModel
import com.ivanna.proyects.taskflow.ui.components.MessageType
import com.ivanna.proyects.taskflow.ui.components.OperationMessage
import com.ivanna.proyects.taskflow.ui.components.TaskFlowDestructiveLink
import com.ivanna.proyects.taskflow.ui.components.TaskFlowHeader
import com.ivanna.proyects.taskflow.ui.components.TaskFlowPrimaryButton
import com.ivanna.proyects.taskflow.ui.components.TaskFlowSecondaryButton
import com.ivanna.proyects.taskflow.ui.theme.TaskFlowTextStyles
import com.ivanna.proyects.taskflow.ui.theme.TaskFlowTheme
import com.upchiapas.taskflow.R
import java.time.LocalDate
import java.time.LocalTime
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

@Composable
fun TaskDetailPage(
    taskId: Int,
    viewModel: TaskDetailViewModel = viewModel(
        factory = (LocalContext.current.applicationContext as TaskFlowApplication)
            .tasksModule
            .createTaskDetailFactory(taskId)
    ),
    onNavigateBack: () -> Unit,
    onDeleted: () -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    onSessionExpired: () -> Unit,
    pendingMessageRes: Int? = null,
    onConsumePendingMessage: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var hasResumedOnce by remember { mutableStateOf(false) }

    LifecycleResumeEffect(Unit) {
        if (hasResumedOnce) {
            viewModel.loadTask()
        } else {
            hasResumedOnce = true
        }
        onPauseOrDispose {}
    }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onDeleted()
        }
    }

    LaunchedEffect(uiState.isSessionExpired) {
        if (uiState.isSessionExpired) {
            onSessionExpired()
        }
    }

    val bannerSuccessText = (pendingMessageRes ?: uiState.operationSuccessRes)?.let {
        stringResource(id = it)
    }
    val bannerErrorText = uiState.operationErrorDetail
        ?: uiState.operationErrorRes?.let { stringResource(id = it) }

    LaunchedEffect(bannerSuccessText, bannerErrorText) {
        if (!bannerSuccessText.isNullOrBlank() || !bannerErrorText.isNullOrBlank()) {
            delay(3.5.seconds)
            if (pendingMessageRes != null) {
                onConsumePendingMessage()
            }
            viewModel.consumeMessages()
        }
    }

    if (uiState.showDeleteDialog) {
        DeleteTaskDialog(
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::onDeleteDismiss,
            isLoading = uiState.isActionLoading
        )
    }

    TaskDetailContent(
        uiState = uiState,
        bannerMessage = when {
            !bannerSuccessText.isNullOrBlank() -> MessageType.SUCCESS to bannerSuccessText
            !bannerErrorText.isNullOrBlank() -> MessageType.ERROR to bannerErrorText
            else -> null
        },
        onDismissBanner = {
            if (pendingMessageRes != null) {
                onConsumePendingMessage()
            }
            viewModel.consumeMessages()
        },
        onNavigateBack = onNavigateBack,
        onRetry = viewModel::loadTask,
        onCompleteClick = viewModel::markAsCompleted,
        onEditClick = { uiState.task?.let { onNavigateToEdit(it.id) } },
        onDeleteClick = viewModel::onDeleteRequest
    )
}

@Composable
fun TaskDetailContent(
    uiState: TaskDetailUiState,
    onNavigateBack: () -> Unit,
    onRetry: () -> Unit,
    onCompleteClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    bannerMessage: Pair<MessageType, String>? = null,
    onDismissBanner: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .systemBarsPadding()
            .padding(top = 12.dp)
    ) {
        TaskFlowHeader(
            title = stringResource(id = R.string.detail_title),
            showBack = true,
            backEnabled = !uiState.isActionLoading,
            onBack = onNavigateBack
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingContent(
                        message = stringResource(id = R.string.loading_tasks),
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                }
            }
            uiState.errorRes != null || !uiState.errorDetail.isNullOrBlank() || uiState.task == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center
                ) {
                    ErrorContent(
                        title = stringResource(id = R.string.error_load_task_detail),
                        subtitle = uiState.errorDetail ?: uiState.errorRes?.let {
                            stringResource(id = it)
                        } ?: stringResource(id = R.string.error_load_tasks_subtitle),
                        retryLabel = stringResource(id = R.string.action_retry),
                        onRetry = onRetry,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }
            }
            else -> {
                val task = uiState.task
                val isCompleted = task.estado == TaskStatus.COMPLETADA
                val displayOwnerName = uiState.ownerName.ifBlank {
                    stringResource(id = R.string.detail_owner_fallback)
                }
                val ownerLine = if (uiState.ownerId != null) {
                    stringResource(
                        id = R.string.detail_owner_with_id_format,
                        displayOwnerName,
                        uiState.ownerId
                    )
                } else {
                    stringResource(
                        id = R.string.detail_owner_name_only_format,
                        displayOwnerName
                    )
                }
                val descriptionValue = task.descripcion ?: stringResource(id = R.string.no_description_provided)
                val createdAtValue = task.createdAt
                    ?.takeIf { it.isNotBlank() }
                    ?.let { TaskDateFormatter.formatForDisplay(it) }
                    ?: stringResource(id = R.string.not_available_value)
                val dueDateValue = TaskDateFormatter.formatForDisplay(task.fechaEntrega)
                val priorityLabel = stringResource(
                    id = when (task.prioridad) {
                        TaskPriority.BAJA -> R.string.badge_priority_low
                        TaskPriority.MEDIA -> R.string.badge_priority_medium
                        TaskPriority.ALTA -> R.string.badge_priority_high
                    }
                )
                val statusLabel = stringResource(
                    id = if (isCompleted) R.string.status_completed else R.string.status_pending
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(top = 20.dp, bottom = 24.dp)
                ) {
                    if (bannerMessage != null) {
                        OperationMessage(
                            type = bannerMessage.first,
                            text = bannerMessage.second,
                            onDismiss = onDismissBanner,
                            modifier = Modifier.padding(horizontal = 21.85.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 21.85.dp),
                        shape = RoundedCornerShape(16.1.dp),
                        color = colorScheme.surfaceVariant
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 13.8.dp, vertical = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = task.titulo,
                                style = TaskFlowTextStyles.detailTitle,
                                color = colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            DetailTextLine(
                                text = stringResource(id = R.string.detail_id_format, task.id)
                            )
                            DetailTextLine(text = ownerLine)
                            DetailTextLine(
                                text = stringResource(id = R.string.detail_subject_format, task.materia)
                            )
                            DetailTextLine(
                                text = stringResource(id = R.string.detail_description_format, descriptionValue)
                            )
                            DetailTextLine(
                                text = stringResource(id = R.string.detail_created_at_format, createdAtValue)
                            )
                            DetailTextLine(
                                text = stringResource(id = R.string.detail_due_date_format, dueDateValue)
                            )
                            DetailTextLine(
                                text = stringResource(
                                    id = R.string.detail_priority_format,
                                    task.prioridad.level,
                                    priorityLabel
                                )
                            )
                            DetailTextLine(
                                text = stringResource(id = R.string.detail_status_format, statusLabel)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 27.6.dp)
                    ) {
                        if (isCompleted) {
                            TaskFlowPrimaryButton(
                                text = stringResource(id = R.string.action_task_completed),
                                onClick = {},
                                enabled = false,
                                leadingIconRes = R.drawable.ic_check
                            )
                        } else {
                            TaskFlowPrimaryButton(
                                text = stringResource(id = R.string.action_mark_completed),
                                onClick = onCompleteClick,
                                enabled = !uiState.isActionLoading,
                                loading = uiState.isCompleting,
                                loadingText = stringResource(id = R.string.action_completing),
                                leadingIconRes = R.drawable.ic_check
                            )
                        }

                        Spacer(modifier = Modifier.height(9.2.dp))

                        TaskFlowSecondaryButton(
                            text = stringResource(id = R.string.action_edit),
                            onClick = onEditClick,
                            enabled = !uiState.isActionLoading
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        TaskFlowDestructiveLink(
                            text = stringResource(id = R.string.action_delete_task),
                            onClick = onDeleteClick,
                            enabled = !uiState.isActionLoading
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailTextLine(text: String) {
    Text(
        text = text,
        style = TaskFlowTextStyles.detailBody,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun sampleDetailTask(status: TaskStatus): Task {
    return Task(
        id = 15,
        userId = 3,
        titulo = stringResource(id = R.string.preview_task_1_title),
        materia = stringResource(id = R.string.preview_task_1_subject),
        descripcion = stringResource(id = R.string.preview_task_1_desc),
        fechaEntrega = TaskDateFormatter.formatToApi(LocalDate.of(2026, 11, 20), LocalTime.of(23, 59)),
        prioridad = TaskPriority.ALTA,
        estado = status,
        vencida = false,
        createdAt = TaskDateFormatter.formatToApi(LocalDate.of(2026, 11, 5), LocalTime.of(10, 24))
    )
}

@Preview(name = "11 · Detalle de tarea", showBackground = true, widthDp = 414, heightDp = 771)
@Composable
private fun Preview11TaskDetail() {
    TaskFlowTheme {
        TaskDetailContent(
            uiState = TaskDetailUiState(
                task = sampleDetailTask(TaskStatus.PENDIENTE),
                ownerName = stringResource(id = R.string.preview_user_name),
                ownerId = 3,
                isLoading = false
            ),
            onNavigateBack = {},
            onRetry = {},
            onCompleteClick = {},
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(name = "17 · Tarea completada", showBackground = true, widthDp = 414, heightDp = 771)
@Composable
private fun Preview17TaskCompleted() {
    TaskFlowTheme {
        TaskDetailContent(
            uiState = TaskDetailUiState(
                task = sampleDetailTask(TaskStatus.COMPLETADA),
                ownerName = stringResource(id = R.string.preview_user_name),
                ownerId = 3,
                isLoading = false
            ),
            onNavigateBack = {},
            onRetry = {},
            onCompleteClick = {},
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}
