package com.example.casajojo.feature.client

import com.example.casajojo.data.repository.MenuRepository
import com.example.casajojo.data.repository.PedidoRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ClienteViewModelCarritoTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val menu = menuDePrueba()
    private lateinit var viewModel: ClienteViewModel

    @Before
    fun preparar() {
        viewModel = ClienteViewModel(
            menuRepository = MenuRepository(FakeMenuDao(menu)),
            pedidoRepository = PedidoRepository(FakePedidoDao())
        )
    }

    @Test
    fun combinacionesIguales_agrupanCantidades() {
        val filete = menu.plato("Filete de pollo")

        viewModel.agregarPlato(filete, listOf("Arroz normal"))
        viewModel.agregarPlato(filete, listOf("Arroz normal"))

        val carrito = viewModel.carrito.value
        assertEquals(1, carrito.size)
        assertEquals(2, carrito.single().cantidad)
    }

    @Test
    fun mismosAgregadosEnOtroOrden_cuentanComoLaMismaCombinacion() {
        val pollo = menu.plato("Pollo asado")

        viewModel.agregarPlato(pollo, listOf("Puré natural", "Arroz normal"))
        viewModel.agregarPlato(pollo, listOf("Arroz normal", "Puré natural"))

        val carrito = viewModel.carrito.value
        assertEquals(1, carrito.size)
        assertEquals(2, carrito.single().cantidad)
    }

    @Test
    fun mismoPlatoConAgregadosDistintos_quedaSeparado() {
        val filete = menu.plato("Filete de pollo")

        viewModel.agregarPlato(filete, listOf("Arroz normal"))
        viewModel.agregarPlato(filete, listOf("Arroz integral"))
        viewModel.agregarPlato(filete, listOf("Arroz normal", "Puré natural"))

        val carrito = viewModel.carrito.value
        assertEquals(3, carrito.size)
        assertTrue(carrito.all { it.cantidad == 1 })
    }

    @Test
    fun platosDistintosConMismosAgregados_quedanSeparados() {
        viewModel.agregarPlato(
            menu.plato("Filete de pollo"),
            listOf("Arroz normal")
        )
        viewModel.agregarPlato(
            menu.plato("Cerdo mongoliano"),
            listOf("Arroz normal")
        )

        assertEquals(2, viewModel.carrito.value.size)
    }

    @Test
    fun subtotalesYTotal_secalculanConLos4600Y4800() {
        val filete = menu.plato("Filete de pollo")
        val polloAsado = menu.plato("Pollo asado")

        viewModel.agregarPlato(filete, listOf("Arroz normal"))
        viewModel.agregarPlato(filete, listOf("Arroz normal"))
        viewModel.agregarPlato(
            polloAsado,
            listOf("Puré natural", "Fideos al pesto")
        )

        assertEquals(
            listOf(9200, 4800),
            viewModel.carrito.value.map { it.subtotal }
        )
        assertEquals(14000, viewModel.total.value)
        assertEquals(3, viewModel.cantidadPlatos.value)
    }

    @Test
    fun cadaPlatoDelMenuBase_cobraSuPrecio() {
        val esperado = mapOf(
            "Guatitas a la jardinera" to 4600,
            "Cerdo mongoliano" to 4600,
            "Filete de pollo" to 4600,
            "Tortilla de porotos verdes" to 4600,
            "Pollo asado" to 4800,
            "Carne al jugo" to 4800
        )

        esperado.forEach { (nombre, precio) ->
            viewModel.agregarPlato(menu.plato(nombre), listOf("Arroz normal"))

            val linea = viewModel.carrito.value
                .first { it.nombrePlato == nombre }

            assertEquals(nombre, precio, linea.precioUnitario)
        }

        assertEquals(28000, viewModel.total.value)
        assertEquals(6, viewModel.cantidadPlatos.value)
    }

    @Test
    fun disminuirHastaCero_eliminaLaLineaYElTotal() {
        val filete = menu.plato("Filete de pollo")

        viewModel.agregarPlato(filete, listOf("Arroz normal"))
        viewModel.agregarPlato(filete, listOf("Arroz normal"))
        val lineaId = viewModel.carrito.value.single().lineaId

        viewModel.disminuirCantidad(lineaId)
        assertEquals(1, viewModel.carrito.value.single().cantidad)
        assertEquals(4600, viewModel.total.value)

        viewModel.disminuirCantidad(lineaId)
        assertTrue(viewModel.carrito.value.isEmpty())
        assertEquals(0, viewModel.total.value)
    }

    @Test
    fun quitarUnaLinea_noAfectaLasDemasCombinaciones() {
        val filete = menu.plato("Filete de pollo")

        viewModel.agregarPlato(filete, listOf("Arroz normal"))
        viewModel.agregarPlato(filete, listOf("Arroz integral"))
        val primera = viewModel.carrito.value.first().lineaId

        viewModel.quitarPlato(primera)

        val restante = viewModel.carrito.value.single()
        assertEquals(listOf("Arroz integral"), restante.agregados)
    }

    @Test
    fun agregadosVacios_noAgregan() {
        viewModel.agregarPlato(menu.plato("Filete de pollo"), emptyList())

        assertTrue(viewModel.carrito.value.isEmpty())
        assertEquals(
            "Selecciona al menos un acompañamiento.",
            viewModel.mensaje.value
        )
    }

    @Test
    fun agregadosDuplicados_noAgregan() {
        viewModel.agregarPlato(
            menu.plato("Filete de pollo"),
            listOf("Arroz normal", "Arroz normal")
        )

        assertTrue(viewModel.carrito.value.isEmpty())
        assertEquals(
            "No repitas el mismo acompañamiento.",
            viewModel.mensaje.value
        )
    }

    @Test
    fun masAgregadosQueElLimite_noAgregan() {
        viewModel.agregarPlato(
            menu.plato("Filete de pollo"),
            listOf("Arroz normal", "Puré natural", "Fideos blancos")
        )

        assertTrue(viewModel.carrito.value.isEmpty())
        assertEquals(0, viewModel.total.value)
    }

    @Test
    fun agregadoInexistente_noAgrega() {
        viewModel.agregarPlato(
            menu.plato("Filete de pollo"),
            listOf("Papas fritas")
        )

        assertTrue(viewModel.carrito.value.isEmpty())
    }

    @Test
    fun unoODosAgregadosValidos_siAgregan() {
        val filete = menu.plato("Filete de pollo")

        viewModel.agregarPlato(filete, listOf("Arroz normal"))
        viewModel.agregarPlato(filete, listOf("Puré natural", "Arroz integral"))

        assertEquals(2, viewModel.carrito.value.size)
    }

    @Test
    fun unaSeleccionInvalida_noAlteraLoQueYaEstabaEnElCarrito() {
        val filete = menu.plato("Filete de pollo")

        viewModel.agregarPlato(filete, listOf("Arroz normal"))
        viewModel.agregarPlato(filete, emptyList())

        assertEquals(1, viewModel.carrito.value.single().cantidad)
        assertEquals(4600, viewModel.total.value)
    }
}