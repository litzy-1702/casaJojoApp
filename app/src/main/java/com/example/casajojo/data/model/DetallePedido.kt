
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
     * Construye una única descripción del plato.
     * El nombre contiene solamente la proteína.
     * Los agregados y la ensalada se muestran una sola vez.
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

fun List<DetallePedido>.aTextoDetalle(): String =
    joinToString(separator = ", ") { linea ->
        "${linea.cantidad}x ${linea.descripcionCompleta}"
    }
