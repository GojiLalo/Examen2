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
import com.google.firebase.messaging.FirebaseMessaging

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
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        // Obtener el token FCM y actualizar el usuario
                        FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
                            if (tokenTask.isSuccessful) {
                                val fcmToken = tokenTask.result
                                firestore.collection("users").document(userId).get()
                                    .addOnSuccessListener { document ->
                                        if (document != null && document.exists()) {
                                            val user = document.toObject(User::class.java)
                                            user?.let { existingUser ->
                                                val currentTokens = existingUser.fcmTokens.toMutableList()
                                                if (!currentTokens.contains(fcmToken)) {
                                                    currentTokens.add(fcmToken)
                                                }
                                                // Actualizar solo el campo fcmTokens
                                                firestore.collection("users").document(userId)
                                                    .update("fcmTokens", currentTokens)
                                                    .addOnSuccessListener {
                                                        // Token actualizado, proceder con la redirección
                                                        redirectToUserActivity(existingUser.role)
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Toast.makeText(this, "Error al actualizar token FCM: ${e.message}", Toast.LENGTH_SHORT).show()
                                                        redirectToUserActivity(existingUser.role) // Redirigir de todos modos
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
                            } else {
                                Toast.makeText(this, "No se pudo obtener el token FCM.", Toast.LENGTH_SHORT).show()
                                // Si no se puede obtener el token, aún así intentar redirigir
                                firestore.collection("users").document(userId).get()
                                    .addOnSuccessListener { document ->
                                        val user = document.toObject(User::class.java)
                                        user?.let { redirectToUserActivity(it.role) }
                                    }
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "Fallo el inicio de sesion: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Función auxiliar para redirigir
    private fun redirectToUserActivity(role: String) {
        if (role == "admin") {
            val intent = Intent(this, AdminActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val intent = Intent(this, NormalUserActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}