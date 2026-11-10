package com.sudo.finanapp.ui.components


import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sudo.finanapp.ui.navigation.Screen

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        Screen.Home,
        Screen.Movements,
        Screen.Analysis,
        Screen.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            // Evita acumular una pila gigante de pantallas al presionar atrás
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Evita crear múltiples copias de la misma pantalla si se presiona varias veces
                            launchSingleTop = true
                            // Restaura el estado guardado al volver a seleccionar una pestaña
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}