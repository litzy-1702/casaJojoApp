package com.example.casajojo.data.repository

import com.example.casajojo.data.local.PedidoDao
import com.example.casajojo.data.model.Pedido
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de pedidos del módulo Cliente (Integrante 2).
 *
 * Es el intermediario entre el ClienteViewModel y Room. Reutiliza el
 * PedidoDao existente (creado por el Integrante 3) sin modificarlo.
 */
class PedidoRepository(
    private val dao: PedidoDao
) {

    /**
     * Pedidos guardados en este dispositivo, del más reciente al más
     * antiguo. Es un Flow: la interfaz se actualiza sola cuando Caja o
     * Cocina cambian el estado de un pedido.
     */
    fun observarPedidos(): Flow<List<Pedido>> {
        return dao.observarTodos()
    }

    /**
     * Guarda un pedido nuevo en Room y devuelve el id generado.
     */
    suspend fun registrar(pedido: Pedido): Int {
        return dao.insertar(pedido).toInt()
    }

    companion object {
        /**
         * PENDIENTE DE ACUERDO CON EL INTEGRANTE 3.
         *
         * Valor de Pedido.horaComprometida cuando el cliente elige retirar
         * "lo antes posible" (el campo no acepta null).
         *
         * Efecto actual en OperationsViewModel.estaAtrasado(): no puede
         * leer este texto como HH:mm, captura el error y devuelve false.
         * La app no se cae, pero este pedido nunca se marca "atrasado",
         * porque no tiene una hora comprometida. Caja y Cocina lo siguen
         * viendo ordenado por hora de creación.
         *
         * Si el equipo decide otra representación, solo cambia esta
         * constante.
         */
        const val RETIRO_LO_ANTES_POSIBLE = "Lo antes posible"
    }
}
