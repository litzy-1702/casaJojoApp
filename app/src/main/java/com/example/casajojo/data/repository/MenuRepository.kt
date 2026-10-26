package com.example.casajojo.data.repository

import com.example.casajojo.data.local.MenuDao
import com.example.casajojo.data.model.MenuItem
import kotlinx.coroutines.flow.Flow

class MenuRepository(
    private val dao: MenuDao
) {

    fun observarMenuDelDia(
        fecha: String
    ): Flow<List<MenuItem>> {
        return dao.observarPorFecha(fecha)
    }

    suspend fun guardar(item: MenuItem) {
        if (item.id == 0) {
            dao.insertar(item)
        } else {
            dao.actualizar(item)
        }
    }

    suspend fun eliminar(item: MenuItem) {
        dao.eliminar(item)
    }

    suspend fun cambiarDisponibilidad(
        id: Int,
        disponible: Boolean
    ) {
        dao.actualizarDisponibilidad(id, disponible)
    }
}
