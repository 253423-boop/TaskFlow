package com.ivanna.proyects.taskflow.tasks.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateTaskDto(
    @SerialName("titulo")
    val titulo: String,
    @SerialName("materia")
    val materia: String,
    @SerialName("descripcion")
    val descripcion: String? = null,
    @SerialName("fecha_entrega")
    val fecha_entrega: String,
    @SerialName("prioridad")
    val prioridad: Int,
    @SerialName("estado")
    val estado: String
)
