package com.example.casajojo.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val MENU_BASE_URL = "http://10.0.2.2:8081/"
    private const val PEDIDO_BASE_URL = "http://10.0.2.2:8082/"

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val menuApi: MenuApiService by lazy {
        Retrofit.Builder()
            .baseUrl(MENU_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MenuApiService::class.java)
    }

    val pedidoApi: PedidoApiService by lazy {
        Retrofit.Builder()
            .baseUrl(PEDIDO_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PedidoApiService::class.java)
    }
}