
package com.example.casajojo.feature.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.casajojo.data.model.DetallePedido
import com.example.casajojo.data.model.MenuCasaJojo
import com.example.casajojo.data.model.MenuItem
import com.example.casajojo.data.model.Pedido
import com.example.casajojo.data.model.aTextoDetalle
import com.example.casajojo.data.model.calcularTotal
import com.example.casajojo.data.model.contarPlatos
import com.example.casajojo.data.repository.MenuRepository
import com.example.casajojo.data.repository.PedidoRepository
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

data class CheckoutUiState(
    val nombre: String = "",
    val retiroLoAntesPosible: Boolean = true,
    val horaRetiro: String? = null,
    val restricciones: String = "",
    val errores: CheckoutErrores = CheckoutErrores(),
    val guardando: Boolean = false,
    val errorGuardado: String? = null,
    val pedidoConfirmadoId: Int? = null
)

class ClienteViewModel(
    menuRepository: MenuRepository,
    private val pedidoRepository: PedidoRepository
) : ViewModel() {

    val fechaMenu: String = LocalDate.now().toString()

    val menuDisponible: StateFlow<List<MenuItem>?> =
        menuRepository.observarMenuDelDia(fechaMenu)
            .map<List<MenuItem>, List<MenuItem>?> { lista ->
                lista.filter { it.disponible }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = null
            )

    private val _carrito =
        MutableStateFlow<List<DetallePedido>>(emptyList())

    val carrito: StateFlow<List<DetallePedido>> =
        _carrito.asStateFlow()

    val total: StateFlow<Int> =
        _carrito
            .map { it.calcularTotal() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = 0
            )

    val cantidadPlatos: StateFlow<Int> =
        _carrito
            .map { it.contarPlatos() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = 0
            )

    private val _mensaje = MutableStateFlow("")
    val mensaje: StateFlow<String> = _mensaje.asStateFlow()

    private val _mostrarMensaje = MutableStateFlow(false)
    val mostrarMensaje: StateFlow<Boolean> =
        _mostrarMensaje.asStateFlow()

    private var trabajoMensaje: Job? = null

    private val _checkout =
        MutableStateFlow(CheckoutUiState())

    val checkout: StateFlow<CheckoutUiState> =
        _checkout.asStateFlow()

    val historial: StateFlow<List<Pedido>> =
        pedidoRepository.observarPedidos()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private val _pedidoSeleccionadoId =
        MutableStateFlow<Int?>(null)

    val pedidoSeleccionadoId: StateFlow<Int?> =
        _pedidoSeleccionadoId.asStateFlow()

    val pedidoSeleccionado: StateFlow<Pedido?> =
        combine(
            _pedidoSeleccionadoId,
            pedidoRepository.observarPedidos()
        ) { id, pedidos ->
            pedidos.firstOrNull { it.id == id }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    /**
     * Agrega una combinación de proteína y acompañamientos.
     *
     * Las combinaciones iguales aumentan su cantidad.
     * Las combinaciones diferentes se mantienen separadas.
     * La ensalada mix está incluida en el precio.
     */
    fun agregarPlato(
        plato: MenuItem,
        acompanamientos: List<String> = emptyList()
    ) {
        val agregadosUnicos = acompanamientos.distinct()

        val seleccionValida =
            agregadosUnicos.size in
                    MenuCasaJojo.MIN_AGREGADOS..
                    MenuCasaJojo.MAX_AGREGADOS &&
                    agregadosUnicos.all {
                        it in MenuCasaJojo.AGREGADOS
                    }

        if (!seleccionValida) {
            mostrarMensajeTemporal(
                "Selecciona uno o dos acompañamientos válidos."
            )
            return
        }

        // Ordenar evita duplicar una combinación cuando los agregados
        // se seleccionan en un orden diferente.
        val agregadosOrdenados = agregadosUnicos.sorted()

        _carrito.update { actual ->

            val indiceExistente = actual.indexOfFirst { linea ->
                linea.menuItemId == plato.id &&
                        linea.nombrePlato == plato.nombre &&
                        linea.precioUnitario == plato.precio &&
                        linea.agregados.sorted() == agregadosOrdenados
            }

            if (indiceExistente >= 0) {
                actual.mapIndexed { indice, linea ->
                    if (indice == indiceExistente) {
                        linea.copy(
                            cantidad = linea.cantidad + 1
                        )
                    } else {
                        linea
                    }
                }
            } else {
                actual + DetallePedido(
                    menuItemId = plato.id,
                    nombrePlato = plato.nombre,
                    precioUnitario = plato.precio,
                    cantidad = 1,
                    agregados = agregadosOrdenados
                )
            }
        }

        limpiarErrorCarrito()

        mostrarMensajeTemporal(
            "Agregaste ${plato.nombre} al carrito."
        )
    }

    /**
     * Aumenta la cantidad de una combinación específica.
     * Utilizado por CarritoScreen mediante lineaId.
     */
    fun aumentarCantidad(lineaId: String) {
        actualizarCantidadPorLinea(
            lineaId = lineaId,
            cambio = 1
        )
    }

    /**
     * Disminuye la cantidad de una combinación específica.
     * Si llega a cero, elimina esa línea del carrito.
     */
    fun disminuirCantidad(lineaId: String) {
        actualizarCantidadPorLinea(
            lineaId = lineaId,
            cambio = -1
        )
    }

    /**
     * Elimina exclusivamente la combinación identificada.
     */
    fun quitarPlato(lineaId: String) {
        _carrito.update { actual ->
            actual.filterNot {
                it.lineaId == lineaId
            }
        }

        limpiarErrorCarrito()
    }

    private fun actualizarCantidadPorLinea(
        lineaId: String,
        cambio: Int
    ) {
        _carrito.update { actual ->
            actual.mapNotNull { linea ->

                if (linea.lineaId != lineaId) {
                    linea
                } else {
                    val nuevaCantidad =
                        linea.cantidad + cambio

                    if (nuevaCantidad > 0) {
                        linea.copy(
                            cantidad = nuevaCantidad
                        )
                    } else {
                        null
                    }
                }
            }
        }

        limpiarErrorCarrito()
    }

    /**
     * Sobrecargas conservadas por compatibilidad con otras pantallas
     * del proyecto que todavía utilizan el ID del producto.
     */
    fun aumentarCantidad(menuItemId: Int) {
        actualizarCantidadPorMenuItem(
            menuItemId = menuItemId,
            nombrePlato = null,
            cambio = 1
        )
    }

    fun aumentarCantidad(
        menuItemId: Int,
        nombrePlato: String
    ) {
        actualizarCantidadPorMenuItem(
            menuItemId = menuItemId,
            nombrePlato = nombrePlato,
            cambio = 1
        )
    }

    fun disminuirCantidad(menuItemId: Int) {
        actualizarCantidadPorMenuItem(
            menuItemId = menuItemId,
            nombrePlato = null,
            cambio = -1
        )
    }

    fun disminuirCantidad(
        menuItemId: Int,
        nombrePlato: String
    ) {
        actualizarCantidadPorMenuItem(
            menuItemId = menuItemId,
            nombrePlato = nombrePlato,
            cambio = -1
        )
    }

    fun quitarPlato(menuItemId: Int) {
        _carrito.update { actual ->
            actual.filterNot {
                it.menuItemId == menuItemId
            }
        }

        limpiarErrorCarrito()
    }

    fun quitarPlato(
        menuItemId: Int,
        nombrePlato: String
    ) {
        _carrito.update { actual ->
            actual.filterNot {
                it.menuItemId == menuItemId &&
                        it.nombrePlato == nombrePlato
            }
        }

        limpiarErrorCarrito()
    }

    private fun actualizarCantidadPorMenuItem(
        menuItemId: Int,
        nombrePlato: String?,
        cambio: Int
    ) {
        _carrito.update { actual ->
            actual.mapNotNull { linea ->

                val coincide =
                    linea.menuItemId == menuItemId &&
                            (
                                    nombrePlato == null ||
                                            linea.nombrePlato == nombrePlato
                                    )

                if (!coincide) {
                    linea
                } else {
                    val nuevaCantidad =
                        linea.cantidad + cambio

                    if (nuevaCantidad > 0) {
                        linea.copy(
                            cantidad = nuevaCantidad
                        )
                    } else {
                        null
                    }
                }
            }
        }

        limpiarErrorCarrito()
    }

    fun onNombreChange(valor: String) {
        _checkout.update {
            it.copy(
                nombre = valor,
                errores = it.errores.copy(
                    nombre = null
                )
            )
        }
    }

    fun onRetiroLoAntesPosibleChange(valor: Boolean) {
        _checkout.update {
            it.copy(
                retiroLoAntesPosible = valor,
                errores = it.errores.copy(
                    horaRetiro = null
                )
            )
        }
    }

    fun onHoraRetiroSeleccionada(
        hora: Int,
        minuto: Int
    ) {
        val texto =
            hora.toString().padStart(2, '0') + ":" +
                    minuto.toString().padStart(2, '0')

        _checkout.update {
            it.copy(
                horaRetiro = texto,
                errores = it.errores.copy(
                    horaRetiro = null
                )
            )
        }
    }

    fun onRestriccionesChange(valor: String) {
        _checkout.update {
            it.copy(
                restricciones = valor,
                errores = it.errores.copy(
                    restricciones = null
                )
            )
        }
    }

    /**
     * Valida y registra el pedido.
     * Si el registro falla, conserva el carrito para reintentar.
     */
    fun confirmarPedido() {
        val estado = _checkout.value

        if (estado.guardando) return

        val carritoActual = _carrito.value

        val idsDisponibles = menuDisponible.value
            .orEmpty()
            .map { it.id }
            .toSet()

        val errores = PedidoValidator.validarCheckout(
            nombre = estado.nombre,
            retiroLoAntesPosible =
                estado.retiroLoAntesPosible,
            horaRetiro = estado.horaRetiro,
            restricciones = estado.restricciones,
            carrito = carritoActual,
            idsDisponibles = idsDisponibles,
            horaActual = LocalTime.now()
        )

        if (errores.tieneErrores) {
            _checkout.update {
                it.copy(
                    errores = errores,
                    errorGuardado = null
                )
            }
            return
        }

        val pedido = Pedido(
            clienteNombre = estado.nombre.trim(),
            detalleItems = carritoActual.aTextoDetalle(),
            restricciones = estado.restricciones.trim(),
            horaComprometida =
                if (estado.retiroLoAntesPosible) {
                    PedidoRepository.RETIRO_LO_ANTES_POSIBLE
                } else {
                    estado.horaRetiro.orEmpty()
                },
            total = carritoActual.calcularTotal()
        )

        _checkout.update {
            it.copy(
                guardando = true,
                errores = CheckoutErrores(),
                errorGuardado = null
            )
        }

        viewModelScope.launch {
            try {
                val idNuevo =
                    pedidoRepository.registrar(pedido)

                _pedidoSeleccionadoId.value = idNuevo
                _carrito.value = emptyList()

                _checkout.value = CheckoutUiState(
                    pedidoConfirmadoId = idNuevo
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _checkout.update {
                    it.copy(
                        guardando = false,
                        errorGuardado =
                            "No se pudo guardar tu pedido. " +
                                    "Tu carrito se mantiene; " +
                                    "intenta nuevamente."
                    )
                }
            }
        }
    }

    fun confirmacionAtendida() {
        _checkout.update {
            it.copy(
                pedidoConfirmadoId = null
            )
        }
    }

    fun seleccionarPedido(id: Int) {
        _pedidoSeleccionadoId.value = id
    }

    private fun limpiarErrorCarrito() {
        _checkout.update {
            it.copy(
                errores = it.errores.copy(
                    carrito = null
                )
            )
        }
    }

    private fun mostrarMensajeTemporal(texto: String) {
        trabajoMensaje?.cancel()

        _mensaje.value = texto
        _mostrarMensaje.value = true

        trabajoMensaje = viewModelScope.launch {
            delay(2000)
            _mostrarMensaje.value = false
        }
    }
}

class ClienteViewModelFactory(
    private val menuRepository: MenuRepository,
    private val pedidoRepository: PedidoRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (
            modelClass.isAssignableFrom(
                ClienteViewModel::class.java
            )
        ) {
            @Suppress("UNCHECKED_CAST")
            return ClienteViewModel(
                menuRepository,
                pedidoRepository
            ) as T
        }

        throw IllegalArgumentException(
            "ViewModel desconocido: ${modelClass.name}"
        )
    }
}
