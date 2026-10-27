
package com.example.casajojo.data.model

import java.util.UUID

data class DetallePedido(
    val menuItemId: Int,
    val nombrePlato: String,
    val precioUnitario: Int,
    val cantidad: Int,
    val agregados: List<String> = emptyList(),
    val lineaId: String = UUID.randomUUID().toString()
) {
    val subtotal: Int
        get() = precioUnitario * cantidad

    /**
     * Descripción completa para mostrar en el carrito.
     * Incluye los agregados seleccionados y la ensalada.
     */
    val descripcionCompleta: String
        get() = buildString {
            append(nombrePlato)

            if (agregados.isNotEmpty()) {
                append(" | Agregados: ")
                append(agregados.joinToString(", "))
            }

            append(" | Ensalada incluida: ")
            append(MenuCasaJojo.ENSALADA_INCLUIDA)
        }
}

fun List<DetallePedido>.calcularTotal(): Int =
    sumOf { it.subtotal }

fun List<DetallePedido>.contarPlatos(): Int =
    sumOf { it.cantidad }

/**
 * Genera el detalle del pedido para caja y cocina.
 *
 * Si el plato tiene agregados, incluye la descripción completa.
 * Si no tiene agregados, conserva el formato tradicional
 * esperado por las pruebas existentes.
 */
fun List<DetallePedido>.aTextoDetalle(): String =
    joinToString(separator = ", ") { linea ->
        val descripcion = if (linea.agregados.isEmpty()) {
            linea.nombrePlato
        } else {
            linea.descripcionCompleta
        }

        "${linea.cantidad}x $descripcion"
    }
