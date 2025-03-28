package com.lex.qr.utils

import android.content.Context
import androidx.core.content.edit

class UserPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD = "password"
    }

    fun saveUser(email: String, password: String) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_EMAIL, email)
            putString(KEY_PASSWORD, password)
            apply()
        }
    }

    fun getUser(): Pair<String?, String?> {
        return Pair(
            prefs.getString(KEY_EMAIL, null),
            prefs.getString(KEY_PASSWORD, null)
        )
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun clearUser() {
        prefs.edit { clear() }
    }
}