package com.jimmy.valladares.notecoupletaker.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.jimmy.valladares.notecoupletaker.utils.NotificationHelper

/**
 * Servicio en primer plano que mantiene la aplicación activa para asegurar
 * que el NotificationCaptureService funcione de manera persistente.
 */
class KeepAliveService : Service() {

    companion object {
        private const val TAG = "KeepAliveService"
    }

    private lateinit var notificationHelper: NotificationHelper

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "📱 KeepAliveService creado")
        
        // Inicializar el helper de notificaciones
        notificationHelper = NotificationHelper(this)
        
        // Iniciar como servicio en primer plano inmediatamente
        startForegroundService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "🚀 KeepAliveService iniciado")
        
        // Asegurar que estemos en primer plano
        if (!isRunningInForeground()) {
            startForegroundService()
        }
        
        // Retornar START_STICKY para que el sistema reinicie el servicio si es terminado
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        // Este servicio no se vincula a ningún componente
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "🛑 KeepAliveService destruido")
    }

    /**
     * Inicia el servicio en primer plano con una notificación persistente
     */
    private fun startForegroundService() {
        try {
            val notification = notificationHelper.createForegroundServiceNotification()
            startForeground(NotificationHelper.CAPTURE_SERVICE_NOTIFICATION_ID, notification)
            Log.d(TAG, "🚀 Servicio iniciado en primer plano")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al iniciar servicio en primer plano", e)
        }
    }

    /**
     * Verifica si el servicio está corriendo en primer plano
     */
    private fun isRunningInForeground(): Boolean {
        // Esta es una implementación simple, en un caso real podrías
        // verificar el estado actual del servicio
        return true
    }
}
