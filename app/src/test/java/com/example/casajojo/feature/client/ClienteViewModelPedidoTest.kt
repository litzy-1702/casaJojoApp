package com.example.casajojo.feature.client

import com.example.casajojo.data.repository.MenuRepository
import com.example.casajojo.data.repository.PedidoRepository
import com.example.casajojo.feature.operations.EstadoPedido
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ClienteViewModelPedidoTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val menu = menuDePrueba()
    private val pedidoDao = FakePedidoDao()
    private lateinit var viewModel: ClienteViewModel

    @Before
    fun preparar() {
        viewModel = ClienteViewModel(
            menuRepository = MenuRepository(FakeMenuDao(menu)),
            pedidoRepository = PedidoRepository(pedidoDao)
        )
    }

    @Test
    fun alConfirmar_elPedidoGuardaLosAgregadosElegidos() {
        viewModel.agregarPlato(
            menu.plato("Pollo asado"),
            listOf("Puré natural", "Arroz normal")
        )
        viewModel.onNombreChange("Ana Pérez")

        viewModel.confirmarPedido()

        val guardado = pedidoDao.pedidos.value.single()

        assertEquals(
            "1x Pollo asado | Agregados: Arroz normal, Puré natural " +
                    "| Ensalada incluida: Mix de ensaladas",
            guardado.detalleItems
        )
        assertEquals("Ana Pérez", guardado.clienteNombre)
        assertEquals(4800, guardado.total)
        assertEquals("Lo antes posible", guardado.horaComprometida)
        assertEquals(EstadoPedido.RECIBIDO, guardado.estado)
        assertFalse(guardado.pagado)
    }

    @Test
    fun conVariasLineas_elDetalleYElTotalSonCorrectos() {
        val filete = menu.plato("Filete de pollo")

        viewModel.agregarPlato(filete, listOf("Arroz normal"))
        viewModel.agregarPlato(filete, listOf("Arroz normal"))
        viewModel.agregarPlato(
            menu.plato("Pollo asado"),
            listOf("Puré natural", "Fideos al pesto")
        )
        viewModel.onNombreChange("Luis Soto")

        viewModel.confirmarPedido()

        val guardado = pedidoDao.pedidos.value.single()

        assertEquals(
            "2x Filete de pollo | Agregados: Arroz normal " +
                    "| Ensalada incluida: Mix de ensaladas, " +
                    "1x Pollo asado | Agregados: Fideos al pesto, Puré natural " +
                    "| Ensalada incluida: Mix de ensaladas",
            guardado.detalleItems
        )
        assertEquals(14000, guardado.total)
    }

    @Test
    fun cajaYCocina_leenElMismoDetalleQueSeGuardo() {
        viewModel.agregarPlato(
            menu.plato("Carne al jugo"),
            listOf("Arroz integral", "Verduras salteadas")
        )
        viewModel.onNombreChange("Marta Rojas")
        viewModel.onRestriccionesChange("Sin sal")

        viewModel.confirmarPedido()

        val enCajaYCocina = runBlocking {
            pedidoDao.observarTodos().first()
        }.single()

        assertTrue(
            enCajaYCocina.detalleItems.contains(
                "Agregados: Arroz integral, Verduras salteadas"
            )
        )
        assertTrue(
            enCajaYCocina.detalleItems.contains(
                "Ensalada incluida: Mix de ensaladas"
            )
        )
        assertEquals("Sin sal", enCajaYCocina.restricciones)
        assertEquals(4800, enCajaYCocina.total)
    }

    @Test
    fun tras_confirmar_vaciaElCarritoYSeleccionaElPedido() {
        viewModel.agregarPlato(
            menu.plato("Filete de pollo"),
            listOf("Arroz normal")
        )
        viewModel.onNombreChange("Ana Pérez")

        viewModel.confirmarPedido()

        assertTrue(viewModel.carrito.value.isEmpty())
        assertEquals(1, viewModel.checkout.value.pedidoConfirmadoId)
        assertEquals(1, viewModel.pedidoSeleccionadoId.value)
    }

    @Test
    fun sinNombre_noGuardaYConservaElCarrito() {
        viewModel.agregarPlato(
            menu.plato("Filete de pollo"),
            listOf("Arroz normal")
        )

        viewModel.confirmarPedido()

        assertTrue(pedidoDao.pedidos.value.isEmpty())
        assertNotNull(viewModel.checkout.value.errores.nombre)
        assertEquals(1, viewModel.carrito.value.size)
    }

    @Test
    fun carritoVacio_noGuarda() {
        viewModel.onNombreChange("Ana Pérez")

        viewModel.confirmarPedido()

        assertTrue(pedidoDao.pedidos.value.isEmpty())
        assertNotNull(viewModel.checkout.value.errores.carrito)
    }

    @Test
    fun siFallaElGuardado_elCarritoSeConserva() {
        pedidoDao.fallar = true
        viewModel.agregarPlato(
            menu.plato("Filete de pollo"),
            listOf("Arroz normal")
        )
        viewModel.onNombreChange("Ana Pérez")

        viewModel.confirmarPedido()

        assertNotNull(viewModel.checkout.value.errorGuardado)
        assertFalse(viewModel.checkout.value.guardando)
        assertNull(viewModel.checkout.value.pedidoConfirmadoId)
        assertEquals(1, viewModel.carrito.value.size)
        assertTrue(pedidoDao.pedidos.value.isEmpty())
    }
}