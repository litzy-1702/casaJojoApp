package com.example.casajojo.feature.client

import com.example.casajojo.data.local.MenuDao
import com.example.casajojo.data.local.PedidoDao
import com.example.casajojo.data.model.MenuCasaJojo
import com.example.casajojo.data.model.MenuItem
import com.example.casajojo.data.model.Pedido
import com.example.casajojo.feature.operations.EstadoPedido
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

class FakeMenuDao(items: List<MenuItem>) : MenuDao {

    private val estado = MutableStateFlow(items)

    override fun observarPorFecha(fecha: String): Flow<List<MenuItem>> =
        estado

    override suspend fun insertar(item: MenuItem) {
        estado.value = estado.value + item
    }

    override suspend fun actualizar(item: MenuItem) {
        estado.value = estado.value.map {
            if (it.id == item.id) item else it
        }
    }

    override suspend fun eliminar(item: MenuItem) {
        estado.value = estado.value.filterNot { it.id == item.id }
    }

    override suspend fun actualizarDisponibilidad(
        id: Int,
        disponible: Boolean
    ) {
        estado.value = estado.value.map {
            if (it.id == id) it.copy(disponible = disponible) else it
        }
    }
}

class FakePedidoDao : PedidoDao {

    val pedidos = MutableStateFlow<List<Pedido>>(emptyList())

    var fallar: Boolean = false

    override fun observarTodos(): Flow<List<Pedido>> = pedidos

    override fun observarPorEstado(
        estado: EstadoPedido
    ): Flow<List<Pedido>> =
        pedidos.map { lista -> lista.filter { it.estado == estado } }

    override suspend fun insertar(pedido: Pedido): Long {
        if (fallar) throw IllegalStateException("Fallo simulado")

        val nuevoId = pedidos.value.size + 1
        pedidos.value = pedidos.value + pedido.copy(id = nuevoId)
        return nuevoId.toLong()
    }

    override suspend fun actualizar(pedido: Pedido) {
        pedidos.value = pedidos.value.map {
            if (it.id == pedido.id) pedido else it
        }
    }

    override suspend fun actualizarEstado(
        id: Int,
        nuevoEstado: EstadoPedido
    ) {
        pedidos.value = pedidos.value.map {
            if (it.id == id) it.copy(estado = nuevoEstado) else it
        }
    }

    override suspend fun actualizarPago(id: Int, pagado: Boolean) {
        pedidos.value = pedidos.value.map {
            if (it.id == id) it.copy(pagado = pagado) else it
        }
    }

    override suspend fun eliminar(pedido: Pedido) {
        pedidos.value = pedidos.value.filterNot { it.id == pedido.id }
    }
}

fun menuDePrueba(fecha: String = "2026-10-28"): List<MenuItem> =
    MenuCasaJojo.platosBase(fecha)
        .mapIndexed { indice, plato -> plato.copy(id = indice + 1) }

fun List<MenuItem>.plato(nombre: String): MenuItem =
    first { it.nombre == nombre }