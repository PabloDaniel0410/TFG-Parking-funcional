package com.example.tfg_parking.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tfg_parking.data.preferences.ThemePreferences
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToTerms: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val context       = LocalContext.current
    val scope         = rememberCoroutineScope()
    val darkMode      by ThemePreferences.darkModeFlow(context).collectAsState(initial = false)
    val accentColor   by ThemePreferences.accentColorFlow(context).collectAsState(initial = "blue")
    var notifications by remember { mutableStateOf(true) }

    val accentOptions = listOf(
        "blue"   to Color(0xFF6650A4),
        "green"  to Color(0xFF2E7D32),
        "orange" to Color(0xFFE65100),
        "red"    to Color(0xFFC62828)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
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
        ) {
            // ── Apariencia ────────────────────────────────────────────
            Text("Apariencia", style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Card(Modifier.fillMaxWidth()) {
                Column {
                    ListItem(
                        headlineContent = { Text("Modo oscuro") },
                        leadingContent  = { Icon(Icons.Default.DarkMode, null) },
                        trailingContent = {
                            Switch(
                                checked = darkMode,
                                onCheckedChange = {
                                    scope.launch { ThemePreferences.setDarkMode(context, it) }
                                }
                            )
                        }
                    )
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                    ListItem(
                        headlineContent   = { Text("Color de acento") },
                        leadingContent    = { Icon(Icons.Default.Palette, null) },
                        supportingContent = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                accentOptions.forEach { (key, color) ->
                                    val selected = accentColor == key
                                    Box(
                                        Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .then(
                                                if (selected)
                                                    Modifier.border(3.dp,
                                                        MaterialTheme.colorScheme.onBackground,
                                                        CircleShape)
                                                else Modifier
                                            )
                                            .clickable {
                                                scope.launch {
                                                    ThemePreferences.setAccentColor(context, key)
                                                }
                                            }
                                    )
                                }
                            }
                        }
                    )
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                    ListItem(
                        headlineContent = { Text("Notificaciones") },
                        leadingContent  = { Icon(Icons.Default.Notifications, null) },
                        trailingContent = {
                            Switch(checked = notifications, onCheckedChange = { notifications = it })
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Legal ─────────────────────────────────────────────────
            Text("Legal", style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Card(Modifier.fillMaxWidth()) {
                Column {
                    ListItem(
                        headlineContent = { Text("Política de privacidad") },
                        leadingContent  = { Icon(Icons.Default.PrivacyTip, null) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, null) },
                        modifier        = Modifier.clickable { onNavigateToPrivacy() }
                    )
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                    ListItem(
                        headlineContent = { Text("Términos y condiciones") },
                        leadingContent  = { Icon(Icons.Default.Description, null) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, null) },
                        modifier        = Modifier.clickable { onNavigateToTerms() }
                    )
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                    ListItem(
                        headlineContent = { Text("Acerca de") },
                        leadingContent  = { Icon(Icons.Default.Info, null) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, null) },
                        modifier        = Modifier.clickable { onNavigateToAbout() }
                    )
                }
            }
        }
    }
}