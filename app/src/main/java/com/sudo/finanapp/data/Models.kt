package com.cuadre.app.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs

val ES: Locale = Locale("es", "CO")

enum class Kind { INCOME, EXPENSE }

enum class Category(val label: String, val color: Color, val icon: ImageVector, val kind: Kind) {
    ALIMENTACION("Alimentación", Color(0xFF0E7490), Icons.Outlined.Restaurant, Kind.EXPENSE),
    TRANSPORTE("Transporte", Color(0xFFB45309), Icons.Outlined.DirectionsBus, Kind.EXPENSE),
    VIVIENDA("Vivienda", Color(0xFF4338CA), Icons.Outlined.Home, Kind.EXPENSE),
    SALUD("Salud", Color(0xFFBE185D), Icons.Outlined.Favorite, Kind.EXPENSE),
    OCIO("Ocio", Color(0xFF7E22CE), Icons.Outlined.PlayArrow, Kind.EXPENSE),
    SERVICIOS("Servicios", Color(0xFF0369A1), Icons.Outlined.Bolt, Kind.EXPENSE),
    EDUCACION("Educación", Color(0xFF4D7C0F), Icons.Outlined.MenuBook, Kind.EXPENSE),
    OTROS("Otros", Color(0xFF64748B), Icons.Outlined.MoreHoriz, Kind.EXPENSE),
    SALARIO("Salario", Color(0xFF15803D), Icons.Outlined.ArrowDownward, Kind.INCOME),
    FREELANCE("Freelance", Color(0xFF15803D), Icons.Outlined.ArrowDownward, Kind.INCOME),
    OTROS_INGRESOS("Otros ingresos", Color(0xFF15803D), Icons.Outlined.ArrowDownward, Kind.INCOME);

    companion object {
        // Usamos entries en lugar de values()
        fun of(kind: Kind) = entries.filter { it.kind == kind }
    }
}

/** [amount] siempre es positivo; el signo lo da la categoría (ingreso/gasto). */
data class Movement(
    val id: Long,
    val title: String,
    val category: Category,
    val amount: Long,
    val date: LocalDate,
) {
    val kind: Kind get() = category.kind
    val signed: Long get() = if (kind == Kind.INCOME) amount else -amount
}

// ---------- Formato ----------
fun money(v: Long, sign: Boolean = false): String {
    val body = "%,d".format(Locale.US, abs(v)).replace(',', '.')
    val s = if (!sign) "" else if (v < 0) "−" else "+"
    return s + "\$" + body
}

private val DAY_LABEL_FORMATTER = DateTimeFormatter.ofPattern("EEE d MMM", ES)

fun dayLabel(d: LocalDate): String {
    val today = LocalDate.now()
    return when (d) {
        today -> "Hoy"
        today.minusDays(1) -> "Ayer"
        else -> d.format(DAY_LABEL_FORMATTER)
            .replace(".", "")
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(ES) else it.toString() }
    }
}

fun monthName(m: YearMonth): String = m.month.getDisplayName(TextStyle.FULL, ES)

// ---------- RF04: cálculos (siempre derivados de la lista) ----------
fun List<Movement>.inMonth(m: YearMonth) = filter { YearMonth.from(it.date) == m }
val List<Movement>.income: Long get() = filter { it.kind == Kind.INCOME }.sumOf { it.amount }
val List<Movement>.expense: Long get() = filter { it.kind == Kind.EXPENSE }.sumOf { it.amount }
fun List<Movement>.expenseByCategory(): List<Pair<Category, Long>> =
    filter { it.kind == Kind.EXPENSE }.groupBy { it.category }
        .map { it.key to it.value.sumOf { m -> m.amount } }.sortedByDescending { it.second }

// ---------- Datos de ejemplo ----------
fun seedMovements(): List<Movement> {
    val now = LocalDate.now()
    val first = now.withDayOfMonth(1)
    var id = 1L
    fun m(t: String, c: Category, a: Long, d: LocalDate) = Movement(id++, t, c, a, d)
    val list = mutableListOf(
        m("Supermercado Éxito", Category.ALIMENTACION, 148900, now),
        m("Transmilenio", Category.TRANSPORTE, 12000, now),
        m("Salario", Category.SALARIO, 3500000, first),
        m("Cine Colombia", Category.OCIO, 38000, now.minusDays(1)),
        m("Arriendo", Category.VIVIENDA, 650000, first),
        m("Factura de energía", Category.SERVICIOS, 96400, first),
        m("Mercado semanal", Category.ALIMENTACION, 106100, first),
    )
    for (i in 1..5) { // meses anteriores para gráficos y comparativos
        val d = now.minusMonths(i.toLong()).withDayOfMonth(5)
        list += m("Salario", Category.SALARIO, 3_200_000L + i * 60_000L, d)
        list += m("Arriendo", Category.VIVIENDA, 650000, d)
        list += m("Mercado", Category.ALIMENTACION, 190_000L + i * 14_000L, d)
        list += m("Transporte", Category.TRANSPORTE, 128_000L + i * 4_000L, d)
    }
    return list
}