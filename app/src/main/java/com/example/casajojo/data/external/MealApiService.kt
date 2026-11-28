package com.example.casajojo.data.external

import retrofit2.http.GET
import retrofit2.http.Query

interface MealApiService {

    // Endpoint público gratuito de TheMealDB para obtener sugerencias de platos
    @GET("api/json/v1/1/search.php")
    suspend fun buscarPlatos(@Query("s") query: String = "chicken"): MealResponse
}