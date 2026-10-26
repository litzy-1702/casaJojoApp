package com.example.casajojo.data.local

import androidx.room.TypeConverter
import com.example.casajojo.feature.operations.EstadoPedido

class Converters {
    @TypeConverter
    fun fromEstadoPedido(estado: EstadoPedido): String = estado.name

    @TypeConverter
    fun toEstadoPedido(value: String): EstadoPedido = try {
        EstadoPedido.valueOf(value)
    } catch (e: Exception) {
        EstadoPedido.RECIBIDO
    }
}