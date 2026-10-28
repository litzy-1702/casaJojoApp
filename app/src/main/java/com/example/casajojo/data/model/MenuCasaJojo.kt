
package com.example.casajojo.data.model

import java.util.Locale

object MenuCasaJojo {

    const val ENSALADA_INCLUIDA = "Mix de ensaladas"

    const val MIN_AGREGADOS = 1
    const val MAX_AGREGADOS = 2

    val AGREGADOS: List<String> = listOf(
        "Puré natural",
        "Arroz normal",
        "Arroz integral",
        "Verduras salteadas",
        "Fideos blancos",
        "Fideos al pesto"
    )

    /**
     * Devuelve los seis platos base para una fecha determinada.
     * No guarda nada en la base de datos.
     */
    fun platosBase(fechaMenu: String): List<MenuItem> {
        val descripcion =
            "Incluye mix de ensaladas. " +
                    "Selecciona uno o dos agregados."

        return listOf(
            MenuItem(
                nombre = "Guatitas a la jardinera",
                descripcion = descripcion,
                precio = 4600,
                disponible = true,
                fechaMenu = fechaMenu
            ),
            MenuItem(
                nombre = "Cerdo mongoliano",
                descripcion = descripcion,
                precio = 4600,
                disponible = true,
                fechaMenu = fechaMenu
            ),
            MenuItem(
                nombre = "Filete de pollo",
                descripcion = descripcion,
                precio = 4600,
                disponible = true,
                fechaMenu = fechaMenu
            ),
            MenuItem(
                nombre = "Tortilla de porotos verdes",
                descripcion = descripcion,
                precio = 4600,
                disponible = true,
                fechaMenu = fechaMenu
            ),
            MenuItem(
                nombre = "Pollo asado",
                descripcion = descripcion,
                precio = 4800,
                disponible = true,
                fechaMenu = fechaMenu
            ),
            MenuItem(
                nombre = "Carne al jugo",
                descripcion = descripcion,
                precio = 4800,
                disponible = true,
                fechaMenu = fechaMenu
            )
        )
    }

    /**
     * Devuelve únicamente los platos base que todavía no existen
     * para la fecha solicitada.
     *
     * La comparación ignora mayúsculas y espacios exteriores.
     * Los registros existentes no se modifican.
     */
    fun platosFaltantes(
        platosExistentes: List<MenuItem>,
        fechaMenu: String
    ): List<MenuItem> {

        val nombresExistentes = platosExistentes
            .filter { it.fechaMenu == fechaMenu }
            .map { normalizarNombre(it.nombre) }
            .toSet()

        return platosBase(fechaMenu).filter { plato ->
            normalizarNombre(plato.nombre) !in nombresExistentes
        }
    }

    private fun normalizarNombre(nombre: String): String {
        return nombre.trim().lowercase(Locale.ROOT)
    }
}
