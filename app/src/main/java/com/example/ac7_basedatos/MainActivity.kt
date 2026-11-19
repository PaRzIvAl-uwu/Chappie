package com.example.ac7_basedatos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnIniciar: Button

    private lateinit var dbHelper: DatabaseHelper

    private lateinit var auth : FirebaseAuth
    private lateinit var credentialManager : CredentialManager
    private lateinit var btnGoogle : ImageButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val tvCrearCuenta = findViewById<TextView>(R.id.tvCrearCuenta)

        etEmail = findViewById<EditText>(R.id.etEmail)
        etPassword = findViewById<EditText>(R.id.etPassword)
        btnIniciar = findViewById<Button>(R.id.btnIniciar)

        dbHelper = DatabaseHelper(this)

        btnGoogle = findViewById(R.id.btnGoogle)
        auth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)


        //crear evento para iniciar sesion
        btnIniciar.setOnClickListener {
            //TODO: agregar el metodo de inicio de sesion
            iniciarSesion()
        }

        btnGoogle.setOnClickListener {
            iniciarSesionGoogle()
        }

        //crear evento para ir a otra pantalla
        tvCrearCuenta.setOnClickListener {
            val intent = Intent(this, Activity_Register::class.java)
            startActivity(intent)
        }

    }

    private fun iniciarSesion(){

        val correo = etEmail.text.toString()
        val pass = etPassword.text.toString()

        if (correo.isEmpty() || pass.isEmpty()){
            Toast.makeText(this, "todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val resultado = dbHelper.inciarSesion(correo, pass)
        if(resultado){
            Toast.makeText(this, "sesion iniciada", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }else{
            Toast.makeText(this, "error al iniciar sesion", Toast.LENGTH_SHORT).show()
        }
    }

    // 3. MÉTODO PARA AUTENTICAR EN FIREBSAE
    private fun autenticarFirebase(idToken: String){

        val credencialFirebase = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credencialFirebase).addOnCompleteListener(this) {

                inicio ->
            //validar que el inicio de sesion del usuario haya sido correcto
            if(inicio.isSuccessful){
                //crear variable que guarde info del usuario
                val usuario = auth.currentUser
                Toast.makeText(this, "Bievenido ${usuario?.displayName}  :D", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }else{
                Toast.makeText(this, "Error al iniciar con Firebase >:/", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 4. MÉTODO PARA PROCESAR LA CREDENCIAL
    private fun procesarCredencial(credential: Credential){

        if(credential is CustomCredential && credential.type ==
            GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL){

            try {
                //crear variable que permita identificar la credencial del usuario
                val credencialGoogle = GoogleIdTokenCredential.createFrom(
                    credential.data
                )

                autenticarFirebase(credencialGoogle.idToken)

            }catch (e : GoogleIdTokenParsingException){
                Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
            }
        }
    }
    // 5. MÉTODO PARA INICIAR SESIÓN
    private fun iniciarSesionGoogle(){

        val inicioGoogle = GetSignInWithGoogleOption.Builder(
            getString(R.string.default_web_client_id))
            .build()

        val solicitud = GetCredentialRequest.Builder()
            .addCredentialOption(inicioGoogle)
            .build()
        //verificar credenciales del usuario
        lifecycleScope.launch {
            try {
                //verificar si las credenciales del usuario pueden ser utilizadas
                val resultado = credentialManager.getCredential(
                    context = this@MainActivity,
                    request = solicitud)
                //procesar credencial para ver si le usuario entra o no
                procesarCredencial(resultado.credential)

            }catch (e : GetCredentialException){
                Toast.makeText(this@MainActivity, "Error en las credenciales del usuario", Toast.LENGTH_SHORT).show()
            }
        }
    }
    //METODO PARA MANTENER LAS SESIONES ACTIVAS
    override fun onStart() {
        super.onStart()

        //info del usuario actual
        val usuarioActual = auth.currentUser
        if(usuarioActual != null){
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}