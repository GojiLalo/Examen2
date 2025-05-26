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
import com.google.firebase.messaging.FirebaseMessaging // Nuevo import

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var etRegEmail: TextInputEditText
    private lateinit var etRegPassword: TextInputEditText
    private lateinit var etRegName: TextInputEditText
    private lateinit var etRegAddress: TextInputEditText
    private lateinit var etRegPhone: TextInputEditText
    private lateinit var btnRegisterUser: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout_register)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        etRegEmail = findViewById(R.id.etEmail)
        etRegPassword = findViewById(R.id.etPassword)
        etRegName = findViewById(R.id.etName)
        etRegAddress = findViewById(R.id.etAddress)
        etRegPhone = findViewById(R.id.etPhone)
        btnRegisterUser = findViewById(R.id.btnRegister)

        btnRegisterUser.setOnClickListener {
            performRegistration()
        }
    }

    private fun performRegistration() {
        val email = etRegEmail.text.toString().trim()
        val password = etRegPassword.text.toString().trim()
        val name = etRegName.text.toString().trim()
        val address = etRegAddress.text.toString().trim()
        val phone = etRegPhone.text.toString().trim()

        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || address.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    firebaseUser?.let { user ->
                        // Obtener el token FCM
                        FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
                            val fcmToken = if (tokenTask.isSuccessful) tokenTask.result else null

                            val newUser = User(
                                uid = user.uid,
                                email = email,
                                name = name,
                                address = address,
                                phone = phone,
                                role = "normal", // Los usuarios registrados son "normales" por defecto
                                fcmTokens = if (fcmToken != null) listOf(fcmToken) else emptyList()
                            )

                            firestore.collection("users").document(user.uid)
                                .set(newUser)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Registro exitoso.", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error al guardar datos de usuario: ${e.message}", Toast.LENGTH_SHORT).show()
                                    // Opcional: Eliminar usuario de Auth si falla Firestore
                                    user.delete()
                                }
                        }
                    }
                } else {
                    Toast.makeText(this, "Fallo el registro: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}