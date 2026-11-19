package com.example.ac7_basedatos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.ac7_basedatos.model.Producto
import com.example.ac7_basedatos.model.ProductosResponse
import com.example.ac7_basedatos.network.RetrofitClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {
    private lateinit var tvEstado: TextView
    private lateinit var contenedorSmartphones: LinearLayout
    private lateinit var contenedorLaptops: LinearLayout
    private lateinit var contenedorRelojes: LinearLayout

    private lateinit var bottomNavigation: BottomNavigationView

    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager : CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvEstado = findViewById(R.id.tvEstado)
        contenedorSmartphones = findViewById(R.id.contenedorSmartphones)
        contenedorLaptops = findViewById(R.id.contenedorLaptops)
        contenedorRelojes = findViewById(R.id.contenedorRelojes)

        bottomNavigation = findViewById(R.id.bottomNavigation)

        auth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)

        obtenerProductos("smartphones", contenedorSmartphones)
        obtenerProductos("laptops", contenedorLaptops)
        obtenerProductos("mens-watches", contenedorRelojes)
    }

    // metodo que consulta el api
    private fun obtenerProductos(categoria: String, contenedor: LinearLayout) {
        //llamamos a la api
        RetrofitClient.api.obtenerPorCategoria(categoria, 10)

            .enqueue(object : Callback<ProductosResponse> {

                // se ejecuta cuando el api si responde
                override fun onResponse(call: Call<ProductosResponse>, response: Response<ProductosResponse>) {

                    //products la lista de productos
                    val productos = response.body()?.products ?: emptyList()

                    if (productos.isNotEmpty()) {
                        tvEstado.text = "Ofertas disponibles"
                        mostrarProductos(productos, contenedor) // pintamos las tarjetas
                    } else {
                        // La API contestó pero esa categoría no trajo nada.
                        Toast.makeText(this@HomeActivity, "No hay resultados en $categoria", Toast.LENGTH_SHORT).show()
                    }
                }

                // cuando el api no da respuestas
                override fun onFailure(call: Call<ProductosResponse>, t: Throwable) {
                    tvEstado.text = "Error en el API"
                    Toast.makeText(this@HomeActivity, "Error en el API", Toast.LENGTH_SHORT).show()
                }
            })
    }
    // metodo q muestra la lista de productos
    private fun mostrarProductos(productos: List<Producto>, contenedor: LinearLayout) {
        contenedor.removeAllViews()
        for (producto in productos) {

            //inflar crea una tarjeta nueva a partir del diseño item_producto
            val vista = layoutInflater.inflate(R.layout.item_producto, contenedor, false)

            val imgProducto = vista.findViewById<ImageView>(R.id.imgProducto)
            val tvNombre = vista.findViewById<TextView>(R.id.tvNombreProducto)
            val tvPrecio = vista.findViewById<TextView>(R.id.tvPrecio)
            val tvDescuento = vista.findViewById<TextView>(R.id.tvDescuento)

            tvNombre.text = producto.title ?: "Producto sin nombre"
            tvPrecio.text = String.format("$%.2f", producto.precioFinal())
            tvDescuento.text = String.format("%.0f%% de descuento", producto.discountPercentage ?: 0.0)

            //glide descarga la imagen desde la url
            Glide.with(this)
                .load(producto.thumbnail)
                .into(imgProducto)
            //pasar informacion a otra pantalla
            vista.setOnClickListener {
                val nombre = producto.title
                if(nombre != null){
                    val intent = Intent(this@HomeActivity, DetailsActivity::class.java)
                    intent.putExtra("ID_PRODUCTO", producto.id)
                    intent.putExtra("NOMBRE_PRODUCTO", nombre)
                    intent.putExtra("MARCA_PRODUCTO", producto.brand)
                    intent.putExtra("PRECIO_PRODUCTO", producto.precioFinal())
                    intent.putExtra("IMAGEN_PRODUCTO", producto.thumbnail)
                    startActivity(intent)

                }
            }

            contenedor.addView(vista)
        }
    }

    //MÉTODO PARA USAR EL MENU DE NAVEGACIÓN
    private fun menuNavegacion(){
        //indicar cual es el item seleccionado
        bottomNavigation.selectedItemId = R.id.nav_inicio

        bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.nav_inicio ->{
                    true
                }
                R.id.nav_favoritos ->{
                    val intent = Intent(this, FavoriteActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_salir ->{
                    cerrarSesionGoogle()
                    true

                }
                else -> false
            }
        }
    }

    //metodo para cerrar sesion
    private fun cerrarSesionGoogle(){
        auth.signOut()

        lifecycleScope.launch {
            try{
                credentialManager.clearCredentialState(
                    androidx.credentials.ClearCredentialStateRequest()
                )
            }catch (e: Exception){
                Toast.makeText(this@HomeActivity, "Error al cerrar sesion", Toast.LENGTH_SHORT).show()
            }
            val intent= Intent(this@HomeActivity, MainActivity::class.java)

            //indicar que se borre toda la info y la aplicacion comience desde 0
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
        }
    }
}
