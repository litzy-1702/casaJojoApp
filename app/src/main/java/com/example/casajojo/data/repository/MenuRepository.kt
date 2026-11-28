package com.example.casajojo.data.repository

import com.example.casajojo.data.local.MenuDao
import com.example.casajojo.data.model.MenuItem
import com.example.casajojo.data.network.MenuApiService
import com.example.casajojo.data.network.toDto
import com.example.casajojo.data.network.toEntity
import kotlinx.coroutines.flow.Flow

class MenuRepository(
    private val dao: MenuDao,
    private val api: MenuApiService? = null
) {

    fun observarMenuDelDia(fecha: String): Flow<List<MenuItem>> {
        return dao.observarPorFecha(fecha)
    }

    suspend fun sincronizarMenuDelDia(fecha: String) {
        val servicio = api ?: return
        try {
            val remotos = servicio.obtenerMenuPorFecha(fecha)
            remotos.forEach { dto ->
                dao.insertar(dto.toEntity())
            }
        } catch (_: Exception) {
            // Si no hay conexión, sigue mostrando los datos locales de Room
        }
    }

    suspend fun guardar(item: MenuItem) {
        if (item.id == 0) {
            val guardadoRemoto = runCatching { api?.crearPlato(item.toDto())?.toEntity() }.getOrNull()
            dao.insertar(guardadoRemoto ?: item)
        } else {
            runCatching { api?.actualizarPlato(item.id, item.toDto()) }
            dao.actualizar(item)
        }
    }

    suspend fun eliminar(item: MenuItem) {
        runCatching { api?.eliminarPlato(item.id) }
        dao.eliminar(item)
    }

    suspend fun cambiarDisponibilidad(id: Int, disponible: Boolean) {
        runCatching { api?.cambiarDisponibilidad(id, disponible) }
        dao.actualizarDisponibilidad(id, disponible)
    }
}