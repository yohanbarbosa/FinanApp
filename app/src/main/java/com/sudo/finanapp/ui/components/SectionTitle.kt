package com.sudo.finanapp.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sudo.finanapp.ui.theme.Cuadre

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) =
    Text(text, modifier, color = Cuadre.colors.tx, fontSize = 18.sp, fontWeight = FontWeight.Bold)
