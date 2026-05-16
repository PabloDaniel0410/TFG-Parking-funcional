package com.example.tfg_parking.navigation

sealed class Screen(val route: String) {
    object Login       : Screen("login")
    object Register    : Screen("register")
    object Home        : Screen("home")
    object Favourites  : Screen("favourites")
    object History     : Screen("history")
    object Booking     : Screen("booking")
    object Profile     : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object Settings    : Screen("settings")
    object Privacy     : Screen("privacy")
    object Terms       : Screen("terms")
    object About       : Screen("about")
}