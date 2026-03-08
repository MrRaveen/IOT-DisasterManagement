package com.example.mad_day3

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.mad_day3.Warning.WarningFragment
import com.example.mad_day3.databinding.ActivityMainBinding
import com.example.mad_day3.databinding.ActivitySecondBinding

class SecondActivity : AppCompatActivity() {
    private lateinit var sharedPref: SharedPreferences
    private lateinit var binding: ActivitySecondBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Apply theme BEFORE setContentView
        applyStoredTheme()

//        val PREF_NAME = "prefs"
//        val KEY_NAME = "city"
//        lateinit var sharedPref : SharedPreferences
//        sharedPref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
//        var savedName = sharedPref.getString(KEY_NAME, null)
//        Toast.makeText(this, "Out ${savedName}", Toast.LENGTH_LONG).show()

        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (savedInstanceState == null) {
            replaceFragment(alerts())
        }
        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.alertPart -> replaceFragment(alerts())
                R.id.weatherPart -> replaceFragment(weather())
                R.id.warningsPart -> replaceFragment(WarningFragment())
                R.id.settingsPart -> replaceFragment(SettingsFragment())
                else -> {
                }
            }
            true
        }
    }
    private fun replaceFragment(fragment : Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment)
        fragmentTransaction.commit()
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