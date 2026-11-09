package com.ivanna.proyects.taskflow.core.navigation

import kotlinx.serialization.Serializable

sealed interface TaskFlowRoute {
    @Serializable
    data object SessionCheck : TaskFlowRoute

    @Serializable
    data class Login(val sessionExpired: Boolean = false) : TaskFlowRoute

    @Serializable
    data object Register : TaskFlowRoute

    @Serializable
    data object TasksHome : TaskFlowRoute

    @Serializable
    data class TaskDetail(val taskId: Int) : TaskFlowRoute

    @Serializable
    data class TaskForm(val taskId: Int? = null) : TaskFlowRoute
}
