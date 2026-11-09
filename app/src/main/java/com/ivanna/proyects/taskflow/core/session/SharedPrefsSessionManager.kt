package com.ivanna.proyects.taskflow.core.session

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SharedPrefsSessionManager(
    context: Context
) : SessionManager {

    private val prefs: SharedPreferences = (context.applicationContext ?: context)
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun saveToken(token: String) {
        prefs.edit { putString(KEY_ACCESS_TOKEN, token) }
    }

    override fun getToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    override fun saveUserName(name: String) {
        prefs.edit { putString(KEY_USER_NAME, name) }
    }

    override fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }

    override fun saveUserId(userId: Int) {
        prefs.edit { putInt(KEY_USER_ID, userId) }
    }

    override fun getUserId(): Int? {
        return if (prefs.contains(KEY_USER_ID)) {
            prefs.getInt(KEY_USER_ID, 0)
        } else {
            null
        }
    }

    override fun clearSession() {
        prefs.edit { clear() }
    }

    override fun hasActiveSession(): Boolean {
        return !getToken().isNullOrBlank()
    }

    private companion object {
        const val PREFS_NAME = "taskflow_session_prefs"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_USER_NAME = "user_name"
        const val KEY_USER_ID = "user_id"
    }
}
