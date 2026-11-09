package com.ivanna.proyects.taskflow.tasks.di

import com.ivanna.proyects.taskflow.auth.di.AuthModule
import com.ivanna.proyects.taskflow.core.di.CoreModule
import com.ivanna.proyects.taskflow.tasks.data.datasource.TasksApiService
import com.ivanna.proyects.taskflow.tasks.data.repository.TasksRepositoryImpl
import com.ivanna.proyects.taskflow.tasks.domain.repository.TasksRepository
import com.ivanna.proyects.taskflow.tasks.presentation.viewmodels.TaskDetailViewModelFactory
import com.ivanna.proyects.taskflow.tasks.presentation.viewmodels.TaskFormViewModelFactory
import com.ivanna.proyects.taskflow.tasks.presentation.viewmodels.TasksHomeViewModelFactory

class TasksModule(
    private val coreModule: CoreModule,
    private val authModule: AuthModule
) {
    private val apiService: TasksApiService by lazy {
        coreModule.retrofit.create(TasksApiService::class.java)
    }

    val repository: TasksRepository by lazy {
        TasksRepositoryImpl(
            apiService = apiService,
            json = coreModule.json
        )
    }

    val factoryTasksHome: TasksHomeViewModelFactory by lazy {
        TasksHomeViewModelFactory(
            tasksRepository = repository,
            authRepository = authModule.repository
        )
    }

    fun createTaskDetailFactory(taskId: Int): TaskDetailViewModelFactory {
        return TaskDetailViewModelFactory(
            taskId = taskId,
            tasksRepository = repository,
            authRepository = authModule.repository,
            sessionManager = coreModule.sessionManager
        )
    }

    fun createTaskFormFactory(taskId: Int?): TaskFormViewModelFactory {
        return TaskFormViewModelFactory(
            taskId = taskId,
            tasksRepository = repository,
            authRepository = authModule.repository
        )
    }
}
