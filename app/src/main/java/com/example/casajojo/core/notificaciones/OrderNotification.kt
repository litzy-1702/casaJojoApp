package com.example.casajojo.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.casajojo.R
import com.example.casajojo.feature.operations.EstadoPedido

class OrderNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "canal_comandas_casajojo"
        const val CHANNEL_NAME = "Notificaciones de Comandas"
    }

    init {
        crearCanalNotificacion()
    }

    private fun crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisos de cambio de estado en comandas de La Casa JoJo"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun notificarCambioEstado(pedidoId: Int, clienteNombre: String, nuevoEstado: EstadoPedido) {
        val titulo = "Comanda #$pedidoId - $clienteNombre"
        val mensaje = when (nuevoEstado) {
            EstadoPedido.RECIBIDO -> "Nueva comanda ingresada en caja."
            EstadoPedido.EN_PREPARACION -> "Cocina comenzó a preparar el pedido."
            EstadoPedido.LISTO -> "¡El pedido está LISTO para retiro!"
            EstadoPedido.ENTREGADO -> "Pedido entregado al cliente."
            EstadoPedido.CANCELADO -> "El pedido ha sido cancelado."
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(pedidoId, builder.build())
    }
}