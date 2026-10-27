package com.example.casajojo.data.model

data class DetallePedido(
    val menuItemId: Int,
    val nombrePlato: String,
    val precioUnitario: Int,
    val cantidad: Int
) {
    val subtotal: Int
        get() = precioUnitario * cantidad
}

fun List<DetallePedido>.calcularTotal(): Int =
    sumOf { it.subtotal }

fun List<DetallePedido>.contarPlatos(): Int =
    sumOf { it.cantidad }

fun List<DetallePedido>.aTextoDetalle(): String =
    joinToString(separator = ", ") { linea ->
        "${linea.cantidad}x ${linea.nombrePlato}"
    }
