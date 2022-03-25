package com.github.windsekirun.playwithksp.core.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject

class PreferenceRepository @Inject constructor(private val sharedPreferences: SharedPreferences) {

    fun getString(key: String) = sharedPreferences.getString(key, "") ?: ""

    fun setString(key: String, value: String) = sharedPreferences.edit { putString(key, value) }

    fun getBoolean(key: String) = sharedPreferences.getBoolean(key, false)

    fun getBoolean(key: String, defValue: Boolean) = sharedPreferences.getBoolean(key, defValue)

    fun setBoolean(key: String, value: Boolean) = sharedPreferences.edit { putBoolean(key, value) }

    fun getLong(key: String) = sharedPreferences.getLong(key, 0)

    fun setLong(key: String, value: Long) = sharedPreferences.edit { putLong(key, value) }

    fun getInt(key: String) = sharedPreferences.getInt(key, 0)

    fun setInt(key: String, value: Int) = sharedPreferences.edit { putInt(key, value) }
}