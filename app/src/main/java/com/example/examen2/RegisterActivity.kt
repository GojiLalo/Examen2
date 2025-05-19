package com.example.examen2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import android.widget.ToggleButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    // Declaración de vistas
    private lateinit var etEmail: TextInputEditText
    private lateinit var etName: TextInputEditText
    private lateinit var etAddress: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var toggleRole: ToggleButton
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout_register)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Inicializar vistas
        etEmail = findViewById(R.id.etEmail)
        etName = findViewById(R.id.etName)
        etAddress = findViewById(R.id.etAddress)
        etPhone = findViewById(R.id.etPhone)
        etPassword = findViewById(R.id.etPassword)
        toggleRole = findViewById(R.id.toggleRole)
        btnRegister = findViewById(R.id.btnRegister)

        // Configurar el click listener del botón de registro
        btnRegister.setOnClickListener {
            registerUser()
        }

        // Opcional: Configurar el TextView para ir a Login
        //findViewById<TextView>(R.id.tvLogin).setOnClickListener {
            // Aquí puedes agregar la navegación a LoginActivity si lo necesitas
            // startActivity(Intent(this, LoginActivity::class.java))
            // finish()
        //}
    }

    private fun registerUser() {
        // Obtener los valores de los campos
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val name = etName.text.toString().trim()
        val address = etAddress.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val role = if (toggleRole.isChecked) "admin" else "normal"

        // Validaciones básicas
        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || address.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        // Mostrar progreso (puedes agregar un ProgressBar si lo necesitas)
        btnRegister.isEnabled = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    firebaseUser?.let { user ->
                        val newUser = User(
                            uid = user.uid,
                            email = email,
                            name = name,
                            address = address,
                            phone = phone,
                            role = role
                        )

                        firestore.collection("users").document(user.uid)
                            .set(newUser)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()

                                // Redirigir a MainActivity
                                val intent = Intent(this, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                btnRegister.isEnabled = true
                                Toast.makeText(this, "Error al guardar datos: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                } else {
                    btnRegister.isEnabled = true
                    Toast.makeText(this, "Error en el registro: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}