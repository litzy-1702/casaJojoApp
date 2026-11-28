package com.example.casajojo.data.external

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object MealRetrofitInstance {

    private const val BASE_URL = "https://www.themealdb.com/"

    val api: MealApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MealApiService::class.java)
    }
}