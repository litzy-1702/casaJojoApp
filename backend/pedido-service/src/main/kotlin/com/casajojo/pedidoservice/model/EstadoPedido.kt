package com.casajojo.pedidoservice.model

/**
 * Estados del pedido: mismos valores que EstadoPedido de la app Android.
 */
enum class EstadoPedido {
    RECIBIDO,
    EN_PREPARACION,
    LISTO,
    ENTREGADO,
    CANCELADO
}
