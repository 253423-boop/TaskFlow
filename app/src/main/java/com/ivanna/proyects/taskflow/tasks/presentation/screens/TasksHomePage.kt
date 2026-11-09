package com.ivanna.proyects.taskflow.tasks.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import com.ivanna.proyects.taskflow.tasks.presentation.components.DeleteTaskDialogOverlay
import com.ivanna.proyects.taskflow.tasks.presentation.components.EmptyContent
import com.ivanna.proyects.taskflow.tasks.presentation.components.ErrorContent
import com.ivanna.proyects.taskflow.tasks.presentation.components.LoadingContent
import com.ivanna.proyects.taskflow.tasks.presentation.components.TaskCard
import com.ivanna.proyects.taskflow.tasks.presentation.components.TaskFilterRow
import com.ivanna.proyects.taskflow.tasks.presentation.util.TaskDateFormatter
import com.ivanna.proyects.taskflow.tasks.presentation.viewmodels.TasksHomeViewModel
import com.ivanna.proyects.taskflow.ui.components.MessageType
import com.ivanna.proyects.taskflow.ui.components.OperationMessage
import com.ivanna.proyects.taskflow.ui.components.TaskFlowHeader
import com.ivanna.proyects.taskflow.ui.theme.TaskFlowTheme
import com.upchiapas.taskflow.R
import java.time.LocalDate
import java.time.LocalTime
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

