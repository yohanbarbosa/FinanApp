package com.cuadre.app.data

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.*

class MovementsViewModel : ViewModel() {
    private val _items = MutableStateFlow(seedMovements())
    val items: StateFlow<List<Movement>> = _items.asStateFlow()
    private var nextId = 1000L

    fun get(id: Long): Movement? = _items.value.firstOrNull { it.id == id }

    /** id == 0 → nuevo registro; otro id → edición. */
    fun save(m: Movement) = _items.update { list ->
        if (m.id == 0L) list + m.copy(id = nextId++)
        else list.map { if (it.id == m.id) m else it }
    }

    fun delete(id: Long) = _items.update { list -> list.filterNot { it.id == id } }
}
