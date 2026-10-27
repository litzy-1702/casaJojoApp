package com.example.casajojo.data.repository

import com.example.casajojo.data.local.PedidoDao
import com.example.casajojo.data.model.Pedido
import kotlinx.coroutines.flow.Flow

class PedidoRepository(
    private val dao: PedidoDao
) {

    fun observarPedidos(): Flow<List<Pedido>> {
        return dao.observarTodos()
    }

    suspend fun registrar(pedido: Pedido): Int {
        return dao.insertar(pedido).toInt()
    }

    companion object {
        const val RETIRO_LO_ANTES_POSIBLE = "Lo antes posible"
    }
}
