package com.example.tfg_parking.ui.screens.secondary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tfg_parking.navigation.Screen

// ─────────────────────────────────────────────────────────────────────────────
// Pantalla: Favoritos
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouritesScreen(navController: NavController) {
    // TODO: conectar con ViewModel + Supabase
    val mockFavourites = listOf(
        "Parking Centro"       to "Calle Mayor 12",
        "Plaza Azul Norte"     to "Av. de la Paz 45",
        "Garaje Metropolitano" to "Paseo del Prado 8"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favoritos") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        },
        bottomBar = { AppBottomBar(navController, current = Screen.Favourites.route) }
    ) { padding ->
        if (mockFavourites.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No tienes favoritos aún")
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(mockFavourites) { (name, address) ->
                    FavouriteCard(name, address, onRemove = { /* TODO */ })
                }
            }
        }
    }
}

@Composable
private fun FavouriteCard(name: String, address: String, onRemove: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleSmall)
                Text(address, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Outlined.FavoriteBorder, contentDescription = "Favorito")
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Pantalla: Historial
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController) {
    // TODO: conectar con ViewModel + Supabase
    val mockHistory = listOf(
        Triple("Parking Centro",    "12/05/2025 · 2 h",  "4,00 €"),
        Triple("Plaza Azul Norte",  "10/05/2025 · 1 h",  "2,50 €"),
        Triple("Garaje Metro",      "01/05/2025 · 3 h",  "7,50 €")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        },
        bottomBar = { AppBottomBar(navController, current = Screen.History.route) }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(mockHistory) { (name, date, price) ->
                HistoryCard(name, date, price)
            }
        }
    }
}

@Composable
private fun HistoryCard(name: String, date: String, price: String) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleSmall)
                Text(date, style = MaterialTheme.typography.bodySmall)
            }
            Text(price, style = MaterialTheme.typography.titleMedium)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BottomBar compartida (evita repetir código)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AppBottomBar(navController: NavController, current: String) {
    NavigationBar {
        NavigationBarItem(
            selected = current == Screen.Home.route,
            onClick  = { navController.navigate(Screen.Home.route) },
            icon     = { Icon(Icons.Default.Map, null) },
            label    = { Text("Mapa") }
        )
        NavigationBarItem(
            selected = current == Screen.Favourites.route,
            onClick  = { navController.navigate(Screen.Favourites.route) },
            icon     = { Icon(Icons.Default.Favorite, null) },
            label    = { Text("Favoritos") }
        )
        NavigationBarItem(
            selected = current == Screen.History.route,
            onClick  = { navController.navigate(Screen.History.route) },
            icon     = { Icon(Icons.Default.History, null) },
            label    = { Text("Historial") }
        )
    }
}