package com.sudo.finanapp.ui.AnalysisScreen


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuadre.app.data.ES
import com.cuadre.app.data.Movement
import com.cuadre.app.data.expense
import com.cuadre.app.data.expenseByCategory
import com.cuadre.app.data.inMonth
import com.cuadre.app.data.income
import com.cuadre.app.data.money
import com.cuadre.app.data.monthName
import com.sudo.finanapp.ui.theme.Cuadre
import com.sudo.finanapp.ui.components.CuadreCard
import com.sudo.finanapp.ui.components.Muted
import com.sudo.finanapp.ui.components.SectionTitle
import com.sudo.finanapp.ui.components.Segmented
import com.sudo.finanapp.ui.components.Title
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun AnalysisScreen(items: List<Movement>) {
    val c = Cuadre.colors
    var period by remember { mutableIntStateOf(1) } // 0 semana, 1 mes, 2 año
    val today = LocalDate.now()
    val start = when (period) { 0 -> today.minusDays(6); 1 -> today.withDayOfMonth(1); else -> today.withDayOfYear(1) }
    val inRange = items.filter { !it.date.isBefore(start) && !it.date.isAfter(today) }

    val byCat = inRange.expenseByCategory()                 // RF05: categorías con mayor gasto
    val totalExp = byCat.sumOf { it.second }
    val top = byCat.firstOrNull()

    Column(
        Modifier.fillMaxSize().background(c.bg).verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 22.dp, bottom = 100.dp),
    ) {
        Title("Análisis")
        Spacer(Modifier.height(14.dp))
        Segmented(listOf("Semana", "Mes", "Año"), period) { period = it }

        // Frase-resumen antes del gráfico
        Text(
            when {
                top == null -> "Aún no hay gastos en este periodo."
                top.second * 2 > totalExp -> "Más de la mitad de tus gastos van a ${top.first.label.lowercase()}."
                else -> "${top.first.label} es tu mayor gasto: ${(top.second * 100.0 / totalExp).roundToInt()} % del total."
            },
            color = c.tx, fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 25.sp,
            modifier = Modifier.padding(top = 16.dp, bottom = 14.dp),
        )

        // Resumen ingresos / gastos
        Row(Modifier.fillMaxWidth().padding(bottom = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SummaryCard("Ingresos", money(inRange.income), c.inc, Modifier.weight(1f))
            SummaryCard("Gastos", money(inRange.expense), c.exp, Modifier.weight(1f))
        }

        // Dona
        Box(Modifier.align(Alignment.CenterHorizontally).size(156.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val stroke = 26.dp.toPx()
                val tl = Offset(stroke / 2, stroke / 2)
                val sz = Size(size.width - stroke, size.height - stroke)
                drawArc(c.sv, 0f, 360f, false, tl, sz, style = Stroke(stroke))
                var a = -90f
                byCat.forEach { (k, v) ->
                    val sweep = 360f * v / totalExp
                    drawArc(k.color, a, (sweep - 1.5f).coerceAtLeast(0.5f), false, tl, sz, style = Stroke(stroke))
                    a += sweep
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(money(totalExp), color = c.tx, fontWeight = FontWeight.Bold, fontSize = 17.sp, textAlign = TextAlign.Center)
                Muted("gastado")
            }
        }
        Spacer(Modifier.height(10.dp))
        byCat.take(5).forEach { (k, v) ->
            Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(12.dp).clip(RoundedCornerShape(4.dp)).background(k.color))
                Text(k.label, Modifier.weight(1f), color = c.tx, fontSize = 14.sp)
                Text(money(v), color = c.tx, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text("${(v * 100.0 / totalExp).roundToInt()} %", Modifier.width(44.dp), color = c.tx2, fontSize = 14.sp, textAlign = TextAlign.End)
            }
        }

        // Barras: últimos 6 meses
        SectionTitle("Ingresos y gastos", Modifier.padding(top = 20.dp, bottom = 10.dp))
        val months = (5 downTo 0).map { YearMonth.now().minusMonths(it.toLong()) }
        val data = months.map { m -> items.inMonth(m).let { it.income to it.expense } }
        val mx = max(1L, data.maxOf { max(it.first, it.second) })
        Row(Modifier.fillMaxWidth().height(110.dp), verticalAlignment = Alignment.Bottom) {
            data.forEach { (i, g) ->
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.Bottom) {
                    Box(Modifier.width(11.dp).height((110f * i / mx).dp).clip(RoundedCornerShape(4.dp, 4.dp, 0.dp, 0.dp)).background(c.inc))
                    Box(Modifier.width(11.dp).height((110f * g / mx).dp).clip(RoundedCornerShape(4.dp, 4.dp, 0.dp, 0.dp)).background(c.exp))
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 6.dp)) {
            months.forEach { m ->
                Text(m.month.getDisplayName(TextStyle.SHORT, ES).replace(".", "").replaceFirstChar { it.uppercase() },
                    Modifier.weight(1f), color = c.tx2, fontSize = 11.sp, textAlign = TextAlign.Center)
            }
        }

        // Comparativo con el mes anterior
        val now = YearMonth.now()
        val curC = items.inMonth(now).expenseByCategory().toMap()
        val prevC = items.inMonth(now.minusMonths(1)).expenseByCategory().toMap()
        SectionTitle("Frente a ${monthName(now.minusMonths(1))}", Modifier.padding(top = 20.dp, bottom = 4.dp))
        curC.entries.sortedByDescending { it.value }.take(3).forEach { (k, v) ->
            val p = prevC[k] ?: 0L
            if (p > 0) {
                val d = ((v - p) * 100.0 / p).roundToInt()
                Row(Modifier.fillMaxWidth().padding(vertical = 9.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(k.label, color = c.tx, fontSize = 14.sp)
                    Text(if (d >= 0) "▲ $d %" else "▼ ${-d} %", color = if (d >= 0) c.exp else c.inc, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                HorizontalDivider(color = c.bd)
            }
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    CuadreCard (modifier) {
        Column(Modifier.padding(14.dp)) {
            Muted(label)
            Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 19.sp)
        }
    }
}
