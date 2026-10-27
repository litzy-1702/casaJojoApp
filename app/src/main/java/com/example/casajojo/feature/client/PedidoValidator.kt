package com.example.casajojo.feature.client

import com.example.casajojo.data.model.DetallePedido
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

data class CheckoutErrores(
    val nombre: String? = null,
    val horaRetiro: String? = null,
    val restricciones: String? = null,
    val carrito: String? = null
) {
    val tieneErrores: Boolean
        get() = listOfNotNull(
            nombre,
            horaRetiro,
            restricciones,
            carrito
        ).isNotEmpty()
}

object PedidoValidator {

    const val NOMBRE_MIN = 2
    const val NOMBRE_MAX = 40
    const val RESTRICCIONES_MAX = 150

    private val FORMATO_HORA: DateTimeFormatter =
        DateTimeFormatter.ofPattern("HH:mm")

    private val SOLO_LETRAS = Regex("^[\\p{L} ]+$")

    fun validarNombre(nombre: String): String? {
        val limpio = nombre.trim()
        return when {
            limpio.isEmpty() ->
                "Ingresa tu nombre para identificar el pedido."
            limpio.length < NOMBRE_MIN ->
                "El nombre debe tener al menos $NOMBRE_MIN letras."
            limpio.length > NOMBRE_MAX ->
                "El nombre no puede superar $NOMBRE_MAX caracteres."
            !SOLO_LETRAS.matches(limpio) ->
                "Usa solo letras y espacios."
            else -> null
        }
    }

    fun validarHoraRetiro(
        retiroLoAntesPosible: Boolean,
        horaRetiro: String?,
        horaActual: LocalTime
    ): String? {
        if (retiroLoAntesPosible) return null

        if (horaRetiro.isNullOrBlank()) {
            return "Selecciona la hora de retiro."
        }

        val hora = try {
            LocalTime.parse(horaRetiro, FORMATO_HORA)
        } catch (e: DateTimeParseException) {
            return "La hora debe tener el formato HH:mm."
        }

        return if (!hora.isAfter(horaActual)) {
            "Elige una hora posterior a la actual."
        } else {
            null
        }
    }

    fun validarRestricciones(restricciones: String): String? {
        return if (restricciones.trim().length > RESTRICCIONES_MAX) {
            "Máximo $RESTRICCIONES_MAX caracteres."
        } else {
            null
        }
    }

    fun validarCarrito(
        carrito: List<DetallePedido>,
        idsDisponibles: Set<Int>
    ): String? {
        if (carrito.isEmpty()) {
            return "Agrega al menos un plato a tu pedido."
        }

        if (carrito.any { it.cantidad < 1 }) {
            return "Hay platos con una cantidad no válida."
        }

        val agotados = carrito.filter { it.menuItemId !in idsDisponibles }
        if (agotados.isNotEmpty()) {
            val nombres = agotados.joinToString(", ") { it.nombrePlato }
            return "Ya no están disponibles: $nombres. Quítalos del carrito."
        }

        return null
    }

    fun validarCheckout(
        nombre: String,
        retiroLoAntesPosible: Boolean,
        horaRetiro: String?,
        restricciones: String,
        carrito: List<DetallePedido>,
        idsDisponibles: Set<Int>,
        horaActual: LocalTime
    ): CheckoutErrores {
        return CheckoutErrores(
            nombre = validarNombre(nombre),
            horaRetiro = validarHoraRetiro(
                retiroLoAntesPosible,
                horaRetiro,
                horaActual
            ),
            restricciones = validarRestricciones(restricciones),
            carrito = validarCarrito(carrito, idsDisponibles)
        )
    }
}
