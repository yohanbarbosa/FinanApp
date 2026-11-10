package com.sudo.finanapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cuadre.app.data.seedMovements
import com.cuadre.app.ui.AnalysisScreen
import com.cuadre.app.ui.HomeScreen
import com.cuadre.app.ui.MovementsScreen
import com.sudo.finanapp.feature.register.RegisterScreen
import com.sudo.finanapp.ui.components.BottomNavigationBar
import com.sudo.finanapp.ui.navigation.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                val navController = rememberNavController()

                Scaffold(
                    bottomBar = { BottomNavigationBar(navController = navController) }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        // Pantalla 1: Home
                        composable(Screen.Home.route) {
                            HomeScreen(
                                items = seedMovements(),
                                onSeeAll = { navController.navigate(Screen.Movements.route) },
                                onOpen = { /* Abrir detalle del movimiento */ }
                            )
                        }

                        // Pantalla 2: Movimientos (Ejemplo temporal)
                        composable(Screen.Movements.route) {
                            MovementsScreen(
                                items = seedMovements(),
                                onOpen = { /* Abrir detalle del movimiento */ }
                            )
                        }


                        // Pantalla 3: Movimientos (Ejemplo temporal)
                        composable(Screen.Analysis.route) {
                            AnalysisScreen(
                                items = seedMovements(),
                            )
                        }

                        // Pantalla 4: Movimientos (Ejemplo temporal)
                        composable(Screen.Register.route) {
                            RegisterScreen(
                                initial = null, // null para CREAR uno nuevo (o pasa un Movement para EDITAR)
                                onSave = { newMovement ->
                                    // 1. Guardas o actualizas la lista de movimientos aquí

                                    // 2. Vuelves a la pantalla anterior en la pila
                                    navController.popBackStack()
                                },
                                onClose = {
                                    // Vuelves a la pantalla anterior sin guardar cambios
                                    navController.popBackStack()
                                }
                            )
                        }

                        // Pantalla 3: Ajustes (Ejemplo temporal)
                        composable(Screen.Settings.route) {
                            DummyScreen("Pantalla de Ajustes")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DummyScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
    }
}