package com.example.tfg_parking.ui.screens.secondary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tfg_parking.data.model.Favourite
import com.example.tfg_parking.data.model.Reservation
import com.example.tfg_parking.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouritesScreen(navController: NavController, vm: FavouritesViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favoritos") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { vm.fetchFavourites() }) {
                        Icon(Icons.Default.Refresh, "Actualizar")
                    }
                }
            )
        },
        bottomBar = { AppBottomBar(navController, current = Screen.Favourites.route) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                state.error != null -> Column(
                    Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { vm.fetchFavourites() }) { Text("Reintentar") }
                }

                state.favourites.isEmpty() -> Column(
                    Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Outlined.FavoriteBorder, null,
                        Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                    Text("No tienes favoritos aún", color = MaterialTheme.colorScheme.outline)
                }

                else -> LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.favourites, key = { it.id }) { fav ->
                        FavouriteCard(fav, onRemove = { vm.removeFavourite(fav.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun FavouriteCard(favourite: Favourite, onRemove: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(favourite.spotName, style = MaterialTheme.typography.titleSmall)
                if (favourite.address.isNotBlank())
                    Text(favourite.address, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.FavoriteSharp, null, tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController, vm: HistoryViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { vm.fetchHistory() }) {
                        Icon(Icons.Default.Refresh, "Actualizar")
                    }
                }
            )
        },
        bottomBar = { AppBottomBar(navController, current = Screen.History.route) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                state.error != null -> Column(
                    Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { vm.fetchHistory() }) { Text("Reintentar") }
                }

                state.reservations.isEmpty() -> Column(
                    Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.History, null,
                        Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                    Text("Aún no tienes reservas", color = MaterialTheme.colorScheme.outline)
                }

                else -> LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.reservations, key = { it.id }) { r ->
                        HistoryCard(r)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(reservation: Reservation) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(reservation.spotName, style = MaterialTheme.typography.titleSmall)
                Text("${reservation.reservedAt.take(10)} · ${reservation.durationHours} h",
                    style = MaterialTheme.typography.bodySmall)
                if (reservation.address.isNotBlank())
                    Text(reservation.address, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline)
            }
            Text("%.2f €".format(reservation.totalPrice),
                style = MaterialTheme.typography.titleMedium)
        }
    }
}

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