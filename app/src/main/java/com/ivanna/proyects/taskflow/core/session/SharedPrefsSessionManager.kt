package com.ivanna.proyects.taskflow.core.session

import android.content.Context
import android.content.SharedPreferences

class SharedPrefsSessionManager(
    context: Context
) : SessionManager {

    private val prefs: SharedPreferences = (context.applicationContext ?: context)
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun saveToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    override fun getToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    override fun saveUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name).apply()
    }

    override fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }

    override fun clearSession() {
        prefs.edit().clear().apply()
    }

    override fun hasActiveSession(): Boolean {
        return !getToken().isNullOrBlank()
    }

    private companion object {
        const val PREFS_NAME = "taskflow_session_prefs"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_USER_NAME = "user_name"
    }
}
