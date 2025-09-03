package com.jimmy.valladares.notecoupletaker.service

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.jimmy.valladares.notecoupletaker.utils.NotificationHelper
import com.jimmy.valladares.notecoupletaker.utils.NotificationPermissionUtils

/**
 * Servicio en primer plano que mantiene la aplicación activa para asegurar
 * que el NotificationCaptureService funcione de manera persistente.
 */
class KeepAliveService : Service() {

    companion object {
        private const val TAG = "KeepAliveService"
        private const val SELF_CHECK_INTERVAL = 30 * 1000L // 30 segundos
    }

    private lateinit var notificationHelper: NotificationHelper
    private val handler = Handler(Looper.getMainLooper())
    private var selfCheckRunnable: Runnable? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "📱 KeepAliveService creado")
        
        // Inicializar el helper de notificaciones
        notificationHelper = NotificationHelper(this)
        
        // Iniciar como servicio en primer plano inmediatamente
        startForegroundService()
        
        // Iniciar auto-verificación periódica
        startSelfCheck()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "🚀 KeepAliveService iniciado")
        
        // Asegurar que estemos en primer plano
        if (!isRunningInForeground()) {
            startForegroundService()
        }
        
        // START_REDELIVER_INTENT: Si el servicio es terminado, el sistema lo reiniciará
        // y volverá a llamar a onStartCommand con el último Intent
        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent?): IBinder? {
        // Este servicio no se vincula a ningún componente
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "🛑 KeepAliveService destruido")
        
        // Detener auto-verificación
        stopSelfCheck()
        
        // Intentar reiniciar el servicio cuando es destruido
        restartService()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d(TAG, "📱 Tarea removida - intentando mantener servicio activo")
        
        // Cuando el usuario cierra la aplicación desde recientes, mantener el servicio
        restartService()
    }

    /**
     * Intenta reiniciar el servicio después de ser terminado
     */
    private fun restartService() {
        try {
            val restartIntent = Intent(applicationContext, KeepAliveService::class.java)
            applicationContext.startForegroundService(restartIntent)
            Log.d(TAG, "🔄 Servicio programado para reinicio")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al programar reinicio del servicio", e)
        }
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

    /**
     * Inicia la auto-verificación periódica del estado del servicio
     */
    private fun startSelfCheck() {
        selfCheckRunnable = object : Runnable {
            override fun run() {
                try {
                    // Verificar si aún tenemos permisos
                    if (!NotificationPermissionUtils.isNotificationListenerEnabled(this@KeepAliveService)) {
                        Log.d(TAG, "🚫 Permisos perdidos - deteniendo servicio")
                        stopSelf()
                        return
                    }
                    
                    Log.v(TAG, "✅ Auto-verificación: Servicio activo")
                    
                    // Programar próxima verificación
                    handler.postDelayed(this, SELF_CHECK_INTERVAL)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error en auto-verificación", e)
                }
            }
        }
        
        // Iniciar la primera verificación
        handler.postDelayed(selfCheckRunnable!!, SELF_CHECK_INTERVAL)
        Log.d(TAG, "🔍 Auto-verificación iniciada")
    }

    /**
     * Detiene la auto-verificación periódica
     */
    private fun stopSelfCheck() {
        selfCheckRunnable?.let { runnable ->
            handler.removeCallbacks(runnable)
            selfCheckRunnable = null
            Log.d(TAG, "🔍 Auto-verificación detenida")
        }
    }
}
