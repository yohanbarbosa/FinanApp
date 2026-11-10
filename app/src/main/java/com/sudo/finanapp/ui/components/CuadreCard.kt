package com.sudo.finanapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sudo.finanapp.ui.theme.Cuadre

@Composable
fun CuadreCard(modifier: Modifier = Modifier, radius: Dp = 16.dp, content: @Composable () -> Unit) {
    val c = Cuadre.colors
    Surface(
        modifier = modifier, shape = RoundedCornerShape(radius), color = c.surface,
        border = BorderStroke(1.dp, c.bd), content = content,
    )
}