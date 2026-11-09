package com.ivanna.proyects.taskflow.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.ivanna.proyects.taskflow.auth.presentation.screens.LoginPage
import com.ivanna.proyects.taskflow.auth.presentation.screens.RegisterPage
import com.ivanna.proyects.taskflow.tasks.presentation.screens.TaskDetailPage
import com.ivanna.proyects.taskflow.tasks.presentation.screens.TaskFormPage
import com.ivanna.proyects.taskflow.tasks.presentation.screens.TasksHomePage
import com.upchiapas.taskflow.R

private const val KEY_OPERATION_MSG_RES = "operation_msg_res"

@Composable
fun TaskFlowNavHost(
    navController: NavHostController = rememberNavController()
) {
    val navigateToTasksClearingBackStack = {
        navController.navigate(TaskFlowRoute.TasksHome) {
            popUpTo(navController.graph.id) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    val navigateToLoginClearingBackStack: (Boolean) -> Unit = { sessionExpired ->
        navController.navigate(TaskFlowRoute.Login(sessionExpired = sessionExpired)) {
            popUpTo(navController.graph.id) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = TaskFlowRoute.SessionCheck
    ) {
        composable<TaskFlowRoute.SessionCheck> {
            SessionCheckPage(
                onNavigateToLogin = navigateToLoginClearingBackStack,
                onNavigateToTasksHome = navigateToTasksClearingBackStack
            )
        }

        composable<TaskFlowRoute.Login> { backStackEntry ->
            val route = backStackEntry.toRoute<TaskFlowRoute.Login>()
            LoginPage(
                sessionExpired = route.sessionExpired,
                onLoginSuccess = navigateToTasksClearingBackStack,
                onNavigateToRegister = {
                    navController.navigate(TaskFlowRoute.Register) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<TaskFlowRoute.Register> {
            RegisterPage(
                onRegisterSuccess = navigateToTasksClearingBackStack,
                onNavigateToLogin = {
                    if (!navController.popBackStack()) {
                        navigateToLoginClearingBackStack(false)
                    }
                }
            )
        }

        composable<TaskFlowRoute.TasksHome> { backStackEntry ->
            val pendingMessageRes by backStackEntry.savedStateHandle
                .getStateFlow<Int?>(KEY_OPERATION_MSG_RES, null)
                .collectAsStateWithLifecycle()

            TasksHomePage(
                onNavigateToDetail = { taskId ->
                    navController.navigate(TaskFlowRoute.TaskDetail(taskId)) {
                        launchSingleTop = true
                    }
                },
                onNavigateToCreate = {
                    navController.navigate(TaskFlowRoute.TaskForm(taskId = null)) {
                        launchSingleTop = true
                    }
                },
                onLogout = { navigateToLoginClearingBackStack(false) },
                onSessionExpired = { navigateToLoginClearingBackStack(true) },
                pendingMessageRes = pendingMessageRes,
                onConsumePendingMessage = {
                    backStackEntry.savedStateHandle[KEY_OPERATION_MSG_RES] = null
                }
            )
        }

        composable<TaskFlowRoute.TaskDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<TaskFlowRoute.TaskDetail>()
            val pendingMessageRes by backStackEntry.savedStateHandle
                .getStateFlow<Int?>(KEY_OPERATION_MSG_RES, null)
                .collectAsStateWithLifecycle()

            TaskDetailPage(
                taskId = route.taskId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onDeleted = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(KEY_OPERATION_MSG_RES, R.string.msg_task_deleted)
                    navController.popBackStack()
                },
                onNavigateToEdit = { editTaskId ->
                    navController.navigate(TaskFlowRoute.TaskForm(taskId = editTaskId)) {
                        launchSingleTop = true
                    }
                },
                onSessionExpired = { navigateToLoginClearingBackStack(true) },
                pendingMessageRes = pendingMessageRes,
                onConsumePendingMessage = {
                    backStackEntry.savedStateHandle[KEY_OPERATION_MSG_RES] = null
                }
            )
        }

        composable<TaskFlowRoute.TaskForm> { backStackEntry ->
            val route = backStackEntry.toRoute<TaskFlowRoute.TaskForm>()
            TaskFormPage(
                taskId = route.taskId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSaveSuccess = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(KEY_OPERATION_MSG_RES, R.string.msg_task_saved)
                    navController.popBackStack()
                },
                onSessionExpired = { navigateToLoginClearingBackStack(true) }
            )
        }
    }
}
