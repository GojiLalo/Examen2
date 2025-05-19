package com.example.examen2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NormalUserActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvUserAddress: TextView
    private lateinit var tvUserPhone: TextView
    private lateinit var tvUserRole: TextView
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_normal_user)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar Firebase Auth y Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Asignar vistas
        tvUserName = findViewById(R.id.tvUserName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        tvUserAddress = findViewById(R.id.tvUserAddress)
        tvUserPhone = findViewById(R.id.tvUserPhone)
        tvUserRole = findViewById(R.id.tvUserRole)
        btnLogout = findViewById(R.id.btnLogout)

        // Cargar los datos del usuario
        loadUserData()

        // Listener para el botón de cerrar sesión
        btnLogout.setOnClickListener {
            auth.signOut() // Cerrar sesión del usuario actual
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Limpiar la pila de actividades
            startActivity(intent)
            finish() // Finalizar esta actividad
            Toast.makeText(this, "Sesión cerrada.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val user = document.toObject(User::class.java)
                        user?.let {
                            tvUserName.text = "Nombre: ${it.name}"
                            tvUserEmail.text = "Correo: ${it.email}"
                            tvUserAddress.text = "Dirección: ${it.address}"
                            tvUserPhone.text = "Teléfono: ${it.phone}"
                            tvUserRole.text = "Rol: ${it.role}"
                        }
                    } else {
                        Toast.makeText(this, "Datos de usuario no encontrados en Firestore.", Toast.LENGTH_LONG).show()
                        // Opcional: Cerrar sesión si no se encuentran los datos
                        auth.signOut()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al cargar datos del usuario: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            // No hay usuario autenticado, redirigir a la pantalla de login
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            Toast.makeText(this, "No hay usuario autenticado.", Toast.LENGTH_SHORT).show()
        }
    }
}