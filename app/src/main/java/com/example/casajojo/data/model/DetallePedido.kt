package com.example.casajojo.data.model

/**
 * Línea del carrito del cliente (Integrante 2).
 *
 * NO es una tabla de Room: vive en memoria dentro de ClienteViewModel
 * mientras el cliente arma su pedido. Al confirmar, la lista de líneas
 * se convierte en el texto de Pedido.detalleItems, que es el formato
 * que ya usan las pantallas de Caja y Cocina (Integrante 3).
 *
 * Guarda una copia del nombre y del precio del plato al momento de
 * agregarlo, para que el pedido no cambie si el administrador
 * edita el menú después.
 */
data class DetallePedido(
    val menuItemId: Int,
    val nombrePlato: String,
    val precioUnitario: Int,
    val cantidad: Int
) {
    /** Subtotal de la línea en pesos chilenos. */
    val subtotal: Int
        get() = precioUnitario * cantidad
}

/** Suma de los subtotales de todas las líneas del carrito. */
fun List<DetallePedido>.calcularTotal(): Int =
    sumOf { it.subtotal }

/** Cantidad total de platos en el carrito (suma de cantidades). */
fun List<DetallePedido>.contarPlatos(): Int =
    sumOf { it.cantidad }

/**
 * Convierte el carrito al texto que se guarda en Pedido.detalleItems.
 * Ejemplo: "2x Pollo Asado, 1x Carne al jugo"
 */
fun List<DetallePedido>.aTextoDetalle(): String =
    joinToString(separator = ", ") { linea ->
        "${linea.cantidad}x ${linea.nombrePlato}"
    }
