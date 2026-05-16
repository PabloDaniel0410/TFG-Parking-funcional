package com.example.tfg_parking.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tfg_parking.data.remote.Supabase
import com.example.tfg_parking.navigation.Screen
import io.github.jan.supabase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val user = remember { Supabase.client.auth.currentUserOrNull() }
    val email = user?.email ?: "Sin sesión"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // Avatar placeholder
            Box(
                Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null,
                    Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }

            Spacer(Modifier.height(16.dp))
            Text(email, style = MaterialTheme.typography.titleMedium)
            Text(user?.id?.take(8)?.let { "ID: $it…" } ?: "", style = MaterialTheme.typography.bodySmall)

            Spacer(Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            ProfileMenuItem(Icons.Default.Edit,    "Editar perfil")     { /* TODO */ }
            ProfileMenuItem(Icons.Default.History, "Historial")         { navController.navigate(Screen.History.route) }
            ProfileMenuItem(Icons.Default.Favorite,"Favoritos")         { navController.navigate(Screen.Favourites.route) }
            ProfileMenuItem(Icons.Default.Settings,"Configuración")     { navController.navigate(Screen.Settings.route) }

            Spacer(Modifier.weight(1f))

            OutlinedButton(
                onClick = {
                    Supabase.client.auth // logout se llama en el VM, aquí navegamos
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Logout, null)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión")
            }
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(icon, null)
        Spacer(Modifier.width(12.dp))
        Text(label, Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null)
    }
}