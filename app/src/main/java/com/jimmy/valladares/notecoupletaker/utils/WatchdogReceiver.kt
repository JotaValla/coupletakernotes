package com.jimmy.valladares.notecoupletaker.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import com.jimmy.valladares.notecoupletaker.service.KeepAliveService

/**
 * BroadcastReceiver que actúa como "perro guardián" para verificar y reiniciar
 * el servicio de captura si es terminado por el sistema o el limpiador
 */
class WatchdogReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "WatchdogReceiver"
        private const val WATCHDOG_ACTION = "com.jimmy.valladares.notecoupletaker.WATCHDOG_ALARM"
        private const val WATCHDOG_INTERVAL = 60 * 1000L // 1 minuto
        
        /**
         * Programa el watchdog para que verifique periódicamente el estado del servicio
         */
        fun scheduleWatchdog(context: Context) {
            try {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(context, WatchdogReceiver::class.java).apply {
                    action = WATCHDOG_ACTION
                }
                
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    9999,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                // Cancelar alarmas anteriores
                alarmManager.cancel(pendingIntent)
                
                // Programar nueva alarma repetitiva
                alarmManager.setRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + WATCHDOG_INTERVAL,
                    WATCHDOG_INTERVAL,
                    pendingIntent
                )
                
                Log.d(TAG, "🐕 Watchdog programado cada ${WATCHDOG_INTERVAL / 1000} segundos")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al programar watchdog", e)
            }
        }
        
        /**
         * Cancela el watchdog
         */
        fun cancelWatchdog(context: Context) {
            try {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(context, WatchdogReceiver::class.java).apply {
                    action = WATCHDOG_ACTION
                }
                
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    9999,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                alarmManager.cancel(pendingIntent)
                Log.d(TAG, "🐕 Watchdog cancelado")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al cancelar watchdog", e)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            WATCHDOG_ACTION -> {
                Log.d(TAG, "🐕 Watchdog verificando estado del servicio...")
                
                // Verificar si tenemos permisos de notificación
                if (NotificationPermissionUtils.isNotificationListenerEnabled(context)) {
                    // Intentar reiniciar el servicio (si no está corriendo, se iniciará)
                    try {
                        val serviceIntent = Intent(context, KeepAliveService::class.java)
                        context.startForegroundService(serviceIntent)
                        Log.d(TAG, "🔄 Servicio KeepAlive verificado/reiniciado")
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error al verificar/reiniciar servicio", e)
                    }
                } else {
                    Log.d(TAG, "🚫 Permisos de notificación deshabilitados - cancelando watchdog")
                    cancelWatchdog(context)
                }
            }
        }
    }
}
