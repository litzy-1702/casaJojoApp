
package com.example.casajojo.feature.admin

data class MenuFormErrors(
    val nombre: String? = null,
    val descripcion: String? = null,
    val precio: String? = null
) {
    val tieneErrores: Boolean
        get() = nombre != null ||
                descripcion != null ||
                precio != null
}

fun validarMenu(
    nombre: String,
    descripcion: String,
    precio: String
): MenuFormErrors {

    val precioNumero = precio.trim().toIntOrNull()

    return MenuFormErrors(
        nombre = if (nombre.isBlank()) {
            "Ingresa el nombre del plato."
        } else {
            null
        },

        descripcion = if (descripcion.isBlank()) {
            "Ingresa una descripción."
        } else {
            null
        },

        precio = when {
            precio.isBlank() ->
                "Ingresa el precio."

            precioNumero == null ->
                "El precio debe ser un número entero."

            precioNumero <= 0 ->
                "El precio debe ser mayor que cero."

            else -> null
        }
    )
}
