package com.example.tfg_parking.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.tfg_parking.ui.screens.auth.LoginScreen
import com.example.tfg_parking.ui.screens.auth.RegisterScreen
import com.example.tfg_parking.ui.screens.home.HomeScreen
import com.example.tfg_parking.ui.screens.secondary.FavouritesScreen
import com.example.tfg_parking.ui.screens.secondary.HistoryScreen
import com.example.tfg_parking.ui.screens.profile.ProfileScreen
import com.example.tfg_parking.ui.screens.settings.SettingsScreen
import com.example.tfg_parking.ui.screens.legal.PrivacyScreen
import com.example.tfg_parking.ui.screens.legal.TermsScreen
import com.example.tfg_parking.ui.screens.legal.AboutScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ── Auth ──────────────────────────────────────────────────────────
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess  = { navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }},
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }},
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        // ── Principal ─────────────────────────────────────────────────────
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        // ── Secundarias ───────────────────────────────────────────────────
        composable(Screen.Favourites.route) {
            FavouritesScreen(navController = navController)
        }
        composable(Screen.History.route) {
            HistoryScreen(navController = navController)
        }

        // ── Perfil & Ajustes ──────────────────────────────────────────────
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                navController   = navController,
                onNavigateToPrivacy = { navController.navigate(Screen.Privacy.route) },
                onNavigateToTerms   = { navController.navigate(Screen.Terms.route)   },
                onNavigateToAbout   = { navController.navigate(Screen.About.route)   }
            )
        }

        // ── Legales ───────────────────────────────────────────────────────
        composable(Screen.Privacy.route) { PrivacyScreen(navController) }
        composable(Screen.Terms.route)   { TermsScreen(navController)   }
        composable(Screen.About.route)   { AboutScreen(navController)   }
    }
}