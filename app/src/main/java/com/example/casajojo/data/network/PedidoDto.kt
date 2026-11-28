package com.example.casajojo.data.network

import com.example.casajojo.data.model.Pedido
import com.example.casajojo.feature.operations.EstadoPedido

data class PedidoDto(
    val id: Int = 0,
    val clienteNombre: String,
    val clienteTelefono: String = "",
    val detalleItems: String,
    val restricciones: String = "",
    val horaCreacion: Long = System.currentTimeMillis(),
    val horaComprometida: String,
    val total: Int,
    val estado: String = EstadoPedido.RECIBIDO.name,
    val pagado: Boolean = false,
    val metodoPago: String = "Efectivo"
)

fun PedidoDto.toEntity() = Pedido(
    id = id,
    clienteNombre = clienteNombre,
    clienteTelefono = clienteTelefono,
    detalleItems = detalleItems,
    restricciones = restricciones,
    horaCreacion = horaCreacion,
    horaComprometida = horaComprometida,
    total = total,
    estado = runCatching { EstadoPedido.valueOf(estado) }.getOrDefault(EstadoPedido.RECIBIDO),
    pagado = pagado,
    metodoPago = metodoPago
)

fun Pedido.toDto() = PedidoDto(
    id = id,
    clienteNombre = clienteNombre,
    clienteTelefono = clienteTelefono,
    detalleItems = detalleItems,
    restricciones = restricciones,
    horaCreacion = horaCreacion,
    horaComprometida = horaComprometida,
    total = total,
    estado = estado.name,
    pagado = pagado,
    metodoPago = metodoPago
)