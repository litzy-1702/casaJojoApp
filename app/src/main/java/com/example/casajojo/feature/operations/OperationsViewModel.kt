package com.example.casajojo.feature.operations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.casajojo.core.notifications.OrderNotificationManager
import com.example.casajojo.data.local.PedidoDao
import com.example.casajojo.data.model.Pedido
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class OperationsViewModel(
    private val pedidoDao: PedidoDao,
    private val notificationManager: OrderNotificationManager
) : ViewModel() {

    val pedidos: StateFlow<List<Pedido>> = pedidoDao.observarTodos().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun cambiarEstadoPedido(pedido: Pedido, nuevoEstado: EstadoPedido) {
        viewModelScope.launch {
            pedidoDao.actualizarEstado(pedido.id, nuevoEstado)
            notificationManager.notificarCambioEstado(pedido.id, pedido.clienteNombre, nuevoEstado)
        }
    }

    fun cambiarEstadoPago(pedidoId: Int, pagado: Boolean) {
        viewModelScope.launch {
            pedidoDao.actualizarPago(pedidoId, pagado)
        }
    }


    //la dema para simular
    fun crearPedidoDemo() {
        viewModelScope.launch {
            val horaActual = Calendar.getInstance()
            val horaComprometidaMs = horaActual.timeInMillis + (20 * 60 * 1000) // +20 min
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())

            val demo = Pedido(
                clienteNombre = "Cliente Demo #${(10..99).random()}",
                clienteTelefono = "+56987654321",
                detalleItems = "1x Menú Ejecutivo Cazuela, 1x Ensalada Mixta, 1x Postre Flan",
                restricciones = if (listOf(true, false).random()) "Alergia al maní, sin sal en ensalada" else "",
                horaCreacion = System.currentTimeMillis(),
                horaComprometida = sdf.format(Date(horaComprometidaMs)),
                total = 6500,
                estado = EstadoPedido.RECIBIDO,
                pagado = false
            )
            pedidoDao.insertar(demo)
        }
    }


    //calcula si el pedido esta atrasado comparando la hora actual con la hora solicitada
    fun estaAtrasado(pedido: Pedido): Boolean {
        if (pedido.estado == EstadoPedido.ENTREGADO || pedido.estado == EstadoPedido.CANCELADO) return false

        // si es lo antes posible, ver si han pasado más de 20 minutos desde su creacion
        if (pedido.horaComprometida == "Lo antes posible") {
            val tiempoLimiteMs = 20 * 60 * 1000 // 20 minutos
            return (System.currentTimeMillis() - pedido.horaCreacion) > tiempoLimiteMs
        }

        // Para horas específicas (ejemplo 14:30)
        return try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val horaActualStr = sdf.format(Date())
            val ahora = sdf.parse(horaActualStr)
            val comprometida = sdf.parse(pedido.horaComprometida)

            if (ahora != null && comprometida != null) {
                ahora.after(comprometida)
            } else false
        } catch (e: Exception) {
            false
        }
    }

    fun formatearHora(timestampMs: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestampMs))
    }
}

class OperationsViewModelFactory(
    private val pedidoDao: PedidoDao,
    private val notificationManager: OrderNotificationManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OperationsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OperationsViewModel(pedidoDao, notificationManager) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}