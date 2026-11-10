package com.sudo.finanapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import com.cuadre.app.data.Movement
import com.sudo.finanapp.ui.theme.Cuadre

@Composable
fun MovementList(items: List<Movement>, onOpen: (Movement) -> Unit) {
    CuadreCard {
        Column {
            items.forEachIndexed { i, m ->
                if (i > 0) HorizontalDivider(color = Cuadre.colors.bd)
                MovementRow(m) { onOpen(m) }
            }
        }
    }
}