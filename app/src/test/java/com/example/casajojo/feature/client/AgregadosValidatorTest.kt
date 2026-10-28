package com.example.casajojo.feature.client

import com.example.casajojo.data.model.MenuCasaJojo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AgregadosValidatorTest {

    @Test
    fun unAgregadoValido_esAceptado() {
        assertNull(AgregadosValidator.validar(listOf("Puré natural")))
    }

    @Test
    fun dosAgregadosValidos_sonAceptados() {
        assertNull(
            AgregadosValidator.validar(
                listOf("Arroz integral", "Verduras salteadas")
            )
        )
    }

    @Test
    fun todosLosAgregadosDelMenu_sonValidosPorSeparado() {
        MenuCasaJojo.AGREGADOS.forEach { agregado ->
            assertNull(AgregadosValidator.validar(listOf(agregado)))
        }
    }

    @Test
    fun seleccionVacia_esRechazada() {
        assertEquals(
            "Selecciona al menos un acompañamiento.",
            AgregadosValidator.validar(emptyList())
        )
    }

    @Test
    fun agregadoDuplicado_esRechazado() {
        assertEquals(
            "No repitas el mismo acompañamiento.",
            AgregadosValidator.validar(
                listOf("Arroz normal", "Arroz normal")
            )
        )
    }

    @Test
    fun masDelLimite_esRechazado() {
        assertEquals(
            "Solo puedes elegir hasta 2 acompañamientos.",
            AgregadosValidator.validar(
                listOf("Puré natural", "Arroz normal", "Fideos blancos")
            )
        )
    }

    @Test
    fun duplicadosQueExcedenElLimite_sonRechazados() {
        val error = AgregadosValidator.validar(
            listOf("Puré natural", "Puré natural", "Arroz normal")
        )

        assertEquals(
            "Solo puedes elegir hasta 2 acompañamientos.",
            error
        )
    }

    @Test
    fun agregadoQueNoExisteEnElMenu_esRechazado() {
        assertEquals(
            "Hay un acompañamiento que no está en el menú.",
            AgregadosValidator.validar(listOf("Papas fritas"))
        )
    }

    @Test
    fun agregadoValidoMasUnoInexistente_esRechazado() {
        assertEquals(
            "Hay un acompañamiento que no está en el menú.",
            AgregadosValidator.validar(
                listOf("Puré natural", "Papas fritas")
            )
        )
    }
}