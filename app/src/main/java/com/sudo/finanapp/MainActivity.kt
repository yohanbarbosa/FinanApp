package com.sudo.finanapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cuadre.app.data.Movement
import com.cuadre.app.data.MovementsViewModel
import com.cuadre.app.data.seedMovements
import com.sudo.finanapp.ui.AnalysisScreen.AnalysisScreen
import com.sudo.finanapp.ui.HomeScreen.HomeScreen
import com.sudo.finanapp.ui.MovementsScreeen.MovementsScreen
import com.sudo.finanapp.ui.Register.RegisterScreen
import com.sudo.finanapp.ui.components.BottomNavigationBar
import com.sudo.finanapp.ui.navigation.Screen
import com.sudo.finanapp.ui.theme.Cuadre
import com.sudo.finanapp.ui.theme.CuadreTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.cuadre.app.data.Kind
import com.cuadre.app.data.dayLabel
import com.cuadre.app.data.money
import com.sudo.finanapp.ui.Muted

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CuadreTheme{FinanApp()}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanApp(vm: MovementsViewModel = viewModel()){
    val c = Cuadre.colors
    val navController = rememberNavController()
    val items by vm.items.collectAsState()
    val route = navController.currentBackStackEntryAsState().value?.destination?.route

    var selected by remember { mutableStateOf<Movement?>(null) }
    var toDelete by remember { mutableStateOf<Movement?>(null) }

    Scaffold(
        containerColor = c.bg,
        bottomBar = {
            if (route != Screen.Register.route) BottomNavigationBar(navController = navController)
        },
        floatingActionButton = {
            if (route == Screen.Home.route) ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.Register.createRoute()) },
                icon = { Icon(Icons.Outlined.Add, null) },
                text = { Text("Registrar") },
                containerColor = c.brand, contentColor = c.onBrand,
            )
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    items = items,
                    onSeeAll = { navController.navigate(Screen.Movements.route) },
                    onOpen = { selected = it },
                )
            }
            composable(Screen.Movements.route) {
                MovementsScreen(items = items, onOpen = { selected = it })
            }
            composable(Screen.Analysis.route) {
                AnalysisScreen(items = items)
            }
            composable(
                Screen.Register.route,
                arguments = listOf(navArgument("id") { type = NavType.LongType }),
            ) { entry ->
                val id = entry.arguments?.getLong("id") ?: 0L
                RegisterScreen(
                    initial = vm.get(id),                       // null → crear, Movement → editar
                    onSave = { vm.save(it); navController.popBackStack() },
                    onClose = { navController.popBackStack() },
                )
            }
            composable(Screen.Settings.route) { DummyScreen("Pantalla de Ajustes") }
        }
    }

    // Detalle: Editar / Eliminar
    selected?.let { m ->
        ModalBottomSheet (onDismissRequest = { selected = null }, containerColor = c.bg) {
            Column (Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp).navigationBarsPadding()) {
                Text(m.title, color = c.tx, style = MaterialTheme.typography.titleMedium)
                Text(
                    money(m.signed, sign = true),
                    color = if (m.kind == Kind.INCOME) c.inc else c.exp,
                    style = MaterialTheme.typography.headlineMedium,
                )
                Muted("${m.category.label} · ${dayLabel(m.date)}", Modifier.padding(top = 4.dp))
                Button (
                    onClick = { navController.navigate(Screen.Register.createRoute(m.id)); selected = null },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                ) { Text("Editar") }
                OutlinedButton (
                    onClick = { toDelete = m; selected = null },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) { Text("Eliminar", color = c.exp) }
            }
        }
    }

    toDelete?.let { m ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("¿Eliminar movimiento?") },
            text = { Text("Se eliminará «${m.title}» y el balance se recalculará.") },
            confirmButton = { TextButton({ vm.delete(m.id); toDelete = null }) { Text("Eliminar", color = c.exp) } },
            dismissButton = { TextButton({ toDelete = null }) { Text("Cancelar") } },
        )
    }
}

@Composable
fun DummyScreen(title: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
    }
}

