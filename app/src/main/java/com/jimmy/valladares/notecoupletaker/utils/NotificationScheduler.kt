package com.jimmy.valladares.notecoupletaker.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jimmy.valladares.notecoupletaker.MainActivity
import com.jimmy.valladares.notecoupletaker.R
import java.util.Calendar

/**
 * Clase utilitaria para programar y gestionar notificaciones de recordatorios
 */
class NotificationScheduler(private val context: Context) {
    
    companion object {
        const val CHANNEL_ID = "reminder_channel"
        const val CHANNEL_NAME = "Recordatorios de Compromisos"
        const val NOTIFICATION_ID_BASE = 1000
        private const val TAG = "NotificationScheduler"
    }
    
    init {
        createNotificationChannel()
    }
    
    /**
     * Crea el canal de notificaciones (requerido para Android 8.0+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones para recordar compromisos de pareja"
                enableVibration(true)
                setShowBadge(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Programa un recordatorio diario para un compromiso
     * @param commitmentId ID del compromiso
     * @param commitmentTitle Título del compromiso
     * @param timeString Hora en formato "HH:mm"
     */
    fun scheduleReminder(commitmentId: Int, commitmentTitle: String, timeString: String) {
        Log.d(TAG, "Programando recordatorio para compromiso $commitmentId a las $timeString")
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra("commitmentId", commitmentId)
            putExtra("commitmentTitle", commitmentTitle)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            commitmentId, // Usar commitmentId como requestCode para poder cancelar después
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val timeParts = timeString.split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()
        
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // Si la hora ya pasó hoy, programar para mañana
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
                Log.d(TAG, "La hora ya pasó hoy, programando para mañana")
            }
        }
        
        Log.d(TAG, "Programando alarma para: ${calendar.time}")
        
        // Para testing inmediato, programar una notificación en 10 segundos
        val testCalendar = Calendar.getInstance().apply {
            add(Calendar.SECOND, 10)
        }
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+ requiere verificar permisos especiales
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        testCalendar.timeInMillis, // Usar testCalendar para prueba inmediata
                        pendingIntent
                    )
                    Log.d(TAG, "Alarma exacta programada")
                } else {
                    // Fallback a alarma inexacta
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        testCalendar.timeInMillis,
                        pendingIntent
                    )
                    Log.d(TAG, "Alarma inexacta programada (sin permisos de alarma exacta)")
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    testCalendar.timeInMillis, // Usar testCalendar para prueba inmediata
                    pendingIntent
                )
                Log.d(TAG, "Alarma exacta programada (Android < 12)")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Error de seguridad al programar alarma", e)
            // Mostrar notificación inmediata para testing
            showTestNotification(commitmentId, commitmentTitle)
        }
    }
    
    /**
     * Cancela el recordatorio de un compromiso
     * @param commitmentId ID del compromiso
     */
    fun cancelReminder(commitmentId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, ReminderBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            commitmentId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
    
    /**
     * Verifica si la aplicación tiene permisos de notificación
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        } else {
            true // Los permisos de notificación no se requieren antes de Android 13
        }
    }
    
    /**
     * Muestra una notificación de prueba inmediata
     */
    private fun showTestNotification(commitmentId: Int, commitmentTitle: String) {
        Log.d(TAG, "Mostrando notificación de prueba inmediata")
        
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
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Recordatorio de prueba")
            .setContentText(commitmentTitle)
            .setStyle(NotificationCompat.BigTextStyle().bigText("Esto es una prueba: $commitmentTitle"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()
        
        val notificationManager = NotificationManagerCompat.from(context)
        
        try {
            notificationManager.notify(
                NOTIFICATION_ID_BASE + commitmentId,
                notification
            )
            Log.d(TAG, "Notificación de prueba enviada")
        } catch (e: SecurityException) {
            Log.e(TAG, "Error al mostrar notificación de prueba", e)
        }
    }
}
