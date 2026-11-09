package com.ivanna.proyects.taskflow.tasks.domain.model

enum class TaskPriority(val level: Int) {
    BAJA(1),
    MEDIA(2),
    ALTA(3);

    companion object {
        fun fromLevel(level: Int?): TaskPriority {
            return when (level) {
                BAJA.level -> BAJA
                ALTA.level -> ALTA
                else -> MEDIA
            }
        }
    }
}
