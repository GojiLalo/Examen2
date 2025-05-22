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
    private lateinit var btnSendNotification: Button // Nuevo botón

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        recyclerViewUsers = findViewById(R.id.recyclerViewUsers)
        btnLogoutAdmin = findViewById(R.id.btnLogoutAdmin)
        btnSendNotification = findViewById(R.id.btnSendNotification) // Asignar el nuevo botón

        userAdapter = UserAdapter(userList) { user ->
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

        loadNormalUsers()

        btnLogoutAdmin.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            Toast.makeText(this, "Sesión de administrador cerrada.", Toast.LENGTH_SHORT).show()
        }

        // Listener para el botón de enviar notificación
        btnSendNotification.setOnClickListener {
            val selectedUids = userAdapter.getSelectedUids()
            if (selectedUids.isNotEmpty()) {
                val intent = Intent(this, SendNotificationActivity::class.java).apply {
                    putStringArrayListExtra("selected_user_uids", ArrayList(selectedUids))
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Por favor, selecciona al menos un usuario.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadNormalUsers()
        userAdapter.clearSelections() // Limpiar selecciones al regresar a la actividad
    }

    private fun loadNormalUsers() {
        firestore.collection("users")
            .whereEqualTo("role", "normal")
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