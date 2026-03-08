package com.example.mad_day3

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ThirdActivity : AppCompatActivity() {

    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Apply theme before setting content view
        applyStoredTheme()

        setContentView(R.layout.activity_third)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private fun applyStoredTheme() {
        sharedPref = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val themePref = sharedPref.getString("themePref", "light") ?: "light"
        ThemeUtils.applyTheme(themePref)
    }

    override fun onResume() {
        super.onResume()
        // Re-apply theme in case it was changed while activity was in background
        applyStoredTheme()
    }
}