package com.cuadre.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuadre.app.data.*
import com.cuadre.app.ui.theme.Cuadre
import java.time.YearMonth

@Composable
fun MovementsScreen(items: List<Movement>, onOpen: (Movement) -> Unit) {
    val c = Cuadre.colors
    var q by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf<Kind?>(null) }
    var cat by remember { mutableStateOf<Category?>(null) }
    var menu by remember { mutableStateOf(false) }

    // RF03: filtros por tipo, categoría y texto
    val shown = items.filter {
        (kind == null || it.kind == kind) && (cat == null || it.category == cat) &&
            (q.isBlank() || it.title.contains(q, ignoreCase = true))
    }
    val groups = shown.groupBy { it.date }.toSortedMap(compareByDescending { it })

    Column(
        Modifier.fillMaxSize().background(c.bg).verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 22.dp, bottom = 100.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) { Title("Movimientos") }
            Muted(monthName(YearMonth.now()))
        }
        OutlinedTextField(
            value = q, onValueChange = { q = it }, singleLine = true,
            placeholder = { Text("Buscar por nombre o nota") },
            leadingIcon = { Icon(Icons.Outlined.Search, null) },
            shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        )
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(kind == null && cat == null, { kind = null; cat = null }, { Text("Todos") })
            FilterChip(kind == Kind.INCOME, { kind = if (kind == Kind.INCOME) null else Kind.INCOME }, { Text("Ingresos") })
            FilterChip(kind == Kind.EXPENSE, { kind = if (kind == Kind.EXPENSE) null else Kind.EXPENSE }, { Text("Gastos") })
            Box {
                FilterChip(cat != null, { menu = true }, { Text(cat?.label ?: "Categoría") })
                DropdownMenu(menu, { menu = false }) {
                    DropdownMenuItem({ Text("Todas") }, { cat = null; menu = false })
                    Category.values().forEach { k -> DropdownMenuItem({ Text(k.label) }, { cat = k; menu = false }) }
                }
            }
        }
        if (groups.isEmpty()) {
            Muted("No hay movimientos con este filtro. Prueba con otra categoría o borra la búsqueda.", Modifier.padding(top = 24.dp))
        }
        groups.forEach { (date, list) ->
            Row(Modifier.fillMaxWidth().padding(start = 2.dp, end = 2.dp, top = 16.dp, bottom = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(dayLabel(date), color = c.tx2, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(money(list.sumOf { it.signed }, true), color = c.tx2, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            MovementList(list.sortedByDescending { it.id }, onOpen)
        }
    }
}
