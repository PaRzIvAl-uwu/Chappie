package com.example.ac7_basedatos

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide

class DetailsActivity : AppCompatActivity() {

    private lateinit var imgProductoDetalle: ImageView
    private lateinit var tvNombreProductoDetalle: TextView
    private lateinit var tvMarcaDetalle: TextView
    private lateinit var tvPrecioDetalle: TextView
    private lateinit var btnFavorito: ImageButton

    private lateinit var dbHelper: DatabaseHelper

    private var productoFavorito = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_details)

        imgProductoDetalle = findViewById(R.id.imgProductoDetalle)
        tvNombreProductoDetalle = findViewById(R.id.tvNombreProductoDetalle)
        tvMarcaDetalle = findViewById(R.id.tvMarcaDetalle)
        tvPrecioDetalle = findViewById(R.id.tvPrecioDetalle)
        btnFavorito = findViewById(R.id.btnFavorito)

        dbHelper = DatabaseHelper(this)

        mostrarInformacionProducto()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private fun mostrarInformacionProducto(){
        val idProducto = intent.getIntExtra("ID_PRODUCTO", 1)
        val nombreProducto = intent.getStringExtra("NOMBRE_PRODUCTO") ?: "Producto desconocido"
        val marcaProducto = intent.getStringExtra("MARCA_PRODUCTO") ?: "Sin marca"
        val precioProducto = intent.getDoubleExtra("PRECIO_PRODUCTO", 0.0)
        val imagen = intent.getStringExtra("IMAGEN_PRODUCTO") ?: ""

        tvNombreProductoDetalle.text = nombreProducto
        tvMarcaDetalle.text = marcaProducto ?: "Sin marca"
        tvPrecioDetalle.text = String.format("$%.2f", precioProducto)

        Glide.with(this)
            .load(imagen)
            .centerCrop()
            .into(imgProductoDetalle)

        //verificar que el album se encuentre como favorito
        if(idProducto != -1){
            dbHelper.existeProductoFavorito(idProducto)
        }

        //se verifica desde la base de datos
        productoFavorito = dbHelper.existeProductoFavorito(idProducto)
        actualizarIcono()

        btnFavorito.setOnClickListener {
            //productoFavorito = !productoFavorito

            if(productoFavorito) {

                val eliminado = dbHelper.eliminarProductoFavorito(idProducto)

                if(eliminado ){

                    productoFavorito = false
                    Toast.makeText(this, "Producto $nombreProducto eliminado de favoritos", Toast.LENGTH_SHORT).show()
                    btnFavorito.setImageResource(R.drawable.no_favorito)
                }

            }else{
                val guardado = dbHelper.guardarProductoFavorito(
                    idProducto = idProducto,
                    nombre= nombreProducto,
                    marca = marcaProducto,
                    precio =  precioProducto,
                    imagen = imagen
                )

                if(guardado){
                    productoFavorito = true
                    btnFavorito.setImageResource(R.drawable.favorito)
                    Toast.makeText(this, "Producto $nombreProducto agregado a favoritos", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this, "Producto $nombreProducto ya existe en favoritos", Toast.LENGTH_SHORT).show()
                }

            }
            actualizarIcono()
        }
    }

    private fun actualizarIcono(){
        if(productoFavorito){
            btnFavorito.setImageResource(R.drawable.favorito)
        }else{
            btnFavorito.setImageResource(R.drawable.no_favorito)
        }
    }
}