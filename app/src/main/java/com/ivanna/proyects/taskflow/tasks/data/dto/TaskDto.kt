package com.ivanna.proyects.taskflow.tasks.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TaskDto(
    @SerialName("id")
    val id: Int,
    @SerialName("user_id")
    val user_id: Int? = null,
    @SerialName("titulo")
    val titulo: String,
    @SerialName("materia")
    val materia: String,
    @SerialName("descripcion")
    val descripcion: String? = null,
    @SerialName("fecha_entrega")
    val fecha_entrega: String,
    @SerialName("prioridad")
    val prioridad: Int? = 2,
    @SerialName("estado")
    val estado: String,
    @SerialName("vencida")
    val vencida: Boolean? = false,
    @SerialName("created_at")
    val created_at: String? = null
)
