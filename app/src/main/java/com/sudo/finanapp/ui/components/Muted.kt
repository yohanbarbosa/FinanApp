package com.sudo.finanapp.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.sudo.finanapp.ui.theme.Cuadre

@Composable
fun Muted(text: String, modifier: Modifier = Modifier) =
    Text(text, modifier, color = Cuadre.colors.tx2, fontSize = 13.sp)