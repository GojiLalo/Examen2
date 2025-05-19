package com.example.examen2

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore

class EditUserActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore

    private lateinit var tvEditUserUid: TextView
    private lateinit var etEditUserName: TextInputEditText
    private lateinit var etEditUserEmail: TextInputEditText
    private lateinit var etEditUserAddress: TextInputEditText
    private lateinit var etEditUserPhone: TextInputEditText
    private lateinit var btnSaveUserChanges: Button

    private var userUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_user)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firestore = FirebaseFirestore.getInstance()

        // Asignar vistas
        tvEditUserUid = findViewById(R.id.tvEditUserUid)
        etEditUserName = findViewById(R.id.etEditUserName)
        etEditUserEmail = findViewById(R.id.etEditUserEmail)
        etEditUserAddress = findViewById(R.id.etEditUserAddress)
        etEditUserPhone = findViewById(R.id.etEditUserPhone)
        btnSaveUserChanges = findViewById(R.id.btnSaveUserChanges)

        // Obtener los datos del usuario de los extras del Intent
        userUid = intent.getStringExtra("user_uid")
        val userEmail = intent.getStringExtra("user_email")
        val userName = intent.getStringExtra("user_name")
        val userAddress = intent.getStringExtra("user_address")
        val userPhone = intent.getStringExtra("user_phone")
        val userRole = intent.getStringExtra("user_role") // Aunque no se edite, se puede mostrar si es necesario

        // Rellenar los campos con los datos actuales del usuario
        tvEditUserUid.text = "UID: $userUid"
        etEditUserName.setText(userName)
        etEditUserEmail.setText(userEmail)
        etEditUserAddress.setText(userAddress)
        etEditUserPhone.setText(userPhone)

        // Listener para el botón de guardar cambios
        btnSaveUserChanges.setOnClickListener {
            saveUserChanges()
        }
    }

    private fun saveUserChanges() {
        val newName = etEditUserName.text.toString().trim()
        val newAddress = etEditUserAddress.text.toString().trim()
        val newPhone = etEditUserPhone.text.toString().trim()

        if (newName.isEmpty() || newAddress.isEmpty() || newPhone.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
            return
        }

        userUid?.let { uid ->
            val userUpdates = hashMapOf(
                "name" to newName,
                "address" to newAddress,
                "phone" to newPhone
                // El email y el rol no se pueden cambiar directamente desde aquí para evitar inconsistencias
            )

            firestore.collection("users").document(uid)
                .update(userUpdates as Map<String, Any>) // Casteo necesario para el método update
                .addOnSuccessListener {
                    Toast.makeText(this, "Datos del usuario actualizados correctamente.", Toast.LENGTH_SHORT).show()
                    finish() // Regresar a AdminActivity
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al actualizar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            Toast.makeText(this, "UID del usuario no disponible.", Toast.LENGTH_SHORT).show()
        }
    }
}