package com.example.tfg_parking.ui.screens.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tfg_parking.data.model.PaymentMethod
import com.example.tfg_parking.data.remote.Supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

// ── Data class auxiliar para leer user_balance ─────────────────────────────
@Serializable
private data class UserBalanceRow(
    @SerialName("user_id") val userId: String = "",
    val balance: Double = 0.0
)

// ── ViewModel ──────────────────────────────────────────────────────────────
data class PaymentUiState(
    val methods: List<PaymentMethod> = emptyList(),
    val userBalance: Double = 0.0,   // saldo real de user_balance
    val isLoading: Boolean = false,
    val error: String? = null
)

class PaymentViewModel : ViewModel() {
    private val _state = MutableStateFlow(PaymentUiState())
    val state: StateFlow<PaymentUiState> = _state

    init {
        load()
        loadUserBalance()
    }

    // Carga los métodos de pago
    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val uid = Supabase.client.auth.currentUserOrNull()?.id ?: run {
                    _state.value = _state.value.copy(isLoading = false, error = "Sin sesión")
                    return@launch
                }
                val list = Supabase.client.postgrest["payment_methods"]
                    .select { filter { eq("user_id", uid) } }
                    .decodeList<PaymentMethod>()
                _state.value = _state.value.copy(methods = list, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    // Carga el saldo real desde user_balance
    fun loadUserBalance() {
        viewModelScope.launch {
            try {
                val uid = Supabase.client.auth.currentUserOrNull()?.id ?: return@launch
                val rows = Supabase.client.postgrest["user_balance"]
                    .select { filter { eq("user_id", uid) } }
                    .decodeList<UserBalanceRow>()
                val balance = rows.firstOrNull()?.balance ?: 0.0
                _state.value = _state.value.copy(userBalance = balance)
            } catch (_: Exception) {}
        }
    }

    // Añade un método de pago Y acredita el saldo inicial en user_balance
    fun add(type: String, label: String, initialBalance: Double) {
        viewModelScope.launch {
            try {
                val uid = Supabase.client.auth.currentUserOrNull()?.id ?: return@launch
                val isFirst = _state.value.methods.isEmpty()

                // 1. Insertar el método de pago
                Supabase.client.postgrest["payment_methods"].insert(buildJsonObject {
                    put("user_id",     uid)
                    put("method_type", type)
                    put("label",       label)
                    put("balance",     initialBalance)
                    put("is_default",  isFirst)
                })

                // 2. Acreditar el saldo inicial en user_balance (upsert)
                if (initialBalance > 0.0) {
                    creditUserBalance(uid, initialBalance)
                }

                load()
                loadUserBalance()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            try {
                Supabase.client.postgrest["payment_methods"].delete { filter { eq("id", id) } }
                load()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    // Recarga saldo: actualiza payment_methods Y user_balance
    fun addBalance(id: Int, currentMethodBalance: Double, amount: Double) {
        viewModelScope.launch {
            try {
                val uid = Supabase.client.auth.currentUserOrNull()?.id ?: return@launch

                // 1. Actualizar el balance del método de pago concreto
                Supabase.client.postgrest["payment_methods"]
                    .update({ set("balance", currentMethodBalance + amount) }) {
                        filter { eq("id", id) }
                    }

                // 2. Acreditar la cantidad en user_balance (upsert sumando)
                creditUserBalance(uid, amount)

                load()
                loadUserBalance()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    // Función auxiliar: suma `amount` al saldo en user_balance haciendo upsert
    private suspend fun creditUserBalance(uid: String, amount: Double) {
        // Intentamos leer el saldo actual primero
        val rows = Supabase.client.postgrest["user_balance"]
            .select { filter { eq("user_id", uid) } }
            .decodeList<UserBalanceRow>()

        val currentBalance = rows.firstOrNull()?.balance ?: 0.0
        val newBalance = currentBalance + amount

        // Upsert: si existe actualiza, si no existe inserta
        Supabase.client.postgrest["user_balance"].upsert(
            buildJsonObject {
                put("user_id", uid)
                put("balance", newBalance)
            }
        )
    }
}

// ── Screen ─────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodsScreen(navController: NavController, vm: PaymentViewModel = viewModel()) {
    val state    by vm.state.collectAsState()
    var showAdd  by remember { mutableStateOf(false) }
    var topUpFor by remember { mutableStateOf<PaymentMethod?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Métodos de pago") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Default.Add, "Añadir método")
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.error != null -> Text(
                    state.error!!, Modifier.align(Alignment.Center).padding(24.dp),
                    color = MaterialTheme.colorScheme.error
                )
                else -> LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tarjeta de saldo total real en la parte superior
                    item {
                        TotalBalanceCard(balance = state.userBalance)
                    }

                    if (state.methods.isEmpty()) {
                        item {
                            Column(
                                Modifier.fillMaxWidth().padding(top = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.CreditCard, null, Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.outline)
                                Text("No tienes métodos de pago", color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    } else {
                        items(state.methods, key = { it.id }) { method ->
                            PaymentCard(
                                method   = method,
                                onDelete = { vm.delete(method.id) },
                                onTopUp  = { topUpFor = method }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        AddPaymentDialog(
            onDismiss = { showAdd = false },
            onAdd     = { type, label, balance ->
                vm.add(type, label, balance)
                showAdd = false
            }
        )
    }

    topUpFor?.let { method ->
        TopUpDialog(
            method    = method,
            onDismiss = { topUpFor = null },
            onTopUp   = { amount ->
                vm.addBalance(method.id, method.balance, amount)
                topUpFor = null
            }
        )
    }
}

// ── Tarjeta de saldo total ─────────────────────────────────────────────────
@Composable
private fun TotalBalanceCard(balance: Double) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "Saldo SmartPark",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "%.2f €".format(balance),
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (balance >= 0) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.error
                )
            }
            Icon(
                Icons.Default.AccountBalanceWallet,
                null,
                Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

// ── Tarjeta de método de pago ──────────────────────────────────────────────
@Composable
private fun PaymentCard(
    method: PaymentMethod,
    onDelete: () -> Unit,
    onTopUp: () -> Unit
) {
    val icon = when (method.methodType) {
        "paypal" -> Icons.Default.AccountBalanceWallet
        "wallet" -> Icons.Default.Wallet
        else     -> Icons.Default.CreditCard
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(method.label, style = MaterialTheme.typography.titleSmall)
                    Text(
                        when (method.methodType) {
                            "paypal" -> "PayPal"
                            "wallet" -> "Monedero"
                            else     -> "Tarjeta"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                if (method.isDefault) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Principal", style = MaterialTheme.typography.labelSmall) }
                    )
                    Spacer(Modifier.width(4.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = onTopUp) {
                    Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Recargar saldo")
                }
            }
        }
    }
}

// ── Diálogo añadir método ──────────────────────────────────────────────────
@Composable
private fun AddPaymentDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, Double) -> Unit
) {
    var type    by remember { mutableStateOf("card") }
    var label   by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("0") }

    val types = listOf("card" to "Tarjeta", "paypal" to "PayPal", "wallet" to "Monedero")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir método de pago") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Tipo", style = MaterialTheme.typography.labelMedium)
                types.forEach { (value, lbl) ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = type == value, onClick = { type = value })
                        Text(lbl)
                    }
                }
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text(if (type == "card") "Alias (ej: Visa ****1234)" else "Email / alias") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = balance,
                    onValueChange = { balance = it },
                    label = { Text("Saldo inicial (€)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val b = balance.toDoubleOrNull() ?: 0.0
                    if (label.isNotBlank()) onAdd(type, label, b)
                },
                enabled = label.isNotBlank()
            ) { Text("Añadir") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

// ── Diálogo recargar saldo ─────────────────────────────────────────────────
@Composable
private fun TopUpDialog(
    method: PaymentMethod,
    onDismiss: () -> Unit,
    onTopUp: (Double) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    val presets = listOf(5.0, 10.0, 20.0, 50.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Recargar saldo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Método: ${method.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    presets.forEach { preset ->
                        SuggestionChip(
                            onClick = { amount = preset.toInt().toString() },
                            label = { Text("+${preset.toInt()}€") }
                        )
                    }
                }
                OutlinedTextField(
                    value = amount,
                    onValueChange = { v -> if (v.all { it.isDigit() || it == '.' }) amount = v },
                    label = { Text("Cantidad (€)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Euro, null) }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { amount.toDoubleOrNull()?.let { if (it > 0) onTopUp(it) } },
                enabled = (amount.toDoubleOrNull() ?: 0.0) > 0.0
            ) { Text("Recargar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}