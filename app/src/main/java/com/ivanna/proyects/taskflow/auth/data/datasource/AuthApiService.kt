package com.ivanna.proyects.taskflow.auth.data.datasource

import com.ivanna.proyects.taskflow.auth.data.dto.AuthResponseDto
import com.ivanna.proyects.taskflow.auth.data.dto.LoginRequestDto
import com.ivanna.proyects.taskflow.auth.data.dto.RegisterRequestDto
import com.ivanna.proyects.taskflow.auth.data.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): AuthResponseDto

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): AuthResponseDto

    @GET("auth/me")
    suspend fun getMe(): UserDto
}
