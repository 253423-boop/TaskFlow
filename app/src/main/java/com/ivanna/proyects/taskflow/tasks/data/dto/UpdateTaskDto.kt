package com.ivanna.proyects.taskflow.tasks.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateTaskDto(
    @SerialName("titulo")
    val titulo: String? = null,
    @SerialName("materia")
    val materia: String? = null,
    @SerialName("descripcion")
    val descripcion: String? = null,
    @SerialName("fecha_entrega")
    val fechaEntrega: String? = null,
    @SerialName("prioridad")
    val prioridad: Int? = null,
    @SerialName("estado")
    val estado: String? = null
)
