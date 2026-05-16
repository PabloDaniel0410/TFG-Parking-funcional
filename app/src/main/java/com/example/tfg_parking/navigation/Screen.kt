package com.example.tfg_parking.navigation

// ── Rutas ─────────────────────────────────────────────────────────────────────
sealed class Screen(val route: String) {
    // Auth
    object Login    : Screen("login")
    object Register : Screen("register")

    // Principal (post-login)
    object Home     : Screen("home")          // Mapa con plazas

    // Secundarias
    object Favourites : Screen("favourites")  // Plazas guardadas
    object History    : Screen("history")     // Historial de reservas

    // Perfil & configuración
    object Profile  : Screen("profile")
    object Settings : Screen("settings")

    // Legales
    object Privacy  : Screen("privacy")
    object Terms    : Screen("terms")
    object About    : Screen("about")
}