
package com.example.casajojo.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MenuCasaJojoTest {

    private val fecha = "2026-10-27"

    @Test
    fun menuBaseContieneSeisPlatosConPreciosCorrectos() {
        val platos = MenuCasaJojo.platosBase(fecha)

        assertEquals(6, platos.size)

        assertEquals(
            listOf(
                "Guatitas a la jardinera",
                "Cerdo mongoliano",
                "Filete de pollo",
                "Tortilla de porotos verdes",
                "Pollo asado",
                "Carne al jugo"
            ),
            platos.map { it.nombre }
        )

        assertEquals(
            listOf(4600, 4600, 4600, 4600, 4800, 4800),
            platos.map { it.precio }
        )

        assertTrue(
            platos.all { it.fechaMenu == fecha }
        )

        assertTrue(
            platos.all { it.disponible }
        )

        assertTrue(
            platos.all {
                it.descripcion.contains(
                    "mix de ensaladas",
                    ignoreCase = true
                )
            }
        )
    }

    @Test
    fun menuBaseTieneSeisAgregadosYLimitesCorrectos() {
        assertEquals(6, MenuCasaJojo.AGREGADOS.size)

        assertEquals(1, MenuCasaJojo.MIN_AGREGADOS)
        assertEquals(2, MenuCasaJojo.MAX_AGREGADOS)

        assertTrue(
            "Puré natural" in MenuCasaJojo.AGREGADOS
        )

        assertTrue(
            "Arroz normal" in MenuCasaJojo.AGREGADOS
        )

        assertTrue(
            "Arroz integral" in MenuCasaJojo.AGREGADOS
        )

        assertTrue(
            "Verduras salteadas" in MenuCasaJojo.AGREGADOS
        )

        assertTrue(
            "Fideos blancos" in MenuCasaJojo.AGREGADOS
        )

        assertTrue(
            "Fideos al pesto" in MenuCasaJojo.AGREGADOS
        )
    }

    @Test
    fun cargaInicialDevuelveLosSeisPlatosFaltantes() {
        val faltantes = MenuCasaJojo.platosFaltantes(
            platosExistentes = emptyList(),
            fechaMenu = fecha
        )

        assertEquals(6, faltantes.size)
    }

    @Test
    fun segundaCargaNoDevuelvePlatosDuplicados() {
        val primeraCarga =
            MenuCasaJojo.platosBase(fecha)

        val segundaCarga =
            MenuCasaJojo.platosFaltantes(
                platosExistentes = primeraCarga,
                fechaMenu = fecha
            )

        assertTrue(segundaCarga.isEmpty())
    }

    @Test
    fun reconoceNombresExistentesSinDistinguirMayusculas() {
        val polloExistente = MenuItem(
            id = 10,
            nombre = "  POLLO ASADO  ",
            descripcion = "",
            precio = 4800,
            disponible = false,
            fechaMenu = fecha
        )

        val faltantes = MenuCasaJojo.platosFaltantes(
            platosExistentes = listOf(polloExistente),
            fechaMenu = fecha
        )

        assertEquals(5, faltantes.size)

        assertFalse(
            faltantes.any {
                it.nombre.equals(
                    "Pollo asado",
                    ignoreCase = true
                )
            }
        )

        // La función identifica faltantes; no modifica el registro.
        assertFalse(polloExistente.disponible)
    }

    @Test
    fun unPlatoDeOtraFechaNoImpideCargarElPlatoDeHoy() {
        val polloDeAyer = MenuItem(
            id = 15,
            nombre = "Pollo asado",
            descripcion = "",
            precio = 4800,
            disponible = false,
            fechaMenu = "2026-10-26"
        )

        val faltantes = MenuCasaJojo.platosFaltantes(
            platosExistentes = listOf(polloDeAyer),
            fechaMenu = fecha
        )

        assertEquals(6, faltantes.size)

        assertTrue(
            faltantes.any {
                it.nombre == "Pollo asado" &&
                        it.fechaMenu == fecha
            }
        )
    }
}
