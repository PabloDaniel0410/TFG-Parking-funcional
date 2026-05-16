package com.example.tfg_parking

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.compose.rememberNavController
import com.example.tfg_parking.navigation.NavGraph
import com.example.tfg_parking.ui.theme.TFGParkingTheme

/**
 * AppCompatActivity (en lugar de ComponentActivity) es necesario para poder usar
 * SupportMapFragment dentro de un composable mediante AndroidView.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TFGParkingTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
