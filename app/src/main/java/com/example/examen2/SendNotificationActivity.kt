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
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class SendNotificationActivity : AppCompatActivity() {

    private lateinit var etNotificationMessage: TextInputEditText
    private lateinit var btnSendNotificationConfirm: Button
    private lateinit var tvSelectedUsersCount: TextView

    private var selectedUserUids: ArrayList<String>? = null
    private val client = OkHttpClient()

    // ¡IMPORTANTE! Reemplaza esto con la URL real de tu Firebase Cloud Function
    private val CLOUD_FUNCTION_URL = "https://sendnotificationfb-dhogzipzua-uc.a.run.app" // Ejemplo: "https://us-central1-your-project-id.cloudfunctions.net/sendNotification"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_send_notification)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etNotificationMessage = findViewById(R.id.etNotificationMessage)
        btnSendNotificationConfirm = findViewById(R.id.btnSendNotificationConfirm)
        tvSelectedUsersCount = findViewById(R.id.tvSelectedUsersCount)

        selectedUserUids = intent.getStringArrayListExtra("selected_user_uids")

        tvSelectedUsersCount.text = "Usuarios seleccionados: ${selectedUserUids?.size ?: 0}"

        btnSendNotificationConfirm.setOnClickListener {
            sendNotification()
        }
    }

    private fun sendNotification() {
        val message = etNotificationMessage.text.toString().trim()

        if (message.isEmpty()) {
            Toast.makeText(this, "El mensaje no puede estar vacío.", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedUserUids.isNullOrEmpty()) {
            Toast.makeText(this, "No hay usuarios seleccionados para enviar la notificación.", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear el cuerpo de la solicitud JSON
        val jsonBody = JSONObject().apply {
            put("message", message)
            put("uids", JSONArray(selectedUserUids))
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(CLOUD_FUNCTION_URL)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@SendNotificationActivity, "Error al enviar notificación: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@SendNotificationActivity, "Notificación enviada exitosamente.", Toast.LENGTH_SHORT).show()
                        finish() // Cerrar esta actividad
                    } else {
                        Toast.makeText(this@SendNotificationActivity, "Fallo al enviar notificación: ${response.code} - $responseBody", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }
}