package com.casajojo.pedidoservice.service

import com.casajojo.pedidoservice.dto.PedidoRequest
import com.casajojo.pedidoservice.model.EstadoPedido
import com.casajojo.pedidoservice.model.Pedido
import com.casajojo.pedidoservice.repository.PedidoRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class PedidoService(
    private val repository: PedidoRepository
) {

    fun listar(estado: EstadoPedido?): List<Pedido> =
        if (estado == null) {
            repository.findAllByOrderByHoraCreacionDesc()
        } else {
            repository.findByEstadoOrderByHoraCreacionAsc(estado)
        }

    fun obtener(id: Int): Pedido =
        repository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el pedido con id $id")
        }

    fun crear(request: PedidoRequest): Pedido {
        val pedido = Pedido(
            clienteNombre = request.clienteNombre!!.trim(),
            clienteTelefono = request.clienteTelefono?.filter { it.isDigit() } ?: "",
            detalleItems = request.detalleItems!!.trim(),
            restricciones = request.restricciones?.trim() ?: "",
            horaCreacion = request.horaCreacion ?: System.currentTimeMillis(),
            horaComprometida = request.horaComprometida!!.trim(),
            total = request.total!!,
            estado = EstadoPedido.RECIBIDO,
            pagado = false,
            metodoPago = request.metodoPago?.takeIf { it.isNotBlank() } ?: "Efectivo"
        )
        return repository.save(pedido)
    }

    fun cambiarEstado(id: Int, estado: EstadoPedido): Pedido {
        val pedido = obtener(id)
        pedido.estado = estado
        return repository.save(pedido)
    }

    fun cambiarPago(id: Int, pagado: Boolean): Pedido {
        val pedido = obtener(id)
        pedido.pagado = pagado
        return repository.save(pedido)
    }

    fun eliminar(id: Int) {
        val pedido = obtener(id)
        repository.delete(pedido)
    }
}
