package com.sudo.finanapp.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuadre.app.data.*
import com.sudo.finanapp.ui.theme.Cuadre

@Composable
fun CuadreCard(modifier: Modifier = Modifier, radius: Dp = 16.dp, content: @Composable () -> Unit) {
    val c = Cuadre.colors
    Surface(
        modifier = modifier, shape = RoundedCornerShape(radius), color = c.surface,
        border = BorderStroke(1.dp, c.bd), content = content,
    )
}

@Composable
fun Title(text: String) =
    Text(text, color = Cuadre.colors.tx, fontSize = 25.sp, fontWeight = FontWeight.Bold)

@Composable
fun Muted(text: String, modifier: Modifier = Modifier) =
    Text(text, modifier, color = Cuadre.colors.tx2, fontSize = 13.sp)

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) =
    Text(text, modifier, color = Cuadre.colors.tx, fontSize = 18.sp, fontWeight = FontWeight.Bold)

@Composable
fun CategoryIcon(cat: Category, size: Dp = 40.dp) {
    Box(
        Modifier.size(size).clip(RoundedCornerShape(size * 0.35f)).background(cat.color),
        contentAlignment = Alignment.Center,
    ) { Icon(cat.icon, contentDescription = cat.label, tint = Color.White, modifier = Modifier.size(size * 0.5f)) }
}

@Composable
fun MovementRow(m: Movement, onClick: () -> Unit) {
    val c = Cuadre.colors
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CategoryIcon(m.category)
        Column(Modifier.weight(1f)) {
            Text(m.title, color = c.tx, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Muted(if (m.kind == Kind.INCOME) "Ingreso · ${m.category.label}" else m.category.label)
        }
        Text(
            money(m.signed, sign = true), fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
            color = if (m.kind == Kind.INCOME) c.inc else c.tx,
        )
    }
}

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

@Composable
fun Segmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val c = Cuadre.colors
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(c.sv).padding(4.dp)) {
        options.forEachIndexed { i, label ->
            val on = i == selected
            Box(
                Modifier.weight(1f).heightIn(min = 44.dp).clip(RoundedCornerShape(12.dp))
                    .background(if (on) c.surface else Color.Transparent).clickable { onSelect(i) },
                contentAlignment = Alignment.Center,
            ) { Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = if (on) c.tx else c.tx2) }
        }
    }
}