@Composable
fun TasksHomePage(
    viewModel: TasksHomeViewModel = viewModel(
        factory = (LocalContext.current.applicationContext as TaskFlowApplication)
            .tasksModule
            .factoryTasksHome
    ),
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToCreate: () -> Unit,
    onLogout: () -> Unit,
    onSessionExpired: () -> Unit,
    pendingMessageRes: Int? = null,
    onConsumePendingMessage: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var hasResumedOnce by remember { mutableStateOf(false) }

    LifecycleResumeEffect(Unit) {
        if (hasResumedOnce) {
            viewModel.loadTasks(showLoading = false)
        } else {
            hasResumedOnce = true
        }
        onPauseOrDispose {}
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

    TasksHomeContent(
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
        onFilterSelected = viewModel::onFilterSelected,
        onRefresh = { viewModel.loadTasks(showLoading = false, isRefresh = true) },
        onRetry = { viewModel.loadTasks(showLoading = true) },
        onTaskClick = onNavigateToDetail,
        onCreateClick = onNavigateToCreate,
        onLogoutClick = {
            viewModel.logout()
            onLogout()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksHomeContent(
    uiState: TasksHomeUiState,
    onFilterSelected: (TaskFilter) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onTaskClick: (Int) -> Unit,
    onCreateClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
    bannerMessage: Pair<MessageType, String>? = null,
    onDismissBanner: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val hasError = uiState.errorRes != null || !uiState.errorDetail.isNullOrBlank()
    val showInitialLoading = uiState.isLoading && !uiState.hasLoadedOnce && uiState.tasks.isEmpty()
    val showInitialError = hasError && !uiState.hasLoadedOnce && uiState.tasks.isEmpty()
    val showFullEmpty = !uiState.isLoading &&
        !hasError &&
        uiState.tasks.isEmpty() &&
        uiState.selectedFilter == TaskFilter.ALL
    val showFilters = !showInitialLoading && !showInitialError && !showFullEmpty
    val showFab = showFilters && !hasError

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp)
        ) {
            TaskFlowHeader(
                title = stringResource(id = R.string.section_my_tasks),
                isHomeTitle = true,
                showBack = false,
                showLogout = true,
                onLogout = onLogoutClick
            )

            if (bannerMessage != null) {
                Spacer(modifier = Modifier.height(14.dp))
                OperationMessage(
                    type = bannerMessage.first,
                    text = bannerMessage.second,
                    onDismiss = onDismissBanner,
                    modifier = Modifier.padding(horizontal = 27.6.dp)
                )
            }

            if (showFilters) {
                Spacer(modifier = Modifier.height(16.dp))
                TaskFilterRow(
                    selectedFilter = uiState.selectedFilter,
                    onFilterSelected = onFilterSelected,
                    modifier = Modifier.padding(horizontal = 25.3.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when {
                    uiState.isLoading && uiState.tasks.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingContent(
                                message = stringResource(id = R.string.loading_tasks),
                                modifier = Modifier.padding(bottom = 48.dp)
                            )
                        }
                    }
                    hasError && uiState.tasks.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            contentAlignment = Alignment.Center
                        ) {
                            ErrorContent(
                                title = stringResource(id = R.string.error_load_tasks_title),
                                subtitle = uiState.errorDetail ?: uiState.errorRes?.let {
                                    stringResource(id = it)
                                } ?: stringResource(id = R.string.error_load_tasks_subtitle),
                                retryLabel = stringResource(id = R.string.action_retry),
                                onRetry = onRetry,
                                modifier = Modifier.padding(bottom = 32.dp)
                            )
                        }
                    }
                    uiState.tasks.isEmpty() -> {
                        val isAllFilter = uiState.selectedFilter == TaskFilter.ALL
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyContent(
                                title = stringResource(id = R.string.empty_tasks_title),
                                subtitle = stringResource(
                                    id = if (isAllFilter) {
                                        R.string.empty_tasks_subtitle
                                    } else {
                                        R.string.empty_filtered_tasks_subtitle
                                    }
                                ),
                                actionLabel = if (isAllFilter) {
                                    stringResource(id = R.string.action_add_task_plus)
                                } else {
                                    null
                                },
                                onActionClick = if (isAllFilter) onCreateClick else null,
                                modifier = Modifier.padding(bottom = 32.dp)
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 25.3.dp,
                                end = 25.3.dp,
                                top = 4.dp,
                                bottom = 104.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(13.8.dp)
                        ) {
                            items(
                                items = uiState.tasks,
                                key = { task -> task.id }
                            ) { task ->
                                TaskCard(
                                    task = task,
                                    onClick = { onTaskClick(task.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showFab) {
            FloatingActionButton(
                onClick = onCreateClick,
                shape = CircleShape,
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 2.dp
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 28.75.dp, bottom = 36.dp)
                    .size(66.7.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = stringResource(id = R.string.cd_add_task),
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@Composable
private fun samplePendingTasks(): List<Task> {
    val createdAtIso = TaskDateFormatter.formatToApi(LocalDate.of(2026, 11, 5), LocalTime.of(10, 24))
    return listOf(
        Task(
            id = 15,
            userId = 3,
            titulo = stringResource(id = R.string.preview_task_1_title),
            materia = stringResource(id = R.string.preview_task_1_subject),
            descripcion = stringResource(id = R.string.preview_task_1_desc),
            fechaEntrega = TaskDateFormatter.formatToApi(LocalDate.of(2026, 11, 20), LocalTime.of(23, 59)),
            prioridad = TaskPriority.ALTA,
            estado = TaskStatus.PENDIENTE,
            vencida = false,
            createdAt = createdAtIso
        ),
        Task(
            id = 14,
            userId = 3,
            titulo = stringResource(id = R.string.preview_task_2_title),
            materia = stringResource(id = R.string.preview_task_2_subject),
            descripcion = null,
            fechaEntrega = TaskDateFormatter.formatToApi(LocalDate.of(2026, 11, 14), LocalTime.of(23, 59)),
            prioridad = TaskPriority.MEDIA,
            estado = TaskStatus.PENDIENTE,
            vencida = false,
            createdAt = createdAtIso
        ),
        Task(
            id = 12,
            userId = 3,
            titulo = stringResource(id = R.string.preview_task_3_title),
            materia = stringResource(id = R.string.preview_task_3_subject),
            descripcion = null,
            fechaEntrega = TaskDateFormatter.formatToApi(LocalDate.of(2026, 11, 6), LocalTime.of(23, 59)),
            prioridad = TaskPriority.BAJA,
            estado = TaskStatus.PENDIENTE,
            vencida = true,
            createdAt = createdAtIso
        )
    )
}

@Composable
private fun sampleCompletedTask(): Task {
    return Task(
        id = 14,
        userId = 3,
        titulo = stringResource(id = R.string.preview_task_2_title),
        materia = stringResource(id = R.string.preview_task_2_subject),
        descripcion = null,
        fechaEntrega = TaskDateFormatter.formatToApi(LocalDate.of(2026, 11, 14), LocalTime.of(23, 59)),
        prioridad = TaskPriority.MEDIA,
        estado = TaskStatus.COMPLETADA,
        vencida = false,
        createdAt = TaskDateFormatter.formatToApi(LocalDate.of(2026, 11, 5), LocalTime.of(10, 24))
    )
}

@Preview(name = "04 · Home · tareas", showBackground = true, widthDp = 414, heightDp = 771)
@Composable
private fun Preview04HomeTasks() {
    TaskFlowTheme {
        TasksHomeContent(
            uiState = TasksHomeUiState(
                tasks = samplePendingTasks(),
                isLoading = false,
                hasLoadedOnce = true
            ),
            onFilterSelected = {},
            onRefresh = {},
            onRetry = {},
            onTaskClick = {},
            onCreateClick = {},
            onLogoutClick = {}
        )
    }
}

@Preview(name = "05 · Cargando", showBackground = true, widthDp = 414, heightDp = 771)
@Composable
private fun Preview05HomeLoading() {
    TaskFlowTheme {
        TasksHomeContent(
            uiState = TasksHomeUiState(
                isLoading = true,
                hasLoadedOnce = false
            ),
            onFilterSelected = {},
            onRefresh = {},
            onRetry = {},
            onTaskClick = {},
            onCreateClick = {},
            onLogoutClick = {}
        )
    }
}

@Preview(name = "06 · Sin tareas", showBackground = true, widthDp = 414, heightDp = 771)
@Composable
private fun Preview06HomeEmpty() {
    TaskFlowTheme {
        TasksHomeContent(
            uiState = TasksHomeUiState(
                tasks = emptyList(),
                isLoading = false,
                hasLoadedOnce = true
            ),
            onFilterSelected = {},
            onRefresh = {},
            onRetry = {},
            onTaskClick = {},
            onCreateClick = {},
            onLogoutClick = {}
        )
    }
}

@Preview(name = "07 · Error de conexión", showBackground = true, widthDp = 414, heightDp = 771)
@Composable
private fun Preview07HomeError() {
    TaskFlowTheme {
        TasksHomeContent(
            uiState = TasksHomeUiState(
                isLoading = false,
                hasLoadedOnce = false,
                errorRes = R.string.error_load_tasks_subtitle
            ),
            onFilterSelected = {},
            onRefresh = {},
            onRetry = {},
            onTaskClick = {},
            onCreateClick = {},
            onLogoutClick = {}
        )
    }
}

@Preview(name = "12 · Confirmar eliminación", showBackground = true, widthDp = 414, heightDp = 771)
@Composable
private fun Preview12ConfirmDelete() {
    TaskFlowTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            TasksHomeContent(
                uiState = TasksHomeUiState(
                    tasks = samplePendingTasks(),
                    isLoading = false,
                    hasLoadedOnce = true
                ),
                onFilterSelected = {},
                onRefresh = {},
                onRetry = {},
                onTaskClick = {},
                onCreateClick = {},
                onLogoutClick = {}
            )
            DeleteTaskDialogOverlay(
                onConfirm = {},
                onDismiss = {},
                isLoading = false
            )
        }
    }
}

@Preview(name = "18 · Filtro completadas", showBackground = true, widthDp = 414, heightDp = 771)
@Composable
private fun Preview18CompletedFilter() {
    TaskFlowTheme {
        TasksHomeContent(
            uiState = TasksHomeUiState(
                tasks = listOf(sampleCompletedTask()),
                selectedFilter = TaskFilter.COMPLETED,
                isLoading = false,
                hasLoadedOnce = true
            ),
            onFilterSelected = {},
            onRefresh = {},
            onRetry = {},
            onTaskClick = {},
            onCreateClick = {},
            onLogoutClick = {}
        )
    }
}
