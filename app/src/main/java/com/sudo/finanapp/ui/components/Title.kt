package com.sudo.finanapp.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sudo.finanapp.ui.theme.Cuadre

@Composable
fun Title(text: String) =
    Text(text, color = Cuadre.colors.tx, fontSize = 25.sp, fontWeight = FontWeight.Bold)