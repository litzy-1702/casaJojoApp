package com.example.casajojo.data.external

class ExternalMealRepository(
    private val api: MealApiService = MealRetrofitInstance.api
) {
    suspend fun obtenerSugerencias(query: String = "chicken"): Result<List<MealDto>> {
        return try {
            val response = api.buscarPlatos(query)
            Result.success(response.meals?.take(4) ?: emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}