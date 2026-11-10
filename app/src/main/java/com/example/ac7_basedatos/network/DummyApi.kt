package com.example.ac7_basedatos.network
import com.example.ac7_basedatos.model.ProductosResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

//archivo para conexion con internet
interface DummyApi {
    @GET("products/search")
    fun buscarProductos(
        @Query("q") busqueda: String,
        @Query("limit") limite: Int
    ): Call<ProductosResponse>

    @GET("products/category/{categoria}")
    fun obtenerPorCategoria(
        // @Path reemplaza {categoria} por el texto que le mandemos.
        // Si mandamos "laptops", la ruta queda products/category/laptops
        @Path("categoria") categoria: String,
        // igual que antes: limita la cantidad de productos
        @Query("limit") limite: Int
    ): Call<ProductosResponse>
}