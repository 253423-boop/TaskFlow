package com.ivanna.proyects.taskflow.tasks.domain.model

enum class TaskStatus(val apiValue: String) {
    PENDIENTE("pendiente"),
    COMPLETADA("completada");

    companion object {
        fun fromApiValue(value: String?): TaskStatus {
            return if (value?.trim()?.lowercase() == COMPLETADA.apiValue) {
                COMPLETADA
            } else {
                PENDIENTE
            }
        }
    }
}
