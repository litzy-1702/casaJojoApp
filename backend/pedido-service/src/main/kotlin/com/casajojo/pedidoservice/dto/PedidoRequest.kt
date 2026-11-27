package com.casajojo.pedidoservice.dto

import com.casajojo.pedidoservice.model.EstadoPedido
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

/**
 * Datos que envía la app al confirmar un pedido.
 * El estado (RECIBIDO) y el pago (pendiente) los asigna el servidor.
 */
data class PedidoRequest(

    @field:NotBlank(message = "El nombre del cliente es obligatorio")
    val clienteNombre: String? = null,

    @field:Pattern(regexp = "\\d{0,12}", message = "El teléfono debe contener solo dígitos (máximo 12)")
    val clienteTelefono: String? = null,

    @field:NotBlank(message = "El detalle del pedido es obligatorio")
    val detalleItems: String? = null,

    @field:Size(max = 150, message = "Las restricciones admiten como máximo 150 caracteres")
    val restricciones: String? = null,

    // Opcional: si no se envía, se usa la hora del servidor
    val horaCreacion: Long? = null,

    @field:NotBlank(message = "La hora de retiro es obligatoria")
    val horaComprometida: String? = null,

    @field:NotNull(message = "El total es obligatorio")
    @field:Positive(message = "El total debe ser mayor que cero")
    val total: Int? = null,

    val metodoPago: String? = null
)

/**
 * Cuerpo para cambiar el estado de un pedido (Caja / Cocina).
 */
data class EstadoRequest(

    @field:NotNull(message = "Debes indicar el nuevo estado")
    val estado: EstadoPedido? = null
)

/**
 * Cuerpo para marcar un pedido como pagado o pendiente (Caja).
 */
data class PagoRequest(

    @field:NotNull(message = "Debes indicar si el pedido está pagado")
    val pagado: Boolean? = null
)
