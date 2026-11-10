package com.sudo.finanapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuadre.app.data.Kind
import com.cuadre.app.data.Movement
import com.cuadre.app.data.money
import com.sudo.finanapp.ui.theme.Cuadre

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
            Text(if (m.kind == Kind.INCOME) "Ingreso · ${m.category.label}" else m.category.label,
                color = c.tx,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis)

            Muted(m.title)
        }
        Text(
            money(m.signed, sign = true), fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
            color = if (m.kind == Kind.INCOME) c.inc else c.tx,
        )
    }
}
