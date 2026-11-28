package com.example.casajojo.data.network

import retrofit2.http.*

interface PedidoApiService {
    @GET("api/pedidos")
    suspend fun obtenerPedidos(): List<PedidoDto>

    @POST("api/pedidos")
    suspend fun crearPedido(@Body pedido: PedidoDto): PedidoDto

    @PATCH("api/pedidos/{id}/estado")
    suspend fun actualizarEstado(
        @Path("id") id: Int,
        @Query("estado") estado: String
    ): PedidoDto

    @PATCH("api/pedidos/{id}/pago")
    suspend fun actualizarPago(
        @Path("id") id: Int,
        @Query("pagado") pagado: Boolean
    ): PedidoDto
}