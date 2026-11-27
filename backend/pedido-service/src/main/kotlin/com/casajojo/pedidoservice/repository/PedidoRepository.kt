package com.casajojo.pedidoservice.repository

import com.casajojo.pedidoservice.model.EstadoPedido
import com.casajojo.pedidoservice.model.Pedido
import org.springframework.data.jpa.repository.JpaRepository

interface PedidoRepository : JpaRepository<Pedido, Int> {

    // Equivale a observarTodos() de la app: más recientes primero
    fun findAllByOrderByHoraCreacionDesc(): List<Pedido>

    // Equivale a observarPorEstado() de la app: más antiguos primero
    fun findByEstadoOrderByHoraCreacionAsc(estado: EstadoPedido): List<Pedido>
}
