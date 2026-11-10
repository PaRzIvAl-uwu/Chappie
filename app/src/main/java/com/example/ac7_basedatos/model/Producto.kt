package com.example.ac7_basedatos.model

//archivo para detectar las respuestas del API (DummyJSON)
data class ProductosResponse( //representa toda la respuesta de la API
    val products: List<Producto>,
    val total: Int
)

data class Producto(
    val id: Int,
    val title: String?,
    val category: String?,
    val price: Double?,
    val discountPercentage: Double?,
    val rating: Double?,
    val brand: String?,
    val thumbnail: String?
) {
    //metodo para calcular el precio ya con el descuento aplicado
    fun precioFinal(): Double {
        val precio = price ?: 0.0
        val descuento = discountPercentage ?: 0.0
        return precio * (1 - descuento / 100)
    }
}