package com.example.casajojo.feature.operations

enum class EstadoPedido(val etiqueta: String) {
    RECIBIDO("Recibido"),
    EN_PREPARACION("En Preparación"),
    LISTO("Listo para Retiro"),
    ENTREGADO("Entregado"),
    CANCELADO("Cancelado")
}