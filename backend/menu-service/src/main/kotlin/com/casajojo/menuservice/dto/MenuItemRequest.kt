package com.casajojo.menuservice.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive

/**
 * Datos que envía la app para crear o editar un plato.
 * Los campos son nulos a propósito: así un dato faltante
 * llega a la validación y se responde con 400.
 */
data class MenuItemRequest(

    @field:NotBlank(message = "El nombre del plato es obligatorio")
    val nombre: String? = null,

    @field:NotBlank(message = "La descripción es obligatoria")
    val descripcion: String? = null,

    @field:NotNull(message = "El precio es obligatorio")
    @field:Positive(message = "El precio debe ser mayor que cero")
    val precio: Int? = null,

    val disponible: Boolean = true,

    val imagenUri: String? = null,

    @field:NotBlank(message = "La fecha del menú es obligatoria")
    @field:Pattern(
        regexp = "\\d{4}-\\d{2}-\\d{2}",
        message = "La fecha debe tener el formato YYYY-MM-DD"
    )
    val fechaMenu: String? = null
)

/**
 * Cuerpo para cambiar solo la disponibilidad de un plato.
 */
data class DisponibilidadRequest(

    @field:NotNull(message = "Debes indicar si el plato está disponible")
    val disponible: Boolean? = null
)
