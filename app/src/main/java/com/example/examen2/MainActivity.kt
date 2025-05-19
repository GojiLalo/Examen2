package com.example.examen2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)

        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            performLogin()
        }
    }

    private fun performLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa tu correo y contraseña.", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login exitoso, ahora obtenemos el rol del usuario
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        firestore.collection("users").document(userId).get()
                            .addOnSuccessListener { document ->
                                if (document != null && document.exists()) {
                                    val user = document.toObject(User::class.java)
                                    user?.let {
                                        if (it.role == "admin") {
                                            // Redirigir a la actividad de administrador
                                            val intent = Intent(this, AdminActivity::class.java)
                                            startActivity(intent)
                                            finish() // Finaliza MainActivity para que el usuario no pueda volver atrás
                                        } else {
                                            // Redirigir a la actividad de usuario normal
                                            val intent = Intent(this, NormalUserActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                    }
                                } else {
                                    Toast.makeText(this, "No se encontró el perfil del usuario.", Toast.LENGTH_SHORT).show()
                                    auth.signOut()
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error al obtener el rol del usuario: ${e.message}", Toast.LENGTH_SHORT).show()
                                auth.signOut()
                            }
                    }
                } else {
                    Toast.makeText(this, "Fallo el inicio de sesion: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}