package com.sudo.finanapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// ==========================================
// 1. MODELO DE DATOS Y ESTRUCTURA JSON
// ==========================================

enum class TransactionType {
    EXPENSE, INCOME
}

data class Transaction(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: TransactionType,
    val amount: Double,
    val category: String,
    val date: String,
    val account: String,
    val note: String
) {
    // Serialización a formato JSONObject para preparación con Room o Firebase
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("type", type.name)
            put("amount", amount)
            put("category", category)
            put("date", date)
            put("account", account)
            put("note", note)
        }
    }

    companion object { // helper estático para listas
        fun listToJson(transactions: List<Transaction>): JSONArray {
            val jsonArray = JSONArray()
            transactions.forEach { jsonArray.put(it.toJson()) }
            return jsonArray
        }
    }
}

// Categorías predefinidas con sus Emojis representativos
data class CategoryUi(val name: String, val emoji: String)

val defaultCategories = listOf(
    CategoryUi("Alimentación", "🍔"),
    CategoryUi("Transporte", "🚕"),
    CategoryUi("Vivienda", "🏠"),
    CategoryUi("Salud", "⚕️"),
    CategoryUi("Ocio", "🎮"),
    CategoryUi("Servicios", "💡"),
    CategoryUi("Educación", "📚"),
    CategoryUi("Otros", "📦")
)

// ==========================================
// 2. ACTIVIDAD PRINCIPAL (PUNTO DE ENTRADA)
// ==========================================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF1E293B),
                    secondary = Color(0xFF3B82F6),
                    background = Color(0xFFF8FAFC)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RegisterScreenContainer()
                }
            }
        }
    }
}

// ==========================================
// 3. CONTENEDOR Y GESTIÓN DE ESTADO EN MEMORIA
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreenContainer() {
    // Lista reactiva temporal en memoria para almacenar las transacciones
    val transactionList = remember { mutableStateListOf<Transaction>() }
    var showSuccessMessage by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { /* Acción de cerrar o volver */ }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            RegisterScreen(
                onSaveTransaction = { newTransaction ->
                    transactionList.add(newTransaction)
                    showSuccessMessage = true

                    // Ejemplo de exportación JSON en consola (Simulando persistencia/API)
                    val jsonExport = Transaction.listToJson(transactionList)
                    println("DEBUG - Transacciones en JSON: $jsonExport")
                }
            )

            // Snackbar o aviso visual rápido de éxito
            if (showSuccessMessage) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { showSuccessMessage = false }) {
                            Text("OK", color = Color.White)
                        }
                    }
                ) {
                    Text("¡Transacción guardada exitosamente!")
                }
            }
        }
    }
}

// ==========================================
// 4. PANTALLA DE REGISTRO COMPLETA
// ==========================================

