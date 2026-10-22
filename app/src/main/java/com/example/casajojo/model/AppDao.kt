package com.example.casajojo.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM menu")
    fun obtenerMenu(): Flow<List<MenuEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPlato(plato: MenuEntity)

    @Query("SELECT * FROM pedidos ORDER BY id DESC")
    fun obtenerPedidos(): Flow<List<PedidoEntity>>

    @Insert
    suspend fun crearPedido(pedido: PedidoEntity)

    @Query("UPDATE pedidos SET estado = :nuevoEstado WHERE id = :pedidoId")
    suspend fun actualizarEstadoPedido(pedidoId: Int, nuevoEstado: String)
}