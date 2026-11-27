package com.casajojo.pedidoservice.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

/**
 * Pedido de un cliente.
 * Tiene los mismos campos que Pedido de la app Android.
 */
@Entity
@Table(name = "pedidos")
class Pedido(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(nullable = false)
    var clienteNombre: String = "",

    // Solo dígitos; puede venir vacío
    @Column(nullable = false, length = 12)
    var clienteTelefono: String = "",

    // Ejemplo: "1x Pollo asado | Agregados: Arroz normal, Puré natural | Ensalada incluida: Mix de ensaladas"
    @Column(nullable = false, length = 2000)
    var detalleItems: String = "",

    @Column(nullable = false, length = 150)
    var restricciones: String = "",

    // Timestamp en milisegundos
    @Column(nullable = false)
    var horaCreacion: Long = System.currentTimeMillis(),

    // "HH:mm" o "Lo antes posible"
    @Column(nullable = false)
    var horaComprometida: String = "",

    @Column(nullable = false)
    var total: Int = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var estado: EstadoPedido = EstadoPedido.RECIBIDO,

    @Column(nullable = false)
    var pagado: Boolean = false,

    @Column(nullable = false)
    var metodoPago: String = "Efectivo"
)
