package com.example.casajojo.data.repository

import com.example.casajojo.data.local.PedidoDao
import com.example.casajojo.data.model.Pedido
import com.example.casajojo.data.network.PedidoApiService
import com.example.casajojo.data.network.toDto
import com.example.casajojo.data.network.toEntity
import com.example.casajojo.feature.operations.EstadoPedido
import kotlinx.coroutines.flow.Flow

class PedidoRepository(
    private val dao: PedidoDao,
    private val api: PedidoApiService? = null
) {

    fun observarPedidos(): Flow<List<Pedido>> {
        return dao.observarTodos()
    }

    suspend fun sincronizarPedidos() {
        val servicio = api ?: return
        try {
            val remotos = servicio.obtenerPedidos()
            remotos.forEach { dto ->
                dao.insertar(dto.toEntity())
            }
        } catch (_: Exception) {
            // Si no hay conexion sigue mostrando los pedidos
        }
    }

    suspend fun registrar(pedido: Pedido): Int {
        val guardadoRemoto = runCatching {
            api?.crearPedido(pedido.toDto())?.toEntity()
        }.getOrNull()

        return dao.insertar(guardadoRemoto ?: pedido).toInt()
    }

    suspend fun actualizarEstado(id: Int, nuevoEstado: EstadoPedido) {
        runCatching { api?.actualizarEstado(id, nuevoEstado.name) }
        dao.actualizarEstado(id, nuevoEstado)
    }

    suspend fun actualizarPago(id: Int, pagado: Boolean) {
        runCatching { api?.actualizarPago(id, pagado) }
        dao.actualizarPago(id, pagado)
    }

    companion object {
        const val RETIRO_LO_ANTES_POSIBLE = "Lo antes posible"
    }
}