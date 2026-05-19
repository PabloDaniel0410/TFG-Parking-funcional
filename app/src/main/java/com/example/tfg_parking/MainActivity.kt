package com.example.tfg_parking

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.example.tfg_parking.data.preferences.ThemePreferences
import com.example.tfg_parking.navigation.NavGraph
import com.example.tfg_parking.ui.theme.TFGParkingTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = this

            // ✅ produceState lanza la colección en un coroutine, no bloquea el hilo principal
            val darkMode by produceState(initialValue = false) {
                ThemePreferences.darkModeFlow(context).collect { value = it }
            }
            val accentColor by produceState(initialValue = "blue") {
                ThemePreferences.accentColorFlow(context).collect { value = it }
            }

            TFGParkingTheme(darkTheme = darkMode, accentColor = accentColor) {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}