package com.example.casajojo.data.local

import androidx.room.*
import com.example.casajojo.data.model.Pedido
import com.example.casajojo.feature.operations.EstadoPedido
import kotlinx.coroutines.flow.Flow

@Dao
interface PedidoDao {
    @Query("SELECT * FROM pedidos ORDER BY horaCreacion DESC")
    fun observarTodos(): Flow<List<Pedido>>

    @Query("SELECT * FROM pedidos WHERE estado = :estado ORDER BY horaCreacion ASC")
    fun observarPorEstado(estado: EstadoPedido): Flow<List<Pedido>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(pedido: Pedido): Long

    @Update
    suspend fun actualizar(pedido: Pedido)

    @Query("UPDATE pedidos SET estado = :nuevoEstado WHERE id = :id")
    suspend fun actualizarEstado(id: Int, nuevoEstado: EstadoPedido)

    @Query("UPDATE pedidos SET pagado = :pagado WHERE id = :id")
    suspend fun actualizarPago(id: Int, pagado: Boolean)

    @Delete
    suspend fun eliminar(pedido: Pedido)
}