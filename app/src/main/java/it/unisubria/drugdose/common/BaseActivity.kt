package it.unisubria.drugdose.common

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        applicaTemaSalvato()

        super.onCreate(savedInstanceState)
    }

    private fun applicaTemaSalvato() {
        val sharedPref = getSharedPreferences("ImpostazioniApp", Context.MODE_PRIVATE)
        if (sharedPref.contains("theme_mode")) {
            val themeMode = sharedPref.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            AppCompatDelegate.setDefaultNightMode(themeMode)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}
