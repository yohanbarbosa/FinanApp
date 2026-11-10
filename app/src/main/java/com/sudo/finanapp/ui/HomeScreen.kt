package com.sudo.finanapp.ui.HomeScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuadre.app.data.*
import com.sudo.finanapp.ui.MovementList
import com.sudo.finanapp.ui.CuadreCard
import com.sudo.finanapp.ui.SectionTitle
import com.sudo.finanapp.ui.Muted
import com.sudo.finanapp.ui.Title
import com.sudo.finanapp.ui.theme.Cuadre
import java.time.YearMonth
import kotlin.math.roundToInt

private const val FOOD_BUDGET = 500_000L

@Composable
fun HomeScreen(items: List<Movement>, onSeeAll: () -> Unit, onOpen: (Movement) -> Unit) {
    val c = Cuadre.colors
    var month by remember { mutableStateOf(YearMonth.now()) }
    val cur = items.inMonth(month)
    val prev = items.inMonth(month.minusMonths(1))
    val inc = cur.income
    val exp = cur.expense
    val food = cur.filter { it.category == Category.ALIMENTACION }.sumOf { it.amount }
    val recent = items.sortedWith(compareByDescending<Movement> { it.date }.thenByDescending { it.id }).take(4)

    Column(
        Modifier.fillMaxSize().background(c.bg).verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 22.dp, bottom = 100.dp),
    ) {
        // Encabezado + selector de mes
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Muted("Hola, yohan"); Title("Tu balance") }
            IconButton({ month = month.minusMonths(1) }) { Icon(Icons.Outlined.ChevronLeft, "Mes anterior", tint = c.tx2) }
            Text(monthName(month), color = c.tx2, fontWeight = FontWeight.SemiBold)
            IconButton({ month = month.plusMonths(1) }) { Icon(Icons.Outlined.ChevronRight, "Mes siguiente", tint = c.tx2) }
        }
        Spacer(Modifier.height(14.dp))

        // RF01 + RF04: balance, ingresos y gastos
        CuadreCard(radius = 26.dp) {
            Column(Modifier.padding(18.dp)) {
                Muted("Te queda este mes")
                Text(money(inc - exp), color = c.tx, fontSize = 42.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                FlowBar(inc, exp)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column { Muted("Ingresos"); Text(money(inc, true), color = c.inc, fontWeight = FontWeight.SemiBold, fontSize = 17.sp) }
                    Column(horizontalAlignment = Alignment.End) { Muted("Gastos"); Text(money(-exp, true), color = c.exp, fontWeight = FontWeight.SemiBold, fontSize = 17.sp) }
                }
                val prevExp = prev.expense
                if (prevExp > 0) {
                    val d = ((exp - prevExp) * 100.0 / prevExp).roundToInt()
                    HorizontalDivider(Modifier.padding(top = 12.dp), color = c.bd)
                    Muted(
                        when {
                            d < 0 -> "Gastaste ${-d} % menos que en ${monthName(month.minusMonths(1))}."
                            d > 0 -> "Gastaste $d % más que en ${monthName(month.minusMonths(1))}."
                            else -> "Gastaste lo mismo que en ${monthName(month.minusMonths(1))}."
                        }, Modifier.padding(top = 10.dp),
                    )
                }
            }
        }

        // Presupuesto de alimentación
        Spacer(Modifier.height(12.dp))
        CuadreCard {
            Column(Modifier.padding(14.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Alimentación", color = c.tx, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("${money(food)} de ${money(FOOD_BUDGET)}", color = c.tx, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                LinearProgressIndicator(
                    progress = { (food.toFloat() / FOOD_BUDGET).coerceIn(0f, 1f) },
                    modifier = Modifier.padding(vertical = 10.dp).fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = c.warn, trackColor = c.sv, drawStopIndicator = {},
                )
                Muted(
                    if (food <= FOOD_BUDGET) "Te quedan ${money(FOOD_BUDGET - food)} para el resto del mes."
                    else "Te pasaste ${money(food - FOOD_BUDGET)} del presupuesto.",
                )
            }
        }

        // Últimos movimientos
        Row(Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 8.dp), verticalAlignment = Alignment.Bottom) {
            SectionTitle("Últimos movimientos", Modifier.weight(1f))
            Text("Ver todos", color = c.brand, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                modifier = Modifier.clickable(onClick = onSeeAll).padding(4.dp))
        }
        if (recent.isEmpty()) Muted("Aún no hay movimientos. Toca Registrar para añadir el primero.")
        else MovementList (recent, onOpen)
    }
}

@Composable
private fun FlowBar(inc: Long, exp: Long) {
    val c = Cuadre.colors
    val total = inc + exp
    Row(
        Modifier.fillMaxWidth().height(16.dp).semantics { contentDescription = "Ingresos ${money(inc)}, gastos ${money(exp)}" },
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        if (total == 0L) Box(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(8.dp)).background(c.sv))
        else {
            if (inc > 0) Box(Modifier.weight(inc.toFloat()).fillMaxHeight()
                .clip(RoundedCornerShape(8.dp, 3.dp, 3.dp, 8.dp)).background(c.inc))
            if (exp > 0) Box(Modifier.weight(exp.toFloat()).fillMaxHeight()
                .clip(RoundedCornerShape(3.dp, 8.dp, 8.dp, 3.dp)).background(c.exp))
        }
    }
}
