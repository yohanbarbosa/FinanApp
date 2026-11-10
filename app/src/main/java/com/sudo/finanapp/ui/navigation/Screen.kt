package com.sudo.finanapp.ui.navigation


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Inicio", Icons.Outlined.Home)
    object Movements : Screen("movements", "Movimientos", Icons.Outlined.List)

    object Analysis : Screen("Analysis", "Analisis", Icons.Outlined.BarChart)

    object Register : Screen("register/{id}", "Registrar", Icons.Outlined.AddCircleOutline) {
        fun createRoute(id: Long = 0L) = "register/$id"
    }
    object Settings : Screen("settings", "Ajustes", Icons.Outlined.Settings)
}