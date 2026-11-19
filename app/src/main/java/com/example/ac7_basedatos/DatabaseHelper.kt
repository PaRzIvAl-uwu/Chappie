package com.example.ac7_basedatos

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.ac7_basedatos.model.Producto

class DatabaseHelper(context: Context) : SQLiteOpenHelper(

    context,
    DATABASE_NAME   ,
    null,
    DATABASE_VERSION,

    ) {
    companion object{
        private const val DATABASE_NAME = "chappie.db"
        private const val DATABASE_VERSION = 2
        private const val TABLE_USUARIOS = "usuarios"

        private const val COL_ID = "id"
        private const val COL_NOMBRE = "nombre"
        private const val COL_CORREO = "correo"
        private const val COL_PASSWORD = "password"

        //TABLA FAVORITOS
        private const val TABLE_FAVORITOS = "favoritos"
        private const val COL_ID_FAVORITO = "id"
        private const val COL_ID_PRODUCTO = "producto_id"
        private const val COL_NOMBRE_PRODUCTO = "nombre_producto"
        private const val COL_MARCA = "marca"
        private const val COL_PRECIO = "precio"
        private const val COL_IMAGEN = "imagen"

        private const val CREATE_TABLE_FAVORITOS = """
            CREATE TABLE $TABLE_FAVORITOS(
            $COL_ID_FAVORITO INTEGER PRIMARY KEY AUTOINCREMENT,
            $COL_ID_PRODUCTO INTEGER UNIQUE NOT NULL,
            $COL_NOMBRE_PRODUCTO TEXT NOT NULL,
            $COL_MARCA TEXT,
            $COL_PRECIO REAL,
            $COL_IMAGEN TEXT
            )
        """
        private const val CREATE_TABLE_USUARIOS = """
            CREATE TABLE $TABLE_USUARIOS(
            $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, 
            $COL_NOMBRE TEXT NOT NULL,
            $COL_CORREO TEXT NOT NULL UNIQUE,
            $COL_PASSWORD TEXT NOT NULL)
        """

    }

    //metodo para crear la base de datos y sus tablas
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_TABLE_USUARIOS)
        db?.execSQL(CREATE_TABLE_FAVORITOS)
    }

    //metodo para actualizar la base de datos
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        //validar la version actual de la BD
        if(oldVersion < 2){
            db?.execSQL(CREATE_TABLE_FAVORITOS)
        }
    }

    //metodo para registrar un usuario (insertar)
    fun registrarUsuario(nombre: String, correo: String, password: String) : Boolean{
        //crear variable con permisos para escribir en la DB
        val db = writableDatabase

        //crear variable para guardar la informacion
        val datos = ContentValues().apply {
            put(COL_NOMBRE, nombre)
            put(COL_CORREO, correo)
            put(COL_PASSWORD, password)
        }
        val resultado = db.insert(TABLE_USUARIOS, null, datos)

        return resultado != -1L
    }

    //metodo para consulta de usuarios
    fun inciarSesion(correo: String, password: String): Boolean{
        val db = readableDatabase

        val query_sql = """
            SELECT * FROM $TABLE_USUARIOS
            WHERE $COL_CORREO = ? AND $COL_PASSWORD = ?
            
        """.trimIndent()

        val cursor = db.rawQuery(query_sql, arrayOf(correo,password))
        //contar el numero de resultados encontrados
        val usuarioEncontrado = cursor.count > 0

        cursor.close()
        db.close()
        return usuarioEncontrado
    }

    //METODO PARA INSERTAR UN REGISTRO DESDE EL API
    fun guardarProductoFavorito(idProducto: Int, nombre : String, marca : String, precio : Double, imagen : String) : Boolean {
        val db = writableDatabase
        val datos = ContentValues().apply {
            put(COL_ID_PRODUCTO, idProducto)
            put(COL_NOMBRE_PRODUCTO, nombre)
            put(COL_MARCA, marca)
            put(COL_PRECIO, precio)
            put(COL_IMAGEN, imagen)
        }
        val resultado = db.insertWithOnConflict(
            TABLE_FAVORITOS,
            null,
            datos,
            SQLiteDatabase.CONFLICT_IGNORE
        )
        return resultado != -1L
    }

    //METODO PARA ELIMINAR UN REGISTRO
    fun eliminarProductoFavorito(idProducto : Int) : Boolean{
        val db = writableDatabase
        val resultado = db.delete(
            TABLE_FAVORITOS,
            "$COL_ID_PRODUCTO = ?",
            arrayOf(idProducto.toString())
        )
        return resultado > 0
    }

    //MÉTODO PARA CONSULTAR LA INFORMACION DEL API (guardada en la base de datos)
    fun existeProductoFavorito(idProducto : Int) : Boolean{
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_ID_FAVORITO FROM $TABLE_FAVORITOS WHERE $COL_ID_PRODUCTO = ?",
            arrayOf(idProducto.toString())
        )
        val existe = cursor.moveToFirst()
        cursor.close()
        return existe
    }
}