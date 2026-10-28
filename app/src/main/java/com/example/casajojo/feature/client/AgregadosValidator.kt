package com.example.casajojo.feature.client

import com.example.casajojo.data.model.MenuCasaJojo

object AgregadosValidator {

    fun validar(agregados: List<String>): String? {
        return when {
            agregados.size < MenuCasaJojo.MIN_AGREGADOS ->
                "Selecciona al menos un acompañamiento."

            agregados.size > MenuCasaJojo.MAX_AGREGADOS ->
                "Solo puedes elegir hasta " +
                        "${MenuCasaJojo.MAX_AGREGADOS} acompañamientos."

            agregados.distinct().size != agregados.size ->
                "No repitas el mismo acompañamiento."

            agregados.any { it !in MenuCasaJojo.AGREGADOS } ->
                "Hay un acompañamiento que no está en el menú."

            else -> null
        }
    }
}