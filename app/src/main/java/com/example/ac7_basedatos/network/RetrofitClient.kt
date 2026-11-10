package com.example.ac7_basedatos.network
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//archivo que construye la URL y conecta la API
object RetrofitClient {
    private const val BASE_URL = "https://dummyjson.com/"
    val api: DummyApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DummyApi::class.java)
    }
}