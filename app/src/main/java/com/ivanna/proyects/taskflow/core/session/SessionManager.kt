package com.ivanna.proyects.taskflow.core.session

interface SessionManager {
    fun saveToken(token: String)
    fun getToken(): String?
    fun saveUserName(name: String)
    fun getUserName(): String?
    fun saveUserId(userId: Int)
    fun getUserId(): Int?
    fun clearSession()
    fun hasActiveSession(): Boolean
}
