package com.example.casajojo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.casajojo.feature.operations.EstadoPedido

@Entity(tableName = "pedidos")
data class Pedido(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val clienteNombre: String,
    val clienteTelefono: String = "",
    val detalleItems: String, // ejemplo: 1x menu cazuela, 1x ensalada mixta, 1x flan
    val restricciones: String = "", // ejemplo: sin sal o sin cebolla
    val horaCreacion: Long = System.currentTimeMillis(), // timestamp en milisegundos
    val horaComprometida: String, // formato hh:mm por ejemplo: 13:30
    val total: Int,
    val estado: EstadoPedido = EstadoPedido.RECIBIDO,
    val pagado: Boolean = false,
    val metodoPago: String = "Efectivo"
)