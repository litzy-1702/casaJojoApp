package com.example.casajojo.feature.admin

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MenuValidatorTest {

    @Test
    fun nombreVacioDebeMostrarError() {
        val resultado = validarMenu("", "Pollo asado", "4600")

        assertEquals(
            "Ingresa el nombre del plato.",
            resultado.nombre
        )
    }

    @Test
    fun descripcionVaciaDebeMostrarError() {
        val resultado = validarMenu(
            "Pollo asado",
            "   ",
            "4600"
        )

        assertEquals(
            "Ingresa una descripción.",
            resultado.descripcion
        )
    }

    @Test
    fun precioVacioDebeMostrarError() {
        val resultado = validarMenu(
            "Pollo asado",
            "Pollo con agregado",
            ""
        )

        assertEquals("Ingresa el precio.", resultado.precio)
    }

    @Test
    fun precioNoNumericoDebeMostrarError() {
        val resultado = validarMenu(
            "Pollo asado",
            "Pollo con agregado",
            "abc"
        )

        assertEquals(
            "El precio debe ser un número entero.",
            resultado.precio
        )
    }

    @Test
    fun precioCeroONegativoDebeMostrarError() {
        val resultadoCero = validarMenu(
            "Pollo asado",
            "Pollo con agregado",
            "0"
        )

        val resultadoNegativo = validarMenu(
            "Pollo asado",
            "Pollo con agregado",
            "-4600"
        )

        assertEquals(
            "El precio debe ser mayor que cero.",
            resultadoCero.precio
        )

        assertEquals(
            "El precio debe ser mayor que cero.",
            resultadoNegativo.precio
        )
    }

    @Test
    fun datosValidosNoDebenGenerarErrores() {
        val resultado = validarMenu(
            "Pollo asado",
            "Pollo con agregado y ensalada",
            "4600"
        )

        assertFalse(resultado.tieneErrores)
        assertNull(resultado.nombre)
        assertNull(resultado.descripcion)
        assertNull(resultado.precio)
    }

    @Test
    fun precioConEspaciosDebeSerAceptado() {
        val resultado = validarMenu(
            "Pollo asado",
            "Pollo con agregado",
            " 4600 "
        )

        assertNull(resultado.precio)
        assertFalse(resultado.tieneErrores)
    }
}

