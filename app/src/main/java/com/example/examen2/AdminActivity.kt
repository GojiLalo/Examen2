package com.example.examen2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var recyclerViewUsers: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private val userList = mutableListOf<User>()

    private lateinit var btnLogoutAdmin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar Firebase Auth y Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Asignar vistas
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers)
        btnLogoutAdmin = findViewById(R.id.btnLogoutAdmin)

        // Configurar RecyclerView
        userAdapter = UserAdapter(userList) { user ->
            // Manejar el clic en un usuario (abrir EditUserActivity)
            val intent = Intent(this, EditUserActivity::class.java).apply {
                putExtra("user_uid", user.uid)
                putExtra("user_email", user.email)
                putExtra("user_name", user.name)
                putExtra("user_address", user.address)
                putExtra("user_phone", user.phone)
                putExtra("user_role", user.role)
            }
            startActivity(intent)
        }
        recyclerViewUsers.layoutManager = LinearLayoutManager(this)
        recyclerViewUsers.adapter = userAdapter

        // Cargar los usuarios
        loadNormalUsers()

        // Listener para el botón de cerrar sesión
        btnLogoutAdmin.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            Toast.makeText(this, "Sesión de administrador cerrada.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Recargar la lista de usuarios cada vez que se regresa a AdminActivity
        loadNormalUsers()
    }

    private fun loadNormalUsers() {
        // Cargar solo usuarios con rol "normal"
        firestore.collection("users")
            .whereEqualTo("role", "normal") // Filtra solo usuarios normales
            .get()
            .addOnSuccessListener { result ->
                val users = mutableListOf<User>()
                for (document in result) {
                    val user = document.toObject(User::class.java)
                    users.add(user)
                }
                userAdapter.updateUsers(users)
                if (users.isEmpty()) {
                    Toast.makeText(this, "No se encontraron usuarios normales.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar usuarios: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}