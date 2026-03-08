package com.example.mad_day3

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeUtils {

    fun applyTheme(theme: String) {
        val nightMode = when (theme) {
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }

        // Only apply if different from current
        if (AppCompatDelegate.getDefaultNightMode() != nightMode) {
            AppCompatDelegate.setDefaultNightMode(nightMode)
        }
    }

    fun getCurrentTheme(context: Context): String {
        val sharedPref = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("themePref", "light") ?: "light"
    }

    fun saveTheme(context: Context, theme: String) {
        val sharedPref = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        sharedPref.edit().putString("themePref", theme).apply()
    }
}