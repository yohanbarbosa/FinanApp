# FinanApp (Cuadre) — Finanzas personales para Android

Aplicación móvil nativa para **registrar, consultar y administrar ingresos y gastos personales**, ver el balance disponible y analizar el comportamiento de los movimientos. Funciona sin conexión: todos los datos se guardan localmente en el dispositivo.

## Descripción del proyecto

Cuadre responde a una pregunta simple: *¿cuánto me queda este mes y en qué se me va el dinero?* El balance se lee como una barra que enfrenta ingresos y gastos, y cada pantalla del análisis empieza con una frase que resume lo que está pasando.

### Requisitos funcionales cubiertos

| Código | Requisito | Dónde se implementa |
|---|---|---|
| RF01 | Panel principal: balance, totales de ingresos y gastos, últimos movimientos y acceso a las demás funciones | Pantalla **Inicio** |
| RF02 | Registro de ingresos y gastos con descripción, valor, categoría y fecha; validación de campos obligatorios y valor mayor que cero | Pantalla **Registrar** |
| RF03 | Listado, detalle, edición, eliminación y filtros por tipo o categoría | Pantalla **Movimientos** y hoja de detalle |
| RF04 | Cálculo automático de ingresos, gastos y balance tras registrar, editar o eliminar | Derivado de la lista de movimientos (ver [Persistencia](#persistencia-de-datos)) |
| RF05 | Resumen, categorías con mayor gasto y gráficos | Pantalla **Análisis** |

### Pantallas

- **Inicio:** balance del mes, barra de flujo ingresos/gastos, selector de mes, presupuesto de alimentación y últimos 4 movimientos.
- **Registrar:** selector Gasto/Ingreso, monto grande, categorías con ícono, descripción y fecha. Se reutiliza para editar.
- **Movimientos:** búsqueda por texto, filtros por tipo y categoría, lista agrupada por día con el neto de cada jornada.
- **Análisis:** periodo Semana/Mes/Año, gráfico de dona por categoría, barras de los últimos 6 meses y comparativo con el mes anterior.
- **Más:** pantalla reservada para ajustes (por implementar).

## Tecnologías utilizadas

| Tecnología | Uso |
|---|---|
| **Kotlin** | Lenguaje principal |
| **Jetpack Compose** | Interfaz declarativa, incluidos los gráficos dibujados con `Canvas` |
| **Material 3** | Componentes (`Scaffold`, `NavigationBar`, `ModalBottomSheet`, `DatePicker`...) |
| **Navigation Compose** | Navegación entre pantallas, con un argumento `id` para editar |
| **ViewModel + StateFlow** | Estado de la aplicación y comunicación con la base de datos |
| **Room (SQLite)** | Persistencia local de los movimientos |
| **KSP** | Procesador de anotaciones que genera el código de Room |
| **Kotlin Coroutines y Flow** | Operaciones asíncronas y datos reactivos |
| **Material Icons Extended** | Íconos de categorías y navegación |
| **java.time** | Manejo de fechas (requiere minSdk 26) |

## Estructura del proyecto

```
app/src/main/java/com/sudo/finanapp/
├── MainActivity.kt          ← punto de entrada, navegación y hoja de detalle
├── data/
│   ├── Models.kt            ← Movement, Category, Kind, formato de dinero y cálculos
│   ├── Database.kt          ← entidad, DAO y base de datos de Room
│   └── MovementsViewModel.kt
└── ui/
    ├── theme/               ← colores claro y oscuro
    ├── components/          ← barra de navegación y componentes compartidos
    ├── navigation/          ← rutas (Screen)
    └── (pantallas)          ← Inicio, Registrar, Movimientos, Análisis
```

> Los nombres de carpetas y paquetes pueden variar ligeramente según cómo se hayan organizado en el proyecto.

## Instrucciones de ejecución

### Requisitos previos

- **Android Studio** Ladybug (2024.2) o superior.
- **JDK 17** (incluido en Android Studio).
- **Android SDK** con API 35 instalada.
- Un emulador o un dispositivo físico con **Android 8.0 (API 26) o superior**.
- Conexión a internet la primera vez, para que Gradle descargue las dependencias.

### Pasos

1. Clona o descarga el proyecto y ábrelo desde **File → Open**.
2. Espera a que Gradle termine la sincronización. Si no arranca solo, pulsa **Sync Now**.
3. Verifica que `app/build.gradle.kts` incluya las dependencias:

   ```kotlin
   plugins {
       id("com.google.devtools.ksp")
   }

   dependencies {
       implementation(platform("androidx.compose:compose-bom:2024.10.01"))
       implementation("androidx.compose.ui:ui")
       implementation("androidx.compose.material3:material3")
       implementation("androidx.compose.material:material-icons-extended")
       implementation("androidx.activity:activity-compose:1.9.3")
       implementation("androidx.navigation:navigation-compose:2.8.4")
       implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

       implementation("androidx.room:room-runtime:2.6.1")
       implementation("androidx.room:room-ktx:2.6.1")
       ksp("androidx.room:room-compiler:2.6.1")
   }
   ```

4. Selecciona un emulador o conecta un dispositivo con la depuración USB activada.
5. Pulsa **Run ▶** (`Shift + F10`).

### Problemas frecuentes

| Síntoma | Causa y solución |
|---|---|
| `Unresolved reference: Icons...` | Falta `material-icons-extended`. Añádela y sincroniza. |
| `State<...> has no method getValue` | Faltan `import androidx.compose.runtime.getValue` y `setValue`. |
| La app se cierra al abrir Registrar desde la barra inferior | `Screen.Register` no debe ser una pestaña, porque su ruta lleva el parámetro `{id}`. |
| Errores de Room al compilar | Revisa que el plugin KSP esté declarado en el `build.gradle.kts` de la raíz y en el del módulo. |

## Persistencia de datos

### Solución seleccionada: Room (SQLite)

Los movimientos se almacenan en una base de datos SQLite local gestionada con **Room**. Los datos sobreviven al cierre de la app y al reinicio del dispositivo, y solo se eliminan si el usuario desinstala la aplicación o borra su almacenamiento.

### Por qué Room

| Alternativa | Por qué se descartó para los movimientos |
|---|---|
| **SharedPreferences** | API antigua, pensada para valores sueltos. No permite consultas ni filtros. |
| **DataStore** | Ideal para ajustes pequeños (tema, moneda), pero no para listas que se filtran, ordenan y agrupan. |
| **Archivos (JSON/CSV)** | Habría que leer y reescribir todo el archivo en cada cambio, sin consultas ni control de concurrencia. |
| **Room** | Adecuado para datos estructurados: permite consultas SQL, ordenamiento y filtros, y emite cambios en tiempo real con `Flow`. |

Los movimientos son una colección que crece, se edita, se elimina y se consulta por tipo, categoría y fecha. Eso es justo lo que una base de datos relacional resuelve bien.

### Cómo funciona

```
Pantalla ──► ViewModel ──► DAO ──► SQLite (cuadre.db)
   ▲                         │
   └──────── Flow<List> ◄────┘
```

1. **Entidad (`MovementEntity`):** representa una fila de la tabla `movements`. La categoría se guarda por el nombre del enum y la fecha como `epochDay` (número de días), lo que evita conversores personalizados.
2. **DAO (`MovementDao`):** define tres operaciones: `observeAll()` (devuelve un `Flow`), `upsert()` (inserta o actualiza según el `id`) y `delete()`.
3. **Base de datos (`AppDb`):** instancia única (patrón singleton) que crea el archivo `cuadre.db`.
4. **ViewModel (`MovementsViewModel`):** convierte las entidades en el modelo de dominio `Movement` y expone un `StateFlow`. Las escrituras se hacen en corrutinas, fuera del hilo principal.

### Cálculo automático del balance (RF04)

El balance **no se guarda**: se calcula cada vez a partir de la lista de movimientos.

```kotlin
val List<Movement>.income  // suma de ingresos
val List<Movement>.expense // suma de gastos
balance = income - expense
```

Como Room emite una lista nueva cada vez que la tabla cambia, al registrar, editar o eliminar un movimiento todas las pantallas se recomponen y el balance siempre es consistente. No existe un total almacenado que pueda quedar desactualizado.

### Decisiones de diseño

- **Montos como `Long` en pesos enteros:** evita los errores de redondeo de `Double`. El monto siempre es positivo y el signo lo determina la categoría (ingreso o gasto).
- **Un único origen de datos:** todas las pantallas leen el mismo `StateFlow` del ViewModel.

### Limitaciones y evolución

- **Sin datos iniciales:** al instalar, la base está vacía. Se pueden precargar ejemplos con un `RoomDatabase.Callback`.
- **Migraciones:** si se agrega una columna, hay que subir `version` en `@Database` y escribir una migración. En desarrollo se puede usar `fallbackToDestructiveMigration()`, que borra los datos al cambiar el esquema.
- **Ajustes de usuario:** el tema o el presupuesto de alimentación (hoy fijo en código) podrían guardarse con **DataStore**.
- **Sin respaldo en la nube:** los datos viven solo en el dispositivo. Una exportación a CSV o una sincronización con servidor serían mejoras futuras.

### Inspeccionar la base de datos

Con la app en ejecución: **View → Tool Windows → App Inspection → Database Inspector**. Ahí se ve y se consulta la tabla `movements` en vivo.

## Próximos pasos

- Implementar la pestaña **Más** (ajustes, cuentas y presupuestos editables).
- Precargar datos de ejemplo en el primer arranque.
- Exportar movimientos a CSV.
- Pruebas unitarias de los cálculos de balance y pruebas de la base de datos.
