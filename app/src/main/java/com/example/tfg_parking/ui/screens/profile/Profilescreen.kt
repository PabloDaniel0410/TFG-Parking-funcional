package com.example.tfg_parking.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tfg_parking.data.remote.Supabase
import com.example.tfg_parking.navigation.Screen
import io.github.jan.supabase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    vm: ProfileViewModel = viewModel()
) {
    val currentUser = Supabase.client.auth.currentUserOrNull()
    val profile by vm.profile.collectAsState()
    val context = LocalContext.current

    // Image picker
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { vm.uploadAvatar(context, it) }
    }

    LaunchedEffect(Unit) { vm.loadProfile() }

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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Avatar ──────────────────────────────────────────────
            Box(contentAlignment = Alignment.BottomEnd) {
                if (profile?.avatarUrl?.isNotBlank() == true) {
                    AsyncImage(
                        model = profile!!.avatarUrl,
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(96.dp).clip(CircleShape)
                    )
                } else {
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
                }
                SmallFloatingActionButton(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.size(28.dp),
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.Default.CameraAlt, null, Modifier.size(14.dp))
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Nombre y email ──────────────────────────────────────
            Text(
                profile?.displayName?.ifBlank { currentUser?.email ?: "" } ?: (currentUser?.email ?: ""),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                currentUser?.email ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            // ── Menú ────────────────────────────────────────────────
            Text("Cuenta", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))

            ProfileMenuItem(Icons.Default.Edit,         "Editar perfil")     { navController.navigate(Screen.EditProfile.route) }
            ProfileMenuItem(Icons.Default.DirectionsCar,"Mis vehículos")     { navController.navigate(Screen.Vehicles.route) }
            ProfileMenuItem(Icons.Default.CreditCard,   "Métodos de pago")   { navController.navigate(Screen.PaymentMethods.route) }

            Spacer(Modifier.height(8.dp))
            Text("Actividad", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))

            ProfileMenuItem(Icons.Default.History,  "Historial")     { navController.navigate(Screen.History.route) }
            ProfileMenuItem(Icons.Default.Favorite, "Favoritos")     { navController.navigate(Screen.Favourites.route) }

            Spacer(Modifier.height(8.dp))
            Text("App", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))

            ProfileMenuItem(Icons.Default.Settings, "Configuración")  { navController.navigate(Screen.Settings.route) }

            Spacer(Modifier.height(24.dp))

            OutlinedButton(
                onClick = {
                    vm.signOut()
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
fun ProfileMenuItem(
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