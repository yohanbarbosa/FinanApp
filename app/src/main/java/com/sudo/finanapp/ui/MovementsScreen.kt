package com.sudo.finanapp.ui.MovementsScreeen

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuadre.app.data.*
import com.sudo.finanapp.ui.theme.Cuadre
import com.sudo.finanapp.ui.components.MovementList
import com.sudo.finanapp.ui.components.Muted
import com.sudo.finanapp.ui.components.Title
import java.time.YearMonth

// Devuelve solo los movimientos que cumplen todos los filtros activos:
// mes seleccionado, tipo (ingreso/gasto), categoría y texto de búsqueda.
// Si un filtro es null (o el texto está vacío) ese filtro no se aplica.
private fun filtrar(
    items: List<Movement>,
    mes: YearMonth,
    tipo: Kind?,
    categoria: Category?,
    texto: String,
): List<Movement> {
    return items.filter { m ->
        val delMes = YearMonth.from(m.date) == mes
        val delTipo = tipo == null || m.kind == tipo
        val deCategoria = categoria == null || m.category == categoria
        // el texto se busca en el nombre y también en el nombre de la categoría
        val coincideTexto = texto.isBlank() ||
                m.title.contains(texto, ignoreCase = true) ||
                m.category.label.contains(texto, ignoreCase = true)

        delMes && delTipo && deCategoria && coincideTexto
    }
}

// Texto que se muestra arriba como mes actual de la lista.
// Solo agrega el año cuando no es el año en curso, para no repetirlo siempre.
private fun etiquetaMes(mes: YearMonth): String {
    val anioActual = YearMonth.now().year
    return if (mes.year == anioActual) monthName(mes) else "${monthName(mes)} ${mes.year}"
}

// Pantalla de Movimientos (RF03): lista agrupada por día con el neto de cada
// jornada, búsqueda, filtros por tipo y categoría, y navegación entre meses.
// Al tocar un movimiento se llama a onOpen para que MainActivity muestre el detalle.
@Composable
fun MovementsScreen(items: List<Movement>, onOpen: (Movement) -> Unit) {
    val c = Cuadre.colors

    // estado de los filtros
    var q by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf<Kind?>(null) }
    var cat by remember { mutableStateOf<Category?>(null) }
    var mes by remember { mutableStateOf(YearMonth.now()) }
    var menu by remember { mutableStateOf(false) }

    val hayFiltros = kind != null || cat != null || q.isNotBlank()

    // vuelve tipo, categoría y búsqueda a su valor inicial (el mes no se toca)
    fun limpiarFiltros() {
        q = ""
        kind = null
        cat = null
    }

    val shown = filtrar(items, mes, kind, cat, q)
    // agrupa por fecha y ordena de la más reciente a la más antigua
    val groups = shown.groupBy { it.date }.toSortedMap(compareByDescending { it })

    Column(
        Modifier.fillMaxSize().background(c.bg).verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 22.dp, bottom = 100.dp),
    ) {
        // título y selector de mes
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) { Title("Movimientos") }
            TextButton(onClick = { mes = mes.minusMonths(1) }) { Text("‹") }
            Muted(etiquetaMes(mes))
            // no se deja avanzar más allá del mes actual
            TextButton(
                onClick = { mes = mes.plusMonths(1) },
                enabled = mes < YearMonth.now(),
            ) { Text("›") }
        }

        // buscador con botón para borrar el texto
        OutlinedTextField(
            value = q, onValueChange = { q = it }, singleLine = true,
            placeholder = { Text("Buscar por nombre o categoría") },
            leadingIcon = { Icon(Icons.Outlined.Search, null) },
            trailingIcon = {
                if (q.isNotEmpty()) {
                    IconButton(onClick = { q = "" }) {
                        Icon(Icons.Outlined.Close, "Borrar búsqueda")
                    }
                }
            },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        )

        // chips de filtro: tipo y categoría
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(kind == null && cat == null, { kind = null; cat = null }, { Text("Todos") })
            FilterChip(kind == Kind.INCOME, { kind = if (kind == Kind.INCOME) null else Kind.INCOME }, { Text("Ingresos") })
            FilterChip(kind == Kind.EXPENSE, { kind = if (kind == Kind.EXPENSE) null else Kind.EXPENSE }, { Text("Gastos") })
            Box {
                FilterChip(cat != null, { menu = true }, { Text(cat?.label ?: "Categoría") })
                DropdownMenu(menu, { menu = false }) {
                    DropdownMenuItem({ Text("Todas") }, { cat = null; menu = false })
                    Category.entries.forEach { k ->
                        DropdownMenuItem({ Text(k.label) }, { cat = k; menu = false })
                    }
                }
            }
        }

        // solo aparece si hay algo que limpiar
        if (hayFiltros) {
            TextButton(onClick = { limpiarFiltros() }) { Text("Limpiar filtros") }
        }

        // mensaje cuando no hay resultados
        if (groups.isEmpty()) {
            val mensaje = if (hayFiltros) {
                "No hay movimientos con este filtro. Prueba con otra categoría o borra la búsqueda."
            } else {
                "No hay movimientos en este mes."
            }
            Muted(mensaje, Modifier.padding(top = 24.dp))
        }

        // un bloque por día: encabezado con el neto del día y luego sus movimientos
        groups.forEach { (date, list) ->
            Row(
                Modifier.fillMaxWidth().padding(start = 2.dp, end = 2.dp, top = 16.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(dayLabel(date), color = c.tx2, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(money(list.sumOf { it.signed }, true), color = c.tx2, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            MovementList(list.sortedByDescending { it.id }, onOpen)
        }
    }
}