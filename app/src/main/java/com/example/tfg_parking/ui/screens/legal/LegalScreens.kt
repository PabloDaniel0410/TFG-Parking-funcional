package com.example.tfg_parking.ui.screens.legal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// ── Pantalla base reutilizable ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LegalScreen(title: String, body: String, navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// ── Política de privacidad ─────────────────────────────────────────────────
@Composable
fun PrivacyScreen(navController: NavController) {
    LegalScreen(
        title = "Política de privacidad",
        body  = """
TFG Parking recopila únicamente los datos necesarios para el funcionamiento de la aplicación:

• Correo electrónico y contraseña: usados para autenticarte de forma segura mediante Supabase Auth. Tu contraseña nunca se almacena en texto plano.

• Ubicación (opcional): se solicita en tiempo real para centrar el mapa en tu posición. No se almacena ni se comparte con terceros.

• Historial de reservas: guardado en nuestra base de datos para que puedas consultarlo. Solo tú tienes acceso a él.

Tus datos no se venden ni se ceden a terceros. Puedes solicitar la eliminación de tu cuenta y todos sus datos asociados contactando con el equipo de desarrollo.

Última actualización: mayo 2026.
        """.trimIndent(),
        navController = navController
    )
}

// ── Términos y condiciones ─────────────────────────────────────────────────
@Composable
fun TermsScreen(navController: NavController) {
    LegalScreen(
        title = "Términos y condiciones",
        body  = """
Al usar TFG Parking aceptas las siguientes condiciones:

1. Uso permitido
   La aplicación es un proyecto académico (TFG). Su uso es personal y no comercial.

2. Reservas
   Las reservas mostradas son de demostración. El equipo de desarrollo no se hace responsable de disponibilidad real de plazas.

3. Precisión de la información
   Los datos de plazas de parking se obtienen de nuestra base de datos y pueden no reflejar el estado en tiempo real.

4. Propiedad intelectual
   El código fuente, diseño y contenidos son propiedad del autor del TFG salvo que se indique lo contrario.

5. Modificaciones
   Estos términos pueden actualizarse sin previo aviso. El uso continuado de la app implica la aceptación de los cambios.

Última actualización: mayo 2026.
        """.trimIndent(),
        navController = navController
    )
}

// ── Acerca de ─────────────────────────────────────────────────────────────
@Composable
fun AboutScreen(navController: NavController) {
    LegalScreen(
        title = "Acerca de",
        body  = """
TFG Parking v1.0

Aplicación desarrollada como Trabajo de Fin de Grado.

Tecnologías utilizadas:
• Kotlin + Jetpack Compose
• Supabase (autenticación y base de datos)
• Google Maps SDK para Android
• Material Design 3

Desarrollado con Android Studio.

© 2026 — Todos los derechos reservados.
        """.trimIndent(),
        navController = navController
    )
}
