package com.cuadre.app.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sudo.finanapp.data.AppDb
import com.sudo.finanapp.data.toEntity
import com.sudo.finanapp.data.toModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MovementsViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDb.get(app).dao()

    val items: StateFlow<List<Movement>> = dao.observeAll()
        .map { list -> list.map { it.toModel() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun get(id: Long): Movement? = items.value.firstOrNull { it.id == id }

    fun save(m: Movement) { viewModelScope.launch { dao.upsert(m.toEntity()) } }

    fun delete(id: Long) { viewModelScope.launch { dao.delete(id) } }
}