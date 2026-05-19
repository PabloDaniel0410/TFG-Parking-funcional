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
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import com.google.android.gms.maps.MapsInitializer.Renderer

class MainActivity : AppCompatActivity(), OnMapsSdkInitializedCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Pre-inicializa Maps de forma asíncrona para evitar bloqueo en el hilo principal
        MapsInitializer.initialize(applicationContext, Renderer.LATEST, this)

        enableEdgeToEdge()
        setContent {
            val context = this

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

    override fun onMapsSdkInitialized(renderer: Renderer) {
        // no-op: solo necesitamos que la inicialización ocurra antes de setContent
    }
}