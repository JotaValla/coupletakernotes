package com.jimmy.valladares.notecoupletaker.utils

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jimmy.valladares.notecoupletaker.MainActivity
import com.jimmy.valladares.notecoupletaker.R

/**
 * BroadcastReceiver que recibe las alarmas programadas y muestra las notificaciones
 */
class ReminderBroadcastReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "ReminderBroadcastReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Broadcast recibido: ${intent.action}")
        
        val commitmentId = intent.getIntExtra("commitmentId", -1)
        val commitmentTitle = intent.getStringExtra("commitmentTitle") ?: "Compromiso"
        
        Log.d(TAG, "CommitmentId: $commitmentId, Title: $commitmentTitle")
        
        if (commitmentId != -1) {
            showNotification(context, commitmentId, commitmentTitle)
        } else {
            Log.e(TAG, "CommitmentId inválido recibido")
        }
    }
    
    private fun showNotification(context: Context, commitmentId: Int, commitmentTitle: String) {
        Log.d(TAG, "Intentando mostrar notificación para compromiso: $commitmentId")
        
        // Intent para abrir la aplicación y navegar al detalle del compromiso
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("commitmentId", commitmentId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            commitmentId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, NotificationScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(commitmentTitle)
            .setStyle(NotificationCompat.BigTextStyle().bigText(commitmentTitle))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()
        
        val notificationManager = NotificationManagerCompat.from(context)
        
        // Verificar permisos antes de mostrar la notificación
        if (notificationManager.areNotificationsEnabled()) {
            try {
                val notificationId = NotificationScheduler.NOTIFICATION_ID_BASE + commitmentId
                notificationManager.notify(notificationId, notification)
                Log.d(TAG, "Notificación mostrada con ID: $notificationId")
            } catch (e: SecurityException) {
                Log.e(TAG, "Error de seguridad al mostrar notificación", e)
            }
        } else {
            Log.w(TAG, "Notificaciones deshabilitadas por el usuario")
        }
    }
}
