package com.ivanna.proyects.taskflow.tasks.data.datasource

import com.ivanna.proyects.taskflow.tasks.data.dto.CreateTaskDto
import com.ivanna.proyects.taskflow.tasks.data.dto.TaskDto
import com.ivanna.proyects.taskflow.tasks.data.dto.UpdateTaskDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TasksApiService {
    @GET("tasks/")
    suspend fun getTasks(@Query("estado") estado: String? = null): List<TaskDto>

    @GET("tasks/proxima-entrega")
    suspend fun getNextDelivery(): Response<TaskDto?>

    @GET("tasks/{id}")
    suspend fun getTaskById(@Path("id") id: Int): TaskDto

    @POST("tasks/")
    suspend fun createTask(@Body body: CreateTaskDto): TaskDto

    @PUT("tasks/{id}")
    suspend fun updateTask(@Path("id") id: Int, @Body body: UpdateTaskDto): TaskDto

    @PUT("tasks/{id}/completar")
    suspend fun completeTask(@Path("id") id: Int): TaskDto

    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") id: Int): Response<Unit>
}
