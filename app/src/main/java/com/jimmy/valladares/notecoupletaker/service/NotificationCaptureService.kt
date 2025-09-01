package com.jimmy.valladares.notecoupletaker.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.jimmy.valladares.notecoupletaker.data.repository.NotificationRepository
import com.jimmy.valladares.notecoupletaker.domain.model.CapturedNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Servicio que escucha y captura todas las notificaciones que llegan al dispositivo.
 * Este servicio hereda de NotificationListenerService y se encarga de interceptar
 * las notificaciones para guardarlas en Firestore.
 */
class NotificationCaptureService : NotificationListenerService() {

    companion object {
        private const val TAG = "NotificationCapture"
        
        // Paquetes que queremos ignorar para evitar spam en los logs y en Firestore
        private val IGNORED_PACKAGES = setOf(
            "android", // Notificaciones del sistema
            "com.android.systemui", // UI del sistema
            "com.jimmy.valladares.notecoupletaker" // Nuestra propia app
        )
    }
    
    // Repositorio para guardar notificaciones en Firestore
    private val notificationRepository = NotificationRepository()
    
    // CoroutineScope para operaciones asíncronas
    // Usamos SupervisorJob para que si falla una operación, no cancele las demás
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Se llama cuando el servicio se conecta exitosamente al sistema de notificaciones
     */
    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "✅ NotificationCaptureService conectado exitosamente")
        Log.d(TAG, "🔥 Firebase inicializado: ${com.google.firebase.FirebaseApp.getInstance() != null}")
        Log.d(TAG, "📡 El servicio está listo para capturar notificaciones y guardarlas en Firestore")
        
        // Test de conectividad con Firestore
        serviceScope.launch {
            try {
                val testDoc = mapOf("test" to "connection", "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp())
                notificationRepository.firestore.collection("connection_test").add(testDoc).await()
                Log.d(TAG, "🎯 Test de conexión a Firestore: EXITOSO")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Test de conexión a Firestore: FALLIDO", e)
            }
        }
    }

    /**
     * Se llama cuando el servicio se desconecta del sistema de notificaciones
     */
    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "❌ NotificationCaptureService desconectado")
        Log.w(TAG, "El servicio ya no puede capturar notificaciones")
    }

    /**
     * Se llama cuando el servicio es destruido
     */
    override fun onDestroy() {
        super.onDestroy()
        // Cancelar todas las operaciones pendientes
        serviceScope.coroutineContext.job.cancel()
        Log.d(TAG, "🛑 NotificationCaptureService destruido")
    }

    /**
     * Método principal: se ejecuta cada vez que llega una nueva notificación al dispositivo
     * @param sbn StatusBarNotification que contiene toda la información de la notificación
     */
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        
        try {
            // Extraer información básica de la notificación
            val packageName = sbn.packageName
            
            // Filtrar notificaciones que no nos interesan
            if (shouldIgnoreNotification(packageName)) {
                Log.v(TAG, "🔇 Ignorando notificación de: $packageName")
                return
            }
            
            val postTime = sbn.postTime
            val notification = sbn.notification
            
            // Extraer el título y texto de la notificación
            val extras = notification.extras
            val title = extras.getCharSequence("android.title")?.toString()
            val text = extras.getCharSequence("android.text")?.toString()
            val bigText = extras.getCharSequence("android.bigText")?.toString()
            val subText = extras.getCharSequence("android.subText")?.toString()
            
            // Usar bigText si está disponible, si no usar text normal
            val finalText = bigText ?: text
            
            // Crear objeto para guardar en Firestore
            val capturedNotification = CapturedNotification(
                packageName = packageName,
                title = title,
                text = finalText
                // timestamp se establece automáticamente con @ServerTimestamp
            )
            
            Log.d(TAG, "🚀 Iniciando guardado en Firestore...")
            
            // Guardar en Firestore de forma asíncrona
            serviceScope.launch {
                try {
                    notificationRepository.saveNotification(capturedNotification)
                    Log.d(TAG, "💾 Notificación guardada en Firestore: $packageName")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error al guardar notificación en Firestore", e)
                    Log.e(TAG, "🔍 Package: $packageName, Title: $title")
                }
            }
            
            // Obtener información adicional útil para los logs
            val appName = getAppName(packageName)
            val category = notification.category ?: "Sin categoría"
            val priority = notification.priority
            val isOngoing = notification.flags and android.app.Notification.FLAG_ONGOING_EVENT != 0
            val isClearable = notification.flags and android.app.Notification.FLAG_NO_CLEAR == 0
            
            // Log detallado de la notificación capturada
            Log.d(TAG, "📱 ===== NUEVA NOTIFICACIÓN CAPTURADA =====")
            Log.d(TAG, "📦 Paquete: $packageName")
            Log.d(TAG, "🎯 App: $appName")
            Log.d(TAG, "🕐 Hora: ${java.util.Date(postTime)}")
            Log.d(TAG, "📋 Título: ${title ?: "Sin título"}")
            Log.d(TAG, "📝 Texto: ${finalText ?: "Sin texto"}")
            if (subText != null) Log.d(TAG, "📄 Subtexto: $subText")
            Log.d(TAG, "🏷️ Categoría: $category")
            Log.d(TAG, "⚡ Prioridad: $priority")
            Log.d(TAG, "🔄 Permanente: $isOngoing")
            Log.d(TAG, "🗑️ Eliminable: $isClearable")
            Log.d(TAG, "🆔 ID: ${sbn.id}")
            Log.d(TAG, "🏷️ Tag: ${sbn.tag ?: "Sin tag"}")
            Log.d(TAG, "💾 Guardada en Firestore: ✅")
            Log.d(TAG, "============================================")
            
            // Log compacto para fácil lectura
            Log.i(TAG, "📨 [$appName] ${title ?: "Sin título"} - ${finalText ?: "Sin texto"}")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al procesar notificación", e)
        }
    }

    /**
     * Se llama cuando una notificación es removida/descartada por el usuario o por el sistema
     * @param sbn StatusBarNotification de la notificación que fue removida
     */
    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        
        try {
            val packageName = sbn.packageName
            val title = sbn.notification.extras.getCharSequence("android.title")?.toString() ?: "Sin título"
            
            Log.d(TAG, "🗑️ Notificación removida de $packageName: $title")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al procesar notificación removida", e)
        }
    }

    /**
     * Determina si una notificación debe ser ignorada basándose en el paquete
     * @param packageName Nombre del paquete de la aplicación que envió la notificación
     * @return true si la notificación debe ser ignorada, false en caso contrario
     */
    private fun shouldIgnoreNotification(packageName: String): Boolean {
        return IGNORED_PACKAGES.any { ignoredPackage ->
            packageName.startsWith(ignoredPackage)
        }
    }

    /**
     * Intenta obtener el nombre legible de la aplicación desde el package name
     * @param packageName Nombre del paquete
     * @return Nombre de la aplicación o el package name si no se puede obtener
     */
    private fun getAppName(packageName: String): String {
        return try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: Exception) {
            packageName // Fallback al package name si no se puede obtener el nombre
        }
    }
}