@Composable
fun RegisterScreen(onSaveTransaction: (Transaction) -> Unit) {
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amountInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Alimentación") }
    var noteInput by remember { mutableStateOf("") }

    // Valores por defecto estéticos solicitados
    val currentDateFormatted = remember {
        val formatter = DateTimeFormatter.ofPattern("d 'de' MMMM", Locale("es", "ES"))
        "Hoy, " + LocalDate.now().format(formatter)
    }
    var accountInput by remember { mutableStateOf("Ahorros Bancolombia") }

    // Colores dinámicos según el tipo (Rojo para Gasto, Verde para Ingreso)
    val themeColor by animateColorAsState(
        targetValue = if (selectedType == TransactionType.EXPENSE) Color(0xFFEF4444) else Color(0xFF10B981),
        label = "ThemeColorChange"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Selector de tipo (Segmented Button personalizado)
            SegmentedButtonTypeSelector(
                selectedType = selectedType,
                onTypeSelected = { selectedType = it }
            )

            // 2. Display de Monto Gigante con signo dinámico
            GiantAmountDisplay(
                amount = amountInput,
                onAmountChange = { amountInput = it },
                themeColor = themeColor,
                isExpense = selectedType == TransactionType.EXPENSE
            )

            // 3. Cuadrícula de Categorías (Grid de 4 columnas)
            Text(
                text = "Categoría",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.Gray
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(150.dp)
            ) {
                items(defaultCategories) { category ->
                    CategoryItem(
                        category = category,
                        isSelected = selectedCategory == category.name,
                        activeColor = themeColor,
                        onSelect = { selectedCategory = category.name }
                    )
                }
            }

            // 4. Campos de Detalle del Formulario (Filas estéticas)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    DetailRow(
                        icon = Icons.Default.DateRange,
                        label = "Fecha",
                        value = currentDateFormatted,
                        onValueChange = {}
                    )
                    Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    DetailRow(
                        icon = Icons.Default.Home,
                        label = "Cuenta",
                        value = accountInput,
                        onValueChange = { accountInput = it }
                    )
                    Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    DetailRow(
                        icon = Icons.Default.Edit,
                        label = "Nota",
                        value = noteInput,
                        placeholder = "Ej. Almuerzo con Laura",
                        onValueChange = { noteInput = it }
                    )
                }
            }
        }

        // 5. Botón de Acción Principal (CTA Dinámico)
        val parsedAmount = amountInput.toDoubleOrNull() ?: 0.0
        val isFormValid = parsedAmount > 0.0

        Button(
            onClick = {
                if (isFormValid) {
                    val transaction = Transaction(
                        type = selectedType,
                        amount = parsedAmount,
                        category = selectedCategory,
                        date = currentDateFormatted,
                        account = accountInput,
                        note = if (noteInput.isBlank()) selectedCategory else noteInput
                    )
                    onSaveTransaction(transaction)
                    // Limpieza tras guardar
                    amountInput = ""
                    noteInput = ""
                }
            },
            enabled = isFormValid,
            colors = ButtonDefaults.buttonColors(
                containerColor = themeColor,
                disabledContainerColor = Color.LightGray
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = if (selectedType == TransactionType.EXPENSE) "Guardar gasto" else "Guardar ingreso",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// ==========================================
// 5. COMPONENTES VISUALES AUXILIARES
// ==========================================

@Composable
fun SegmentedButtonTypeSelector(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFE2E8F0))
            .padding(4.dp)
    ) {
        val isExpense = selectedType == TransactionType.EXPENSE

        // Botón Gasto
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(20.dp))
                .background(if (isExpense) Color(0xFFEF4444) else Color.Transparent)
                .clickable { onTypeSelected(TransactionType.EXPENSE) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Gasto",
                color = if (isExpense) Color.White else Color.DarkGray,
                fontWeight = FontWeight.Bold
            )
        }

        // Botón Ingreso
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(20.dp))
                .background(if (!isExpense) Color(0xFF10B981) else Color.Transparent)
                .clickable { onTypeSelected(TransactionType.INCOME) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Ingreso",
                color = if (!isExpense) Color.White else Color.DarkGray,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun GiantAmountDisplay(
    amount: String,
    onAmountChange: (String) -> Unit,
    themeColor: Color,
    isExpense: Boolean
) {
    val sign = if (isExpense) "−" else "+"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$sign \$",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = themeColor
            )
            Spacer(modifier = Modifier.width(4.dp))

            // Campo numérico transparente integrado
            OutlinedTextField(
                value = amount,
                onValueChange = { newValue ->
                    // Filtrar solo dígitos válidos para moneda
                    if (newValue.all { it.isDigit() } && newValue.length <= 10) {
                        onAmountChange(newValue)
                    }
                },
                placeholder = {
                    Text("0", fontSize = 36.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                ),
                modifier = Modifier.widthIn(min = 60.dp, max = 220.dp)
            )
        }
    }
}

@Composable
fun CategoryItem(
    category: CategoryUi,
    isSelected: Boolean,
    activeColor: Color,
    onSelect: () -> Unit
) {
    val borderColor = if (isSelected) activeColor else Color.Transparent
    val backgroundColor = if (isSelected) activeColor.copy(alpha = 0.1f) else Color.White

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onSelect() }
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(Color(0xFFF1F5F9), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = category.emoji, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = category.name,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) activeColor else Color.DarkGray,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    placeholder: String = "",
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.width(80.dp)
        )

        // Campo editable minimalista
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.LightGray) },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1E293B),
                textAlign = TextAlign.End
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
