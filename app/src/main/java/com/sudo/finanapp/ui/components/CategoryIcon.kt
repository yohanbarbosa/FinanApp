package com.sudo.finanapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cuadre.app.data.Category

@Composable
fun CategoryIcon(cat: Category, size: Dp = 40.dp) {
    Box(
        Modifier.size(size).clip(RoundedCornerShape(size * 0.35f)).background(cat.color),
        contentAlignment = Alignment.Center,
    ) { Icon(cat.icon, contentDescription = cat.label, tint = Color.White, modifier = Modifier.size(size * 0.5f)) }
}