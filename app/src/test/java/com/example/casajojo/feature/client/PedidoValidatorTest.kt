package com.example.casajojo.feature.client

import com.example.casajojo.data.model.DetallePedido
import com.example.casajojo.data.model.aTextoDetalle
import com.example.casajojo.data.model.calcularTotal
import com.example.casajojo.data.model.contarPlatos
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PedidoValidatorTest {

    private val pollo = DetallePedido(1, "Pollo Asado", 4800, 2)
    private val tortilla = DetallePedido(2, "Tortilla de Porotos verdes", 4600, 1)
    private val mediodia = LocalTime.of(12, 0)

@Test
    fun total_sumaLosSubtotales() {
        val carrito = listOf(pollo, tortilla)
        assertEquals(9600, pollo.subtotal)
        assertEquals(14200, carrito.calcularTotal())
        assertEquals(3, carrito.contarPlatos())
    }

    @Test
    fun textoDetalle_usaElFormatoDeCajaYCocina() {
        val texto = listOf(pollo, tortilla).aTextoDetalle()
        assertEquals("2x Pollo Asado, 1x Tortilla de Porotos verdes", texto)
    }

@Test
    fun nombre_vacio_esError() {
        assertNotNull(PedidoValidator.validarNombre("   "))
    }

    @Test
    fun nombre_conNumeros_esError() {
        assertNotNull(PedidoValidator.validarNombre("Juan123"))
    }

    @Test
    fun nombre_conTildesYEnie_esValido() {
        assertNull(PedidoValidator.validarNombre("José Muñoz"))
    }

    @Test
    fun nombre_muyLargo_esError() {
        assertNotNull(PedidoValidator.validarNombre("a".repeat(41)))
    }

@Test
    fun hora_loAntesPosible_noExigeHora() {
        assertNull(PedidoValidator.validarHoraRetiro(true, null, mediodia))
    }

    @Test
    fun hora_especifica_sinSeleccionar_esError() {
        assertNotNull(PedidoValidator.validarHoraRetiro(false, null, mediodia))
    }

    @Test
    fun hora_pasada_esError() {
        assertNotNull(PedidoValidator.validarHoraRetiro(false, "11:30", mediodia))
    }

    @Test
    fun hora_igualALaActual_esError() {
        assertNotNull(PedidoValidator.validarHoraRetiro(false, "12:00", mediodia))
    }

    @Test
    fun hora_futura_esValida() {
        assertNull(PedidoValidator.validarHoraRetiro(false, "13:30", mediodia))
    }

    @Test
    fun hora_formatoInvalido_esError() {
        assertNotNull(PedidoValidator.validarHoraRetiro(false, "13h30", mediodia))
    }

@Test
    fun restricciones_vacias_sonValidas() {
        assertNull(PedidoValidator.validarRestricciones(""))
    }

    @Test
    fun restricciones_muyLargas_sonError() {
        assertNotNull(PedidoValidator.validarRestricciones("x".repeat(151)))
    }

@Test
    fun carrito_vacio_esError() {
        assertNotNull(PedidoValidator.validarCarrito(emptyList(), setOf(1, 2)))
    }

    @Test
    fun carrito_conPlatoAgotado_esError() {
        val error = PedidoValidator.validarCarrito(listOf(pollo, tortilla), setOf(1))
        assertNotNull(error)
        assertTrue(error!!.contains("Tortilla de Porotos verdes"))
    }

    @Test
    fun carrito_conPlatosDisponibles_esValido() {
        assertNull(PedidoValidator.validarCarrito(listOf(pollo, tortilla), setOf(1, 2)))
    }

@Test
    fun checkout_completoYValido_noTieneErrores() {
        val errores = PedidoValidator.validarCheckout(
            nombre = "Ana Pérez",
            retiroLoAntesPosible = false,
            horaRetiro = "13:15",
            restricciones = "Sin cebolla",
            carrito = listOf(pollo),
            idsDisponibles = setOf(1),
            horaActual = mediodia
        )
        assertFalse(errores.tieneErrores)
    }

    @Test
    fun checkout_conVariosErrores_losMarcaPorCampo() {
        val errores = PedidoValidator.validarCheckout(
            nombre = "",
            retiroLoAntesPosible = false,
            horaRetiro = null,
            restricciones = "",
            carrito = emptyList(),
            idsDisponibles = emptySet(),
            horaActual = mediodia
        )
        assertTrue(errores.tieneErrores)
        assertNotNull(errores.nombre)
        assertNotNull(errores.horaRetiro)
        assertNull(errores.restricciones)
        assertNotNull(errores.carrito)
    }
}
