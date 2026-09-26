package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    val context: Context = context.applicationContext
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "field_report_preferences"
        private const val KEY_DEFAULT_OPERATOR_NAME = "key_default_operator_name"
        private const val KEY_PHOTO_COUNT = "key_photo_count"
    }

    var defaultOperatorName: String
        get() = prefs.getString(KEY_DEFAULT_OPERATOR_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_DEFAULT_OPERATOR_NAME, value.trim()).apply()

    var photoCount: Int
        get() = prefs.getInt(KEY_PHOTO_COUNT, 2)
        set(value) = prefs.edit().putInt(KEY_PHOTO_COUNT, value).apply()
}
