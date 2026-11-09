package com.ivanna.proyects.taskflow

import android.app.Application
import com.ivanna.proyects.taskflow.auth.di.AuthModule
import com.ivanna.proyects.taskflow.core.di.CoreModule
import com.ivanna.proyects.taskflow.tasks.di.TasksModule

class TaskFlowApplication : Application() {
    val coreModule: CoreModule by lazy { CoreModule(this) }
    val authModule: AuthModule by lazy { AuthModule(coreModule) }
    val tasksModule: TasksModule by lazy { TasksModule(coreModule, authModule) }
}
