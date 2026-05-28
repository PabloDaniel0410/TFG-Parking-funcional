package com.example.tfg_parking.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.tfg_parking.data.model.ParkingSpot
import com.example.tfg_parking.ui.screens.auth.LoginScreen
import com.example.tfg_parking.ui.screens.auth.RegisterScreen
import com.example.tfg_parking.ui.screens.booking.BookingScreen
import com.example.tfg_parking.ui.screens.home.HomeScreen
import com.example.tfg_parking.ui.screens.legal.AboutScreen
import com.example.tfg_parking.ui.screens.legal.PrivacyScreen
import com.example.tfg_parking.ui.screens.legal.TermsScreen
import com.example.tfg_parking.ui.screens.payment.PaymentMethodsScreen
import com.example.tfg_parking.ui.screens.profile.EditProfileScreen
import com.example.tfg_parking.ui.screens.profile.ProfileScreen
import com.example.tfg_parking.ui.screens.secondary.FavouritesScreen
import com.example.tfg_parking.ui.screens.secondary.HistoryScreen
import com.example.tfg_parking.ui.screens.settings.SettingsScreen
import com.example.tfg_parking.ui.screens.vehicles.VehiclesScreen
import kotlinx.serialization.json.Json

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess       = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                onReserveSpot = { spot ->
                    val json = Uri.encode(Json.encodeToString(ParkingSpot.serializer(), spot))
                    navController.navigate("${Screen.Booking.route}/$json")
                }
            )
        }

        composable(
            route     = "${Screen.Booking.route}/{spotJson}",
            arguments = listOf(navArgument("spotJson") { type = NavType.StringType })
        ) { backStack ->
            val raw  = backStack.arguments?.getString("spotJson") ?: ""

            val spot = runCatching {
                Json.decodeFromString(ParkingSpot.serializer(), raw)
            }.getOrNull() ?: return@composable

            BookingScreen(spot = spot, navController = navController)
        }

        composable(Screen.Favourites.route)     { FavouritesScreen(navController) }
        composable(Screen.History.route)        { HistoryScreen(navController) }
        composable(Screen.Profile.route)        { ProfileScreen(navController) }
        composable(Screen.EditProfile.route)    { EditProfileScreen(navController) }
        composable(Screen.Vehicles.route)       { VehiclesScreen(navController) }
        composable(Screen.PaymentMethods.route) { PaymentMethodsScreen(navController) }

        composable(Screen.Settings.route) {
            SettingsScreen(
                navController       = navController,
                onNavigateToPrivacy = { navController.navigate(Screen.Privacy.route) },
                onNavigateToTerms   = { navController.navigate(Screen.Terms.route) },
                onNavigateToAbout   = { navController.navigate(Screen.About.route) }
            )
        }

        composable(Screen.Privacy.route) { PrivacyScreen(navController) }
        composable(Screen.Terms.route)   { TermsScreen(navController) }
        composable(Screen.About.route)   { AboutScreen(navController) }
    }
}