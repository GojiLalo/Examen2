package com.example.examen2

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "MyFirebaseMsgService"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Comprobar si el mensaje contiene datos de notificación.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            sendNotification(it.title, it.body)
        }

        // Comprobar si el mensaje contiene datos personalizados.
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
            // Aquí puedes manejar datos personalizados si los envías desde la función
            // Por ejemplo, si envías {'type': 'admin_message', 'sender': 'admin'}
            // val type = remoteMessage.data["type"]
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        // Envía este token a tu servidor si lo necesitas para actualizar los tokens de usuario
        // O, si el usuario está logueado, actualiza el token en Firestore como hicimos en MainActivity/RegisterActivity
        // Si el usuario no está logueado, puedes guardarlo localmente y subirlo cuando lo esté.
    }

    private fun sendNotification(title: String?, messageBody: String?) {
        val channelId = "default_channel_id" // ID del canal de notificación
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title ?: "Notificación")
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Para Android 8.0 (Oreo) y superior, se requiere un canal de notificación
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Canal por defecto", // Nombre visible del canal
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para notificaciones generales de la aplicación"
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID de notificación */, notificationBuilder.build())
    }
}