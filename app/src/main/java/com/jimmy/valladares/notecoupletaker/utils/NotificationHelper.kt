package com.jimmy.valladares.notecoupletaker.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.jimmy.valladares.notecoupletaker.MainActivity
import com.jimmy.valladares.notecoupletaker.R

/**
 * Clase utilitaria para crear y gestionar las notificaciones del servicio en primer plano.
 */
class NotificationHelper(private val context: Context) {
    
    companion object {
        const val CAPTURE_SERVICE_CHANNEL_ID = "capture_service_channel"
        const val CAPTURE_SERVICE_NOTIFICATION_ID = 2000
        private const val CHANNEL_NAME = "Servicio de Captura"
        private const val CHANNEL_DESCRIPTION = "Notificación persistente del servicio de captura de notificaciones"
    }
    
    init {
        createNotificationChannel()
    }
    
    /**
     * Crea el canal de notificación para el servicio en primer plano.
     * Solo se ejecuta en Android 8.0 (API 26) y superior.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CAPTURE_SERVICE_CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW // Baja importancia para evitar interrupciones
            ).apply {
                description = CHANNEL_DESCRIPTION
                setShowBadge(false) // No mostrar badge en el ícono de la app
                enableLights(false) // No usar luces LED
                enableVibration(false) // No vibrar
                setSound(null, null) // Sin sonido
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Crea la notificación para el servicio en primer plano.
     * @return Objeto Notification configurado para el servicio
     */
    fun createForegroundServiceNotification(): Notification {
        // Intent para abrir la aplicación cuando el usuario toque la notificación
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(context, CAPTURE_SERVICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Usar el mismo ícono que ya tienes
            .setContentTitle("NoteCoupleTaker está activo")
            .setContentText("Servicio de captura ejecutándose en segundo plano")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("El servicio está capturando notificaciones del sistema y guardándolas en tu historial. Toca para abrir la aplicación."))
            .setPriority(NotificationCompat.PRIORITY_LOW) // Prioridad baja para no molestar
            .setContentIntent(pendingIntent)
            .setOngoing(true) // La notificación no se puede deslizar para eliminar
            .setAutoCancel(false) // No se elimina automáticamente al tocarla
            .setShowWhen(false) // No mostrar timestamp
            .build()
    }
}
