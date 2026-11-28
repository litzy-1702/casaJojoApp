package com.example.casajojo.data.network

import retrofit2.http.*

interface MenuApiService {
    @GET("api/menu")
    suspend fun obtenerMenuPorFecha(@Query("fecha") fecha: String): List<MenuDto>

    @POST("api/menu")
    suspend fun crearPlato(@Body plato: MenuDto): MenuDto

    @PUT("api/menu/{id}")
    suspend fun actualizarPlato(@Path("id") id: Int, @Body plato: MenuDto): MenuDto

    @PATCH("api/menu/{id}/disponibilidad")
    suspend fun cambiarDisponibilidad(
        @Path("id") id: Int,
        @Query("disponible") disponible: Boolean
    ): MenuDto

    @DELETE("api/menu/{id}")
    suspend fun eliminarPlato(@Path("id") id: Int)
}