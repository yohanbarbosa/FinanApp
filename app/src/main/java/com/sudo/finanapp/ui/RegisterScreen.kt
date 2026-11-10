package com.sudo.finanapp.ui.Register

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuadre.app.data.*
import com.sudo.finanapp.ui.theme.Cuadre
import com.sudo.finanapp.ui.CategoryIcon
import com.sudo.finanapp.ui.Muted
import com.sudo.finanapp.ui.Segmented
import com.sudo.finanapp.ui.Title
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    initial: Movement?,
    onSave: (Movement) -> Unit,
    onClose: () -> Unit
) {
    val c = Cuadre.colors
    var kind by remember { mutableStateOf(initial?.kind ?: Kind.EXPENSE) }
    var digits by remember { mutableStateOf(initial?.amount?.toString() ?: "") }
    var cat by remember { mutableStateOf(initial?.category ?: Category.ALIMENTACION) }
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var date by remember { mutableStateOf(initial?.date ?: LocalDate.now()) }
    var tried by remember { mutableStateOf(false) }   // se activa al pulsar Guardar
    var pickDate by remember { mutableStateOf(false) }
    val focus = remember { FocusRequester() }

    val amount = digits.toLongOrNull() ?: 0L
    val amountErr = tried && amount <= 0                // RF02: valor > 0
    val titleErr = tried && title.isBlank()             // RF02: descripción obligatoria
    val isIncome = kind == Kind.INCOME
    val accent = if (isIncome) c.inc else c.exp
    val caret by rememberInfiniteTransition(label = "caret").animateFloat(
        1f, 0f, infiniteRepeatable(tween(550), RepeatMode.Reverse), label = "a",
    )

    Column(
        Modifier
            .fillMaxSize()
            .background(c.bg)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 22.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) { Title(if (initial == null) "Registrar" else "Editar movimiento") }
            IconButton(onClose) { Icon(Icons.Outlined.Close, "Cerrar", tint = c.tx) }
        }
        Spacer(Modifier.height(14.dp))

        // RF02: diferenciar ingreso / gasto
        Segmented (listOf("Gasto", "Ingreso"), if (isIncome) 1 else 0) {
            kind = if (it == 1) Kind.INCOME else Kind.EXPENSE
            cat = Category.of(kind).first()
        }

        // Monto grande: campo invisible + texto formateado
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 22.dp)
                .clickable { focus.requestFocus() },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    (if (isIncome) "+" else "−") + money(amount),
                    color = if (amountErr) c.exp else accent,
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Bold,
                )
                Box(Modifier.padding(start = 3.dp).width(3.dp).height(42.dp).alpha(caret).background(c.brand))
            }
            Muted("Pesos colombianos")
            if (amountErr) Text("El valor debe ser mayor que cero.", color = c.exp, fontSize = 13.sp)
            BasicTextField(
                value = digits,
                onValueChange = { digits = it.filter(Char::isDigit).take(12) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.size(1.dp).alpha(0f).focusRequester(focus),
            )
        }
        LaunchedEffect(Unit) { if (initial == null) focus.requestFocus() }

        Text("Categoría", color = c.tx2, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        Category.of(kind).chunked(4).forEach { row ->
            Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { k ->
                    val on = k == cat
                    Column(
                        Modifier
                            .weight(1f)
                            .heightIn(min = 64.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (on) c.brandC else c.sv)
                            .clickable { cat = k }
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        CategoryIcon(k, 32.dp)
                        Text(k.label, color = c.tx, fontSize = 11.5.sp, maxLines = 1)
                    }
                }
                // Corregido: 4 - row.size
                repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }

        OutlinedTextField(
            value = title,
            onValueChange = { title = it.take(40) },
            label = { Text("Descripción") },
            singleLine = true,
            isError = titleErr,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            supportingText = { if (titleErr) Text("Escribe una descripción.") },
        )

        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(c.sv)
                .clickable { pickDate = true }
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Fecha", color = c.tx2, fontSize = 14.sp)
            Text(
                if (date == LocalDate.now()) "Hoy, " + date.format(DateTimeFormatter.ofPattern("d MMM", ES))
                else date.format(DateTimeFormatter.ofPattern("d MMM yyyy", ES)),
                color = c.tx, fontSize = 14.sp,
            )
        }

        Button(
            onClick = {
                tried = true
                if (amount > 0 && title.isNotBlank()) {
                    onSave(Movement(initial?.id ?: 0L, title.trim(), cat, amount, date))
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 18.dp).heightIn(min = 54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = c.brand, contentColor = c.onBrand),
        ) {
            Text(
                when {
                    initial != null -> "Guardar cambios"
                    isIncome -> "Guardar ingreso"
                    else -> "Guardar gasto"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }

    if (pickDate) {
        val st = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { pickDate = false },
            confirmButton = {
                TextButton(onClick = {
                    st.selectedDateMillis?.let { millis ->
                        date = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                    }
                    pickDate = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { pickDate = false }) { Text("Cancelar") }
            },
        ) { DatePicker(state = st) }
    }
